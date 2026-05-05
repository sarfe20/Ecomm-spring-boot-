package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ChatRequestDTO;
import com.ecommerce.project.payload.ChatResponseDTO;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatbotServiceImpl implements ChatbotService {
    private static final int MAX_HISTORY_MESSAGES = 10;
    private static final int MAX_LIVE_CATALOG_PRODUCTS = 8;
    private static final Set<String> SEARCH_STOP_WORDS = Set.of(
            "about", "after", "again", "also", "available", "best", "buy", "can", "catalog",
            "cost", "could", "does", "find", "for", "from", "give", "have", "help", "how",
            "into", "list", "me", "need", "please", "price", "product", "products", "show",
            "stock", "store", "tell", "that", "the", "this", "under", "want", "what", "when",
            "which", "with", "you", "your"
    );

    private final RestTemplate restTemplate;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Value("${ai.chat.local-url:http://localhost:8081}")
    private String localAiUrl;

    public ChatbotServiceImpl(RestTemplate restTemplate,
                              CartRepository cartRepository,
                              ProductRepository productRepository) {
        this.restTemplate = restTemplate;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public ChatResponseDTO chat(ChatRequestDTO chatRequestDTO, Authentication authentication) {
        Map<String, Object> requestBody = Map.of(
                "question", buildQuestionWithHistory(chatRequestDTO, authentication),
                "k", 5
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    localAiUrl + "/doc/ask",
                    new HttpEntity<>(requestBody),
                    Map.class
            );

            return new ChatResponseDTO(extractAnswer(response.getBody()));
        } catch (Exception e) {
            throw new APIException("Local AI chatbot is not available. Start the desktop ai_chatbot server and Ollama, then try again. Details: " + e.getMessage());
        }
    }

    private boolean isAllowedRole(String role) {
        return "user".equals(role) || "assistant".equals(role);
    }

    private String buildQuestionWithHistory(ChatRequestDTO chatRequestDTO, Authentication authentication) {
        StringBuilder question = new StringBuilder();
        question.append("You are the customer assistant for this e-commerce store. ");
        question.append("Answer using the live product catalog context below first, then any indexed product catalog information. ");
        question.append("For product recommendations, personalize the answer from the customer's cart when available. ");
        question.append("Recommend in-stock products, mention the reason, and keep the answer concise. ");
        question.append("Do not invent prices, stock, discounts, or product details.\n\n");

        appendCustomerContext(question, authentication);
        appendLiveCatalogContext(question, chatRequestDTO.getMessage());

        if (chatRequestDTO.getHistory() != null && !chatRequestDTO.getHistory().isEmpty()) {
            question.append("Recent conversation:\n");
            chatRequestDTO.getHistory().stream()
                    .filter(message -> isAllowedRole(message.getRole()))
                    .limit(MAX_HISTORY_MESSAGES)
                    .forEach(message -> question
                            .append(message.getRole())
                            .append(": ")
                            .append(message.getContent())
                            .append("\n"));
            question.append("\n");
        }

        question.append("Customer question: ").append(chatRequestDTO.getMessage());
        return question.toString();
    }

    private void appendLiveCatalogContext(StringBuilder question, String customerMessage) {
        List<Product> products = findRelevantProducts(customerMessage);

        if (products.isEmpty()) {
            question.append("Live catalog context: no direct product matches were found in the database for this question. ");
            question.append("If you cannot answer from indexed catalog data either, say that no matching product was found.\n\n");
            return;
        }

        question.append("Live catalog context from the current database:\n");
        for (Product product : products) {
            question.append("- Product ID: ").append(product.getProductId())
                    .append("; Name: ").append(safe(product.getProductName()))
                    .append("; Category: ").append(product.getCategory() == null ? "Uncategorized" : safe(product.getCategory().getCategoryName()))
                    .append("; Description: ").append(safe(product.getDescription()))
                    .append("; Original price: ").append(product.getPrice())
                    .append("; Discount percentage: ").append(product.getDiscount())
                    .append("; Final selling price: ").append(product.getSpecialPrice())
                    .append("; Available stock quantity: ").append(product.getQuantity())
                    .append("\n");
        }
        question.append("Use these live database products as the most current source of truth.\n\n");
    }

    private List<Product> findRelevantProducts(String customerMessage) {
        LinkedHashMap<Long, Product> matches = new LinkedHashMap<>();
        PageRequest pageRequest = PageRequest.of(0, MAX_LIVE_CATALOG_PRODUCTS);
        List<String> searchTerms = extractSearchTerms(customerMessage);

        for (String keyword : searchTerms) {
            productRepository.searchCatalogForChatbot(keyword, pageRequest)
                    .forEach(product -> {
                        if (product.getProductId() != null && matches.size() < MAX_LIVE_CATALOG_PRODUCTS) {
                            matches.putIfAbsent(product.getProductId(), product);
                        }
                    });

            if (matches.size() >= MAX_LIVE_CATALOG_PRODUCTS) {
                break;
            }
        }

        if (matches.isEmpty() && shouldShowRecentProducts(customerMessage, searchTerms)) {
            productRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "productId")))
                    .forEach(product -> matches.putIfAbsent(product.getProductId(), product));
        }

        return List.copyOf(matches.values());
    }

    private List<String> extractSearchTerms(String customerMessage) {
        String normalizedMessage = customerMessage == null ? "" : customerMessage.toLowerCase();
        List<String> terms = Arrays.stream(normalizedMessage.split("[^a-z0-9]+"))
                .map(String::trim)
                .filter(term -> term.length() > 2)
                .filter(term -> !SEARCH_STOP_WORDS.contains(term))
                .distinct()
                .limit(6)
                .collect(Collectors.toList());

        return terms;
    }

    private boolean shouldShowRecentProducts(String customerMessage, List<String> searchTerms) {
        if (customerMessage == null || customerMessage.isBlank()) {
            return false;
        }

        String normalizedMessage = customerMessage.toLowerCase();
        return searchTerms.isEmpty()
                || normalizedMessage.contains("catalog")
                || normalizedMessage.contains("products")
                || normalizedMessage.contains("show")
                || normalizedMessage.contains("list");
    }

    private void appendCustomerContext(StringBuilder question, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            question.append("Customer context: not logged in. Ask one short preference question if needed, then recommend from available catalog data.\n\n");
            return;
        }

        question.append("Customer context:\n");
        question.append("Username: ").append(userDetails.getUsername()).append("\n");

        try {
            Cart cart = cartRepository.findCartByEmail(userDetails.getEmail());
            if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                question.append("Current cart: empty. Recommend products based on the customer's stated budget, category, and needs.\n\n");
                return;
            }

            question.append("Current cart items:\n");
            for (CartItem item : cart.getCartItems()) {
                if (item.getProduct() == null) {
                    continue;
                }

                question.append("- ")
                        .append(item.getProduct().getProductName())
                        .append(", quantity ")
                        .append(item.getQuantity())
                        .append(", price ")
                        .append(item.getProduct().getSpecialPrice())
                        .append(", stock ")
                        .append(item.getProduct().getQuantity());

                if (item.getProduct().getCategory() != null) {
                    question.append(", category ")
                            .append(item.getProduct().getCategory().getCategoryName());
                }

                question.append("\n");
            }

            question.append("Recommendation style: suggest related or better-value products that fit with the cart items.\n\n");
        } catch (Exception e) {
            question.append("Current cart: unavailable. Recommend from available catalog data.\n\n");
        }
    }

    private String extractAnswer(Map body) {
        if (body == null) {
            throw new APIException("Local AI chatbot returned an empty response.");
        }

        Object error = body.get("error");
        if (error != null && !error.toString().isBlank()) {
            throw new APIException("Local AI chatbot error: " + error);
        }

        Object answer = body.get("answer");
        if (answer == null || answer.toString().isBlank()) {
            throw new APIException("Local AI chatbot returned a blank answer.");
        }

        return answer.toString().trim();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Not available" : value;
    }
}

package com.ecommerce.project.service;

import com.ecommerce.project.model.Product;
import com.ecommerce.project.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductKnowledgeServiceImpl implements ProductKnowledgeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductKnowledgeServiceImpl.class);

    private final RestTemplate restTemplate;
    private final ProductRepository productRepository;

    @Value("${ai.chat.local-url:http://localhost:8081}")
    private String localAiUrl;

    @Value("${ai.chat.product-indexing-enabled:true}")
    private boolean productIndexingEnabled;

    public ProductKnowledgeServiceImpl(RestTemplate restTemplate, ProductRepository productRepository) {
        this.restTemplate = restTemplate;
        this.productRepository = productRepository;
    }

    @Override
    public void indexProduct(Product product) {
        if (!productIndexingEnabled || product == null || product.getProductId() == null) {
            return;
        }

        try {
            removeProduct(product);

            Map<String, String> requestBody = Map.of(
                    "title", "Product #" + product.getProductId() + ": " + safe(product.getProductName()),
                    "text", buildProductDocument(product)
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    localAiUrl + "/doc/insert",
                    requestBody,
                    Map.class
            );

            Object ids = response.getBody() == null ? null : response.getBody().get("ids");
            if (ids instanceof List<?> idList && !idList.isEmpty()) {
                product.setAiDocumentIds(idList.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.joining(",")));
                productRepository.save(product);
            }
        } catch (Exception e) {
            LOGGER.warn("Product {} was saved, but could not be indexed in local AI chatbot: {}",
                    product.getProductId(), e.getMessage());
        }
    }

    @Override
    public void removeProduct(Product product) {
        if (!productIndexingEnabled || product == null || product.getAiDocumentIds() == null || product.getAiDocumentIds().isBlank()) {
            return;
        }

        Arrays.stream(product.getAiDocumentIds().split(","))
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .forEach(this::deleteDocument);

        product.setAiDocumentIds(null);
    }

    @Override
    public int reindexProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return 0;
        }

        products.forEach(this::indexProduct);
        return products.size();
    }

    private void deleteDocument(String documentId) {
        try {
            restTemplate.delete(localAiUrl + "/doc/delete/" + documentId);
        } catch (Exception e) {
            LOGGER.warn("Could not delete local AI document {}: {}", documentId, e.getMessage());
        }
    }

    private String buildProductDocument(Product product) {
        String category = product.getCategory() == null ? "Uncategorized" : product.getCategory().getCategoryName();
        String seller = product.getUser() == null ? "Unknown seller" : product.getUser().getUserName();

        return String.join("\n",
                "This is a product from the e-commerce catalog.",
                "Product ID: " + product.getProductId(),
                "Name: " + safe(product.getProductName()),
                "Category: " + safe(category),
                "Description: " + safe(product.getDescription()),
                "Original price: " + product.getPrice(),
                "Discount percentage: " + product.getDiscount(),
                "Final selling price: " + product.getSpecialPrice(),
                "Available stock quantity: " + product.getQuantity(),
                "Seller: " + safe(seller),
                "When customers ask about this product, use this catalog information and do not invent unavailable details."
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Not available" : value;
    }
}

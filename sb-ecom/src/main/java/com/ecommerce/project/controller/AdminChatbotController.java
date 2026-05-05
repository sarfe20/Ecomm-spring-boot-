package com.ecommerce.project.controller;

import com.ecommerce.project.model.Product;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.service.ProductKnowledgeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/chatbot")
public class AdminChatbotController {
    private final ProductRepository productRepository;
    private final ProductKnowledgeService productKnowledgeService;

    public AdminChatbotController(ProductRepository productRepository,
                                  ProductKnowledgeService productKnowledgeService) {
        this.productRepository = productRepository;
        this.productKnowledgeService = productKnowledgeService;
    }

    @PostMapping("/reindex-products")
    public ResponseEntity<Map<String, Object>> reindexProducts() {
        List<Product> products = productRepository.findAll();
        int indexedProducts = productKnowledgeService.reindexProducts(products);

        return new ResponseEntity<>(
                Map.of(
                        "message", "Product catalog sent to the local AI chatbot.",
                        "indexedProducts", indexedProducts
                ),
                HttpStatus.OK
        );
    }
}

package com.ecommerce.project.service;

import com.ecommerce.project.model.Product;

import java.util.List;

public interface ProductKnowledgeService {
    void indexProduct(Product product);

    void removeProduct(Product product);

    int reindexProducts(List<Product> products);
}

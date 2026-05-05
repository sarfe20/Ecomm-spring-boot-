package com.ecommerce.project.service;

import java.util.Optional;
import java.util.List;

import com.ecommerce.project.payload.ExternalProductResultDTO;

public interface ScraperService {
    Optional<Double> scrapePrice(String productUrl);

    ScrapedProduct scrapeProduct(String productUrl);

    List<ExternalProductResultDTO> searchExternalProducts(String keyword);

    record ScrapedProduct(String productName, String description, Double price, String imageUrl) {
    }
}

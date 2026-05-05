package com.ecommerce.project.service;

import com.ecommerce.project.model.Platform;
import com.ecommerce.project.model.Price;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.repositories.PriceRepository;
import org.springframework.stereotype.Service;

@Service
public class FallbackPriceServiceImpl implements FallbackPriceService {
    private final PriceRepository priceRepository;

    public FallbackPriceServiceImpl(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Override
    public Double getFallbackPrice(Product product, Platform platform) {
        return priceRepository.findTopByProductAndPlatformOrderByDateDescIdDesc(product, platform)
                .map(Price::getPrice)
                .orElseGet(() -> mockPriceFromProduct(product, platform));
    }

    private Double mockPriceFromProduct(Product product, Platform platform) {
        double basePrice = product.getSpecialPrice() > 0 ? product.getSpecialPrice() : product.getPrice();
        String platformName = platform.getName() == null ? "" : platform.getName().toLowerCase();

        if (platformName.contains("amazon")) {
            return round(basePrice * 0.98);
        }
        if (platformName.contains("flipkart")) {
            return round(basePrice * 0.96);
        }
        return round(basePrice);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

package com.ecommerce.project.service;

import com.ecommerce.project.model.Platform;
import com.ecommerce.project.model.Product;

public interface FallbackPriceService {
    Double getFallbackPrice(Product product, Platform platform);
}

package com.ecommerce.project.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceScheduler {
    private final PriceService priceService;

    public PriceScheduler(PriceService priceService) {
        this.priceService = priceService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void updateTrackedPricesDaily() {
        priceService.refreshTrackedPrices();
    }
}

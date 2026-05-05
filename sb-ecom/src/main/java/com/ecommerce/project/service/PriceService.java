package com.ecommerce.project.service;

import com.ecommerce.project.payload.PriceAnalysisDTO;
import com.ecommerce.project.payload.PriceDTO;
import com.ecommerce.project.payload.PriceRequestDTO;

import java.util.List;

public interface PriceService {
    PriceDTO addPriceFromUrl(Long productId, PriceRequestDTO requestDTO);

    PriceDTO savePriceSnapshot(Long productId, String platformName, String productUrl);

    List<PriceDTO> getPrices(Long productId);

    PriceDTO getLowestPrice(Long productId);

    PriceDTO getHighestPrice(Long productId);

    List<PriceDTO> getPriceHistory(Long productId);

    PriceAnalysisDTO getPriceAnalysis(Long productId);

    void refreshTrackedPrices();
}

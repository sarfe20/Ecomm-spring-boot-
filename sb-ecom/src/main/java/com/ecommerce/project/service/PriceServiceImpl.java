package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.model.Platform;
import com.ecommerce.project.model.Price;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.SourceType;
import com.ecommerce.project.payload.PriceAnalysisDTO;
import com.ecommerce.project.payload.PriceDTO;
import com.ecommerce.project.payload.PriceRequestDTO;
import com.ecommerce.project.repositories.PlatformRepository;
import com.ecommerce.project.repositories.PriceRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PriceServiceImpl implements PriceService {
    private final PriceRepository priceRepository;
    private final PlatformRepository platformRepository;
    private final ProductRepository productRepository;
    private final ScraperService scraperService;
    private final FallbackPriceService fallbackPriceService;

    public PriceServiceImpl(PriceRepository priceRepository,
                            PlatformRepository platformRepository,
                            ProductRepository productRepository,
                            ScraperService scraperService,
                            FallbackPriceService fallbackPriceService) {
        this.priceRepository = priceRepository;
        this.platformRepository = platformRepository;
        this.productRepository = productRepository;
        this.scraperService = scraperService;
        this.fallbackPriceService = fallbackPriceService;
    }

    @Override
    public PriceDTO addPriceFromUrl(Long productId, PriceRequestDTO requestDTO) {
        String platformName = requestDTO.getPlatformName();
        if (platformName == null || platformName.isBlank()) {
            platformName = detectPlatformName(requestDTO.getProductUrl());
        }

        if (requestDTO.getPrice() != null) {
            if (requestDTO.getPrice() <= 0) {
                throw new APIException("Price must be greater than zero.");
            }

            Product product = getProduct(productId);
            Platform platform = getOrCreatePlatform(platformName);
            LocalDate date = requestDTO.getDate() == null ? LocalDate.now() : requestDTO.getDate();
            return savePrice(product, platform, requestDTO.getPrice(), SourceType.STORED, requestDTO.getProductUrl(), date);
        }

        if (requestDTO.getProductUrl() == null || requestDTO.getProductUrl().isBlank()) {
            throw new APIException("Enter a marketplace URL or a manual price.");
        }

        return savePriceSnapshot(productId, platformName, requestDTO.getProductUrl());
    }

    @Override
    public PriceDTO savePriceSnapshot(Long productId, String platformName, String productUrl) {
        Product product = getProduct(productId);
        Platform platform = getOrCreatePlatform(platformName);

        return scraperService.scrapePrice(productUrl)
                .map(price -> savePrice(product, platform, price, SourceType.SCRAPED, productUrl))
                .orElseGet(() -> savePrice(
                        product,
                        platform,
                        fallbackPriceService.getFallbackPrice(product, platform),
                        SourceType.MOCK,
                        productUrl
                ));
    }

    @Override
    public List<PriceDTO> getPrices(Long productId) {
        ensureAtLeastOnePrice(productId);
        return priceRepository.findByProductProductIdOrderByDateAscIdAsc(productId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                this::priceKey,
                                price -> price,
                                (first, duplicate) -> first,
                                LinkedHashMap::new
                        ),
                        pricesByKey -> List.copyOf(pricesByKey.values())
                ))
                ;
    }

    @Override
    public PriceDTO getLowestPrice(Long productId) {
        ensureAtLeastOnePrice(productId);
        return priceRepository.findFirstByProductProductIdOrderByPriceAsc(productId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Price", "productId", productId));
    }

    @Override
    public PriceDTO getHighestPrice(Long productId) {
        ensureAtLeastOnePrice(productId);
        return priceRepository.findFirstByProductProductIdOrderByPriceDesc(productId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Price", "productId", productId));
    }

    @Override
    public List<PriceDTO> getPriceHistory(Long productId) {
        return getPrices(productId);
    }

    @Override
    public PriceAnalysisDTO getPriceAnalysis(Long productId) {
        List<PriceDTO> prices = getPrices(productId);
        double lowest = prices.stream().mapToDouble(PriceDTO::getPrice).min().orElse(0);
        double highest = prices.stream().mapToDouble(PriceDTO::getPrice).max().orElse(0);
        double average = prices.stream().mapToDouble(PriceDTO::getPrice).average().orElse(0);

        List<LocalDate> bestBuyDates = prices.stream()
                .filter(price -> Double.compare(price.getPrice(), lowest) == 0)
                .map(PriceDTO::getDate)
                .distinct()
                .sorted()
                .toList();

        String suggestion = "Best time to buy: " + bestBuyDates
                + " when the price touched " + round(lowest) + ".";

        return new PriceAnalysisDTO(round(lowest), round(highest), round(average), bestBuyDates, suggestion);
    }

    @Override
    public void refreshTrackedPrices() {
        Map<String, Price> trackedPrices = new LinkedHashMap<>();
        priceRepository.findBySourceUrlIsNotNullAndSourceUrlNot("").stream()
                .sorted(Comparator.comparing(Price::getDate).thenComparing(Price::getId).reversed())
                .forEach(price -> trackedPrices.putIfAbsent(trackingKey(price), price));

        trackedPrices.values().forEach(price -> {
            try {
                savePriceSnapshot(
                        price.getProduct().getProductId(),
                        price.getPlatform().getName(),
                        price.getSourceUrl()
                );
            } catch (RuntimeException ignored) {
                // Scheduler must never interrupt the application because a marketplace blocks scraping.
            }
        });
    }

    private synchronized void ensureAtLeastOnePrice(Long productId) {
        Product product = getProduct(productId);
        if (!priceRepository.findByProductProductIdOrderByDateAscIdAsc(productId).isEmpty()) {
            return;
        }

        Platform platform = getOrCreatePlatform("Stored");
        savePrice(product, platform, fallbackPriceService.getFallbackPrice(product, platform), SourceType.STORED, null);
    }

    private PriceDTO savePrice(Product product, Platform platform, Double value, SourceType sourceType, String sourceUrl) {
        return savePrice(product, platform, value, sourceType, sourceUrl, LocalDate.now());
    }

    private PriceDTO savePrice(Product product, Platform platform, Double value, SourceType sourceType, String sourceUrl, LocalDate date) {
        Price price = new Price();
        price.setProduct(product);
        price.setPlatform(platform);
        price.setPrice(value);
        price.setDate(date);
        price.setSourceType(sourceType);
        price.setSourceUrl(sourceUrl);
        return toDTO(priceRepository.save(price));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
    }

    private Platform getOrCreatePlatform(String name) {
        String platformName = name == null || name.isBlank() ? "Unknown" : name.trim();
        return platformRepository.findByNameIgnoreCase(platformName)
                .orElseGet(() -> {
                    Platform platform = new Platform();
                    platform.setName(platformName);
                    return platformRepository.save(platform);
                });
    }

    private String detectPlatformName(String productUrl) {
        String url = productUrl == null ? "" : productUrl.toLowerCase();
        if (url.contains("amazon")) {
            return "Amazon";
        }
        if (url.contains("flipkart")) {
            return "Flipkart";
        }
        return "Marketplace";
    }

    private String trackingKey(Price price) {
        return price.getProduct().getProductId() + "|" + price.getPlatform().getId() + "|" + price.getSourceUrl();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String priceKey(PriceDTO price) {
        return price.getProductId() + "|"
                + price.getPlatformId() + "|"
                + price.getPrice() + "|"
                + price.getDate() + "|"
                + price.getSourceType() + "|"
                + (price.getSourceUrl() == null ? "" : price.getSourceUrl());
    }

    private PriceDTO toDTO(Price price) {
        return new PriceDTO(
                price.getId(),
                price.getProduct().getProductId(),
                price.getPlatform().getId(),
                price.getPlatform().getName(),
                price.getPrice(),
                price.getDate(),
                price.getSourceType(),
                price.getSourceUrl()
        );
    }
}

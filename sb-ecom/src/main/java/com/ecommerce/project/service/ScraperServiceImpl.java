package com.ecommerce.project.service;

import com.ecommerce.project.payload.ExternalProductResultDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScraperServiceImpl implements ScraperService {
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+[\\d,]*\\.?\\d*)");
    private static final List<String> PRICE_SELECTORS = List.of(
            ".a-price .a-offscreen",
            "#priceblock_ourprice",
            "#priceblock_dealprice",
            "._30jeq3",
            ".Nx9bqj",
            ".CxhGGd",
            "[itemprop=price]",
            "meta[property=product:price:amount]"
    );
    private static final List<String> TITLE_SELECTORS = List.of(
            "#productTitle",
            "span.B_NuCI",
            ".VU-ZEz",
            "meta[property=og:title]",
            "title"
    );
    private static final List<String> DESCRIPTION_SELECTORS = List.of(
            "#feature-bullets",
            "#productDescription",
            ".yN\\+eNk",
            ".Xbd0Sd",
            "meta[name=description]",
            "meta[property=og:description]"
    );
    private static final List<String> IMAGE_SELECTORS = List.of(
            "meta[property=og:image]",
            "meta[name=twitter:image]",
            "#landingImage",
            "#imgTagWrapperId img",
            "#main-image-container img",
            "img[data-a-dynamic-image]",
            "img._396cs4",
            "img.DByuf4",
            "img._53J4C-",
            "img"
    );
    private static final List<String> IMAGE_ATTRIBUTES = List.of(
            "content",
            "data-old-hires",
            "data-a-dynamic-image",
            "srcset",
            "data-src",
            "data-original",
            "src"
    );
    private static final List<MarketplaceSearchConfig> MARKETPLACE_SEARCHES = List.of(
            new MarketplaceSearchConfig(
                    "Amazon",
                    "https://www.amazon.in/s?k=%s",
                    "https://www.amazon.in",
                    "[data-component-type=s-search-result]",
                    List.of("h2 span", ".a-size-medium", ".a-size-base-plus"),
                    "h2 a",
                    List.of("img.s-image"),
                    List.of(".a-price .a-offscreen"),
                    6
            ),
            new MarketplaceSearchConfig(
                    "Flipkart",
                    "https://www.flipkart.com/search?q=%s",
                    "https://www.flipkart.com",
                    "a.CGtC98, a.rPDeLR, a.VJA3rP, div[data-id]",
                    List.of(".KzDlHZ", ".wjcEIp", ".syl9yP", "a[title]", "img"),
                    "a.CGtC98, a.rPDeLR, a.VJA3rP, a[href]",
                    List.of("img"),
                    List.of(".Nx9bqj", "._30jeq3"),
                    6
            ),
            new MarketplaceSearchConfig(
                    "Croma",
                    "https://www.croma.com/searchB?q=%s%%3Arelevance&text=%s",
                    "https://www.croma.com",
                    ".product-item, .cp-product, li.product-item",
                    List.of(".product-title", ".product-name", "h3", "h2", "a[title]"),
                    "a[href]",
                    List.of("img"),
                    List.of(".amount", ".new-price", ".price", "[data-testid=price]"),
                    4
            ),
            new MarketplaceSearchConfig(
                    "Reliance Digital",
                    "https://www.reliancedigital.in/search?q=%s",
                    "https://www.reliancedigital.in",
                    ".sp__product, .product-card, .grid, li",
                    List.of(".sp__name", ".product-card-title", ".product-name", "h3", "a[title]"),
                    "a[href]",
                    List.of("img"),
                    List.of(".sp__price", ".price", ".TextWeb__Text-sc-1cyx778-0"),
                    4
            ),
            new MarketplaceSearchConfig(
                    "Vijay Sales",
                    "https://www.vijaysales.com/search/%s",
                    "https://www.vijaysales.com",
                    ".product-listing, .productbox, .product-card, li",
                    List.of(".product-name", ".ProductName", "h2", "h3", "a[title]"),
                    "a[href]",
                    List.of("img"),
                    List.of(".price", ".Price", ".mrp", ".offer-price"),
                    4
            ),
            new MarketplaceSearchConfig(
                    "Tata CLiQ",
                    "https://www.tatacliq.com/search/?searchCategory=all&text=%s",
                    "https://www.tatacliq.com",
                    ".ProductModule__base, .product-card, .Grid__element, li",
                    List.of(".ProductDescription__boldText", ".ProductDescription__description", "h3", "a[title]"),
                    "a[href]",
                    List.of("img"),
                    List.of(".ProductDescription__priceHolder", ".price", ".ProductDescription__price"),
                    4
            )
    );

    @Override
    public Optional<Double> scrapePrice(String productUrl) {
        try {
            Document document = Jsoup.connect(productUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36")
                    .timeout(10000)
                    .get();

            for (String selector : PRICE_SELECTORS) {
                Element element = document.selectFirst(selector);
                Optional<Double> parsedPrice = parsePrice(element);
                if (parsedPrice.isPresent()) {
                    return parsedPrice;
                }
            }
        } catch (IOException | IllegalArgumentException ex) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    @Override
    public ScrapedProduct scrapeProduct(String productUrl) {
        try {
            Document document = Jsoup.connect(productUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36")
                    .referrer("https://www.google.com/")
                    .timeout(12000)
                    .get();

            String productName = firstValue(document, TITLE_SELECTORS).orElse("");
            String description = firstValue(document, DESCRIPTION_SELECTORS).orElse(productName);
            Double price = findPrice(document).orElse(null);
            String imageUrl = firstImageUrl(document).orElse("");

            return new ScrapedProduct(clean(productName), clean(description), price, imageUrl);
        } catch (IOException | IllegalArgumentException ex) {
            return new ScrapedProduct("", "", null, "");
        }
    }

    @Override
    public List<ExternalProductResultDTO> searchExternalProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        Map<String, ExternalProductResultDTO> resultsByUrl = new LinkedHashMap<>();
        for (MarketplaceSearchConfig config : MARKETPLACE_SEARCHES) {
            List<ExternalProductResultDTO> marketplaceResults = searchMarketplace(keyword, config);
            if (marketplaceResults.isEmpty()) {
                marketplaceResults = List.of(marketplaceFallback(keyword, config));
            }

            marketplaceResults.forEach(result -> resultsByUrl.putIfAbsent(result.getProductUrl(), result));
        }

        return resultsByUrl.values().stream()
                .sorted(Comparator
                        .comparing((ExternalProductResultDTO result) -> result.getPrice() == null)
                        .thenComparing(result -> result.getPrice() == null ? Double.MAX_VALUE : result.getPrice()))
                .limit(24)
                .toList();
    }

    private Optional<Double> parsePrice(Element element) {
        if (element == null) {
            return Optional.empty();
        }

        String value = element.hasAttr("content") ? element.attr("content") : element.text();
        Matcher matcher = PRICE_PATTERN.matcher(value);
        if (!matcher.find()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Double.parseDouble(matcher.group(1).replace(",", "")));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private Optional<Double> findPrice(Document document) {
        for (String selector : PRICE_SELECTORS) {
            Optional<Double> parsedPrice = parsePrice(document.selectFirst(selector));
            if (parsedPrice.isPresent()) {
                return parsedPrice;
            }
        }
        return Optional.empty();
    }

    private Optional<String> firstValue(Document document, List<String> selectors) {
        for (String selector : selectors) {
            Element element = document.selectFirst(selector);
            if (element == null) {
                continue;
            }

            String value = element.hasAttr("content") ? element.attr("content") : element.attr("src");
            if (value == null || value.isBlank()) {
                value = element.text();
            }
            if (value != null && !value.isBlank()) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    private Optional<String> firstImageUrl(Document document) {
        for (String selector : IMAGE_SELECTORS) {
            Element element = document.selectFirst(selector);
            if (element == null) {
                continue;
            }

            for (String attribute : IMAGE_ATTRIBUTES) {
                String value = element.attr(attribute);
                Optional<String> imageUrl = extractImageUrl(value, document.baseUri());
                if (imageUrl.isPresent()) {
                    return imageUrl;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> extractImageUrl(String value, String baseUri) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String trimmed = value.trim();
        if (trimmed.startsWith("{")) {
            Matcher matcher = Pattern.compile("\"(https?://[^\"]+)\"").matcher(trimmed);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        }

        if (trimmed.contains(",")) {
            trimmed = trimmed.split(",")[0].trim();
        }

        if (trimmed.contains(" ")) {
            trimmed = trimmed.split("\\s+")[0].trim();
        }

        return normalizeUrl(trimmed, baseUri);
    }

    private Optional<String> normalizeUrl(String value, String baseUri) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String url = value.trim();
        if (url.startsWith("//")) {
            return Optional.of("https:" + url);
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return Optional.of(url);
        }

        try {
            return Optional.of(URI.create(baseUri).resolve(url).toString());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\s+", " ").trim();
    }

    private List<ExternalProductResultDTO> searchMarketplace(String keyword, MarketplaceSearchConfig config) {
        String searchUrl = marketplaceSearchUrl(keyword, config);
        try {
            Document document = fetch(searchUrl);
            return document.select(config.itemSelector()).stream()
                    .map(item -> toExternalResult(item, config))
                    .filter(this::hasUsefulExternalResult)
                    .limit(config.limit())
                    .toList();
        } catch (IOException | IllegalArgumentException ex) {
            return List.of();
        }
    }

    private ExternalProductResultDTO marketplaceFallback(String keyword, MarketplaceSearchConfig config) {
        String cleanKeyword = clean(keyword);
        return new ExternalProductResultDTO(
                "Search \"" + cleanKeyword + "\" on " + config.source(),
                "",
                marketplaceSearchUrl(keyword, config),
                null,
                config.source()
        );
    }

    private String marketplaceSearchUrl(String keyword, MarketplaceSearchConfig config) {
        String encodedKeyword = encode(keyword);
        return config.searchUrlTemplate().formatted(encodedKeyword, encodedKeyword);
    }

    private ExternalProductResultDTO toExternalResult(Element item, MarketplaceSearchConfig config) {
        String title = clean(firstText(item, config.titleSelectors()));
        if (title.isBlank()) {
            title = clean(item.attr("title"));
        }

        String productUrl = absoluteUrl(item, config.linkSelector(), "href", config.baseUrl());
        String imageUrl = firstImageUrl(item, config.imageSelectors()).orElse("");
        Double price = firstPrice(item, config.priceSelectors()).orElse(null);
        return new ExternalProductResultDTO(title, imageUrl, productUrl, price, config.source());
    }

    private Optional<Double> firstPrice(Element item, List<String> selectors) {
        for (String selector : selectors) {
            Optional<Double> price = parsePrice(item.selectFirst(selector));
            if (price.isPresent()) {
                return price;
            }
        }
        return Optional.empty();
    }

    private Optional<String> firstImageUrl(Element item, List<String> selectors) {
        for (String selector : selectors) {
            Element image = item.selectFirst(selector);
            if (image == null) {
                continue;
            }

            for (String attribute : IMAGE_ATTRIBUTES) {
                Optional<String> imageUrl = extractImageUrl(image.attr(attribute), item.baseUri());
                if (imageUrl.isPresent()) {
                    return imageUrl;
                }
            }
        }
        return Optional.empty();
    }

    private Document fetch(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .referrer("https://www.google.com/")
                .timeout(12000)
                .get();
    }

    private boolean hasUsefulExternalResult(ExternalProductResultDTO result) {
        return result.getTitle() != null && !result.getTitle().isBlank()
                && result.getProductUrl() != null && !result.getProductUrl().isBlank();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String firstText(Element root, List<String> selectors) {
        for (String selector : selectors) {
            Element element = root.selectFirst(selector);
            if (element == null) {
                continue;
            }

            String value = selector.equals("img") ? element.attr("alt") : element.text();
            if ((value == null || value.isBlank()) && element.hasAttr("title")) {
                value = element.attr("title");
            }
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String attr(Element root, String selector, String attr) {
        Element element = selector.isBlank() ? root : root.selectFirst(selector);
        return element == null ? "" : element.attr(attr);
    }

    private String absoluteUrl(Element root, String selector, String attr, String baseUrl) {
        String url = attr(root, selector, attr);
        if (url.isBlank()) {
            return "";
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return baseUrl + url;
    }

    private record MarketplaceSearchConfig(
            String source,
            String searchUrlTemplate,
            String baseUrl,
            String itemSelector,
            List<String> titleSelectors,
            String linkSelector,
            List<String> imageSelectors,
            List<String> priceSelectors,
            int limit
    ) {
    }
}

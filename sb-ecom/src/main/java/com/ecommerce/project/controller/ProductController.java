package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.PriceAnalysisDTO;
import com.ecommerce.project.payload.PriceDTO;
import com.ecommerce.project.payload.PriceRequestDTO;
import com.ecommerce.project.payload.ExternalProductResultDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductImportRequestDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.PriceService;
import com.ecommerce.project.service.ProductService;
import com.ecommerce.project.service.ScraperService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;
    private final PriceService priceService;
    private final ScraperService scraperService;

    public ProductController(ProductService productService, PriceService priceService, ScraperService scraperService) {
        this.productService = productService;
        this.priceService = priceService;
        this.scraperService = scraperService;
    }

    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO,
                                                 @PathVariable Long categoryId){
        ProductDTO savedProductDTO = productService.addProduct(categoryId, productDTO);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    @PostMapping("/seller/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProductSeller(@Valid @RequestBody ProductDTO productDTO,
                                                 @PathVariable Long categoryId){
        ProductDTO savedProductDTO = productService.addProduct(categoryId, productDTO);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    @PostMapping("/admin/products/import-preview")
    public ResponseEntity<ProductDTO> importProductPreview(@Valid @RequestBody ProductImportRequestDTO requestDTO) {
        return new ResponseEntity<>(productService.importProductPreview(requestDTO.getProductUrl()), HttpStatus.OK);
    }

    @PostMapping("/seller/products/import-preview")
    public ResponseEntity<ProductDTO> importProductPreviewSeller(@Valid @RequestBody ProductImportRequestDTO requestDTO) {
        return new ResponseEntity<>(productService.importProductPreview(requestDTO.getProductUrl()), HttpStatus.OK);
    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder, keyword, category);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }

    @GetMapping("/public/external-products/search")
    public ResponseEntity<List<ExternalProductResultDTO>> searchExternalProducts(
            @RequestParam(name = "keyword") String keyword
    ) {
        return new ResponseEntity<>(scraperService.searchExternalProducts(keyword), HttpStatus.OK);
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategory(@PathVariable Long categoryId,
                                                                 @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                 @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                 @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                 @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder){
        ProductResponse productResponse = productService.searchByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyword(@PathVariable String keyword,
                                                                @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder){
        ProductResponse productResponse = productService.searchProductByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO productDTO,
                                                    @PathVariable Long productId){
        ProductDTO updatedProductDTO = productService.updateProduct(productId, productDTO);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }

    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId){
        ProductDTO deletedProduct = productService.deleteProduct(productId);
        return new ResponseEntity<>(deletedProduct, HttpStatus.OK);
    }

    @PutMapping("/admin/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId,
                                                         @RequestParam("image")MultipartFile image) throws IOException {
        ProductDTO updatedProduct = productService.updateProductImage(productId, image);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @PutMapping("/admin/products/{productId}/image-url")
    public ResponseEntity<ProductDTO> updateProductImageUrl(@PathVariable Long productId,
                                                            @RequestBody Map<String, String> request) {
        ProductDTO updatedProduct = productService.updateProductImageUrl(productId, request.get("imageUrl"));
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }


    @GetMapping("/admin/products")
    public ResponseEntity<ProductResponse> getAllProductsForAdmin(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        ProductResponse productResponse = productService.getAllProductsForAdmin(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }


    @GetMapping("/seller/products")
    public ResponseEntity<ProductResponse> getAllProductsForSeller(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        ProductResponse productResponse = productService.getAllProductsForSeller(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }

    @PutMapping("/seller/products/{productId}")
    public ResponseEntity<ProductDTO> updateProductSeller(@Valid @RequestBody ProductDTO productDTO,
                                                    @PathVariable Long productId){
        ProductDTO updatedProductDTO = productService.updateProduct(productId, productDTO);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }

    @DeleteMapping("/seller/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProductSeller(@PathVariable Long productId){
        ProductDTO deletedProduct = productService.deleteProduct(productId);
        return new ResponseEntity<>(deletedProduct, HttpStatus.OK);
    }

    @PutMapping("/seller/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImageSeller(@PathVariable Long productId,
                                                         @RequestParam("image")MultipartFile image) throws IOException {
        ProductDTO updatedProduct = productService.updateProductImage(productId, image);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @PutMapping("/seller/products/{productId}/image-url")
    public ResponseEntity<ProductDTO> updateProductImageUrlSeller(@PathVariable Long productId,
                                                                  @RequestBody Map<String, String> request) {
        ProductDTO updatedProduct = productService.updateProductImageUrl(productId, request.get("imageUrl"));
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @PostMapping("/products/{productId}/prices")
    public ResponseEntity<PriceDTO> addProductPrice(@PathVariable Long productId,
                                                    @Valid @RequestBody PriceRequestDTO priceRequestDTO) {
        PriceDTO priceDTO = priceService.addPriceFromUrl(productId, priceRequestDTO);
        return new ResponseEntity<>(priceDTO, HttpStatus.CREATED);
    }

    @GetMapping("/products/{productId}/prices")
    public ResponseEntity<List<PriceDTO>> getProductPrices(@PathVariable Long productId) {
        return new ResponseEntity<>(priceService.getPrices(productId), HttpStatus.OK);
    }

    @GetMapping("/products/{productId}/prices/lowest")
    public ResponseEntity<PriceDTO> getLowestProductPrice(@PathVariable Long productId) {
        return new ResponseEntity<>(priceService.getLowestPrice(productId), HttpStatus.OK);
    }

    @GetMapping("/products/{productId}/prices/highest")
    public ResponseEntity<PriceDTO> getHighestProductPrice(@PathVariable Long productId) {
        return new ResponseEntity<>(priceService.getHighestPrice(productId), HttpStatus.OK);
    }

    @GetMapping("/products/{productId}/prices/history")
    public ResponseEntity<List<PriceDTO>> getProductPriceHistory(@PathVariable Long productId) {
        return new ResponseEntity<>(priceService.getPriceHistory(productId), HttpStatus.OK);
    }

    @GetMapping("/products/{productId}/prices/analysis")
    public ResponseEntity<PriceAnalysisDTO> getProductPriceAnalysis(@PathVariable Long productId) {
        return new ResponseEntity<>(priceService.getPriceAnalysis(productId), HttpStatus.OK);
    }
}

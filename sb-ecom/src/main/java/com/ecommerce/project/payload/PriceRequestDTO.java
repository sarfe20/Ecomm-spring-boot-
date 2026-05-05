package com.ecommerce.project.payload;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PriceRequestDTO {
    private String productUrl;
    private String platformName;
    private Double price;
    private LocalDate date;
}

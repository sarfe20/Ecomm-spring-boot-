package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalProductResultDTO {
    private String title;
    private String imageUrl;
    private String productUrl;
    private Double price;
    private String source;
}

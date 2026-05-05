package com.ecommerce.project.payload;

import com.ecommerce.project.model.SourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceDTO {
    private Long id;
    private Long productId;
    private Long platformId;
    private String platformName;
    private Double price;
    private LocalDate date;
    private SourceType sourceType;
    private String sourceUrl;
}

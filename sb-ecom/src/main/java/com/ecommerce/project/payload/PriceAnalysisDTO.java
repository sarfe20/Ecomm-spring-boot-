package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceAnalysisDTO {
    private Double lowestPrice;
    private Double highestPrice;
    private Double averagePrice;
    private List<LocalDate> bestBuyDates;
    private String suggestion;
}

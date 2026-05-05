package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponse {
    private String id;
    private Long amount;
    private Long amountDue;
    private Long amountPaid;
    private String currency;
    private String receipt;
    private String status;
    private String key;
}

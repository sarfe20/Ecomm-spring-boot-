package com.ecommerce.project.service;

import com.ecommerce.project.payload.RazorpayOrderResponse;
import com.ecommerce.project.payload.RazorpayPaymentDto;

public interface RazorpayService {

    RazorpayOrderResponse createOrder(RazorpayPaymentDto razorpayPaymentDto);

    void verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);
}

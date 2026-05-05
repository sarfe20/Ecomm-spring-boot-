package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.payload.RazorpayOrderResponse;
import com.ecommerce.project.payload.RazorpayPaymentDto;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class RazorpayServiceImpl implements RazorpayService {

    private final String razorpayKeyId;
    private final String razorpayKeySecret;

    public RazorpayServiceImpl(
            @Value("${razorpay.key.id}") String razorpayKeyId,
            @Value("${razorpay.key.secret}") String razorpayKeySecret) {
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
    }

    @Override
    public RazorpayOrderResponse createOrder(RazorpayPaymentDto razorpayPaymentDto) {
        if (razorpayPaymentDto.getAmount() == null || razorpayPaymentDto.getAmount() <= 0) {
            throw new APIException("Payment amount must be greater than zero");
        }

        try {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", razorpayPaymentDto.getAmount());
            orderRequest.put("currency", razorpayPaymentDto.getCurrency() == null ? "INR" : razorpayPaymentDto.getCurrency());
            orderRequest.put("receipt", "order_" + UUID.randomUUID());
            orderRequest.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(orderRequest);
            JSONObject orderJson = order.toJson();

            return new RazorpayOrderResponse(
                    orderJson.getString("id"),
                    orderJson.getLong("amount"),
                    orderJson.getLong("amount_due"),
                    orderJson.getLong("amount_paid"),
                    orderJson.getString("currency"),
                    orderJson.optString("receipt", null),
                    orderJson.getString("status"),
                    razorpayKeyId
            );
        } catch (RazorpayException exception) {
            throw new APIException("Unable to create Razorpay order: " + exception.getMessage());
        }
    }

    @Override
    public void verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
            throw new APIException("Missing Razorpay payment verification details");
        }

        try {
            JSONObject verificationPayload = new JSONObject();
            verificationPayload.put("razorpay_order_id", razorpayOrderId);
            verificationPayload.put("razorpay_payment_id", razorpayPaymentId);
            verificationPayload.put("razorpay_signature", razorpaySignature);

            if (!Utils.verifyPaymentSignature(verificationPayload, razorpayKeySecret)) {
                throw new APIException("Razorpay payment verification failed");
            }
        } catch (RazorpayException exception) {
            throw new APIException("Unable to verify Razorpay payment: " + exception.getMessage());
        }
    }
}

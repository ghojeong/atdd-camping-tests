package com.camping.tests.utils;

public final class PaymentRequestHelper {
    private PaymentRequestHelper() {}

    public static String createPaymentRequestBody(int unitPrice) {
        return String.format(
                """
                        {
                            "items": [
                                {
                                    "productId": 1,
                                    "quantity": 1,
                                    "unitPrice": %d,
                                    "productName": "Test Product"
                                }
                            ],
                            "paymentMethod": "CARD"
                        }
                        """, unitPrice
        );
    }

    public static String createConfirmRequestBody(String paymentKey, String orderId, int amount) {
        return String.format(
                """
                        {
                            "paymentKey": "%s",
                            "orderId": "%s",
                            "amount": %d,
                            "items": [
                                {
                                    "productId": 1,
                                    "quantity": 1,
                                    "unitPrice": %d,
                                    "productName": "Test Product"
                                }
                            ]
                        }
                        """, paymentKey, orderId, amount, amount
        );
    }
}

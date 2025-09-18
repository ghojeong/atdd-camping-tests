package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.restassured.response.Response;

public class CommonSteps {
    private static Response currentResponse;
    private static String sharedPaymentKey;
    private static String sharedOrderId;

    public static void setCurrentResponse(Response response) {
        currentResponse = response;
    }

    public static String getSharedPaymentKey() {
        return sharedPaymentKey;
    }

    public static void setSharedPaymentKey(String paymentKey) {
        sharedPaymentKey = paymentKey;
    }

    public static String getSharedOrderId() {
        return sharedOrderId;
    }

    public static void setSharedOrderId(String orderId) {
        sharedOrderId = orderId;
    }

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        if (currentResponse != null) {
            currentResponse.then().statusCode(200);
        }
    }
}

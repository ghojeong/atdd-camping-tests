package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.restassured.response.Response;

public class CommonSteps {
    private static Response currentResponse;

    public static void setCurrentResponse(Response response) {
        currentResponse = response;
    }

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        if (currentResponse != null) {
            currentResponse.then().statusCode(200);
        }
    }
}

package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KioskSmokeSteps {
    private Response response;

    public KioskSmokeSteps() {
        final String KIOSK_BASE_URL = "KIOSK_BASE_URL";
        final String DEFAULT_URL = "http://localhost:18081";
        RestAssured.baseURI = System.getProperty(
                KIOSK_BASE_URL,
                System.getenv().getOrDefault(KIOSK_BASE_URL, DEFAULT_URL)
        );
    }

    @When("Kiosk 서비스의 헬스 체크를 요청한다")
    public void kioskHealthCheck() {
        response = given().when().get("/");
        assertNotNull(response, "응답이 null입니다");
    }

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        response.then().statusCode(200);
    }
}

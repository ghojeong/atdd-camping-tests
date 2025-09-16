package com.camping.tests.steps;

import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReservationSmokeSteps {
    private Response response;

    public ReservationSmokeSteps() {
        final String RESERVATION_BASE_URL = "RESERVATION_BASE_URL";
        final String DEFAULT_URL = "http://localhost:18083";
        RestAssured.baseURI = System.getProperty(
                RESERVATION_BASE_URL,
                System.getenv().getOrDefault(RESERVATION_BASE_URL, DEFAULT_URL)
        );
    }

    @When("Reservation 서비스의 헬스 체크를 요청한다")
    public void reservationHealthCheck() {
        response = given().when().get("/");
        assertNotNull(response, "응답이 null입니다");
        CommonSteps.setCurrentResponse(response);
    }
}

package com.camping.tests.steps;

import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AdminSmokeSteps {

    public AdminSmokeSteps() {
        final String ADMIN_BASE_URL = "ADMIN_BASE_URL";
        final String DEFAULT_URL = "http://localhost:18082";
        RestAssured.baseURI = System.getProperty(
                ADMIN_BASE_URL,
                System.getenv().getOrDefault(ADMIN_BASE_URL, DEFAULT_URL)
        );
    }

    @When("Admin 서비스의 헬스 체크를 요청한다")
    public void adminHealthCheck() {
        Response response = given().when().get("/login");
        assertNotNull(response, "응답이 null입니다");
        CommonSteps.setCurrentResponse(response);
    }
}

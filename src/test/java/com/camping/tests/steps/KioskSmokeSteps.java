package com.camping.tests.steps;

import com.camping.tests.config.TestConfiguration;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KioskSmokeSteps {
    private final TestConfiguration testConfiguration;

    public KioskSmokeSteps(TestConfiguration testConfiguration) {
        this.testConfiguration = testConfiguration;
        RestAssured.baseURI = testConfiguration.getKioskBaseUrl();
    }

    @When("Kiosk 서비스의 헬스 체크를 요청한다")
    public void kioskHealthCheck() {
        Response response = given().when().get("/");
        assertNotNull(response, "응답이 null입니다");
        CommonSteps.setCurrentResponse(response);
    }
}

package com.camping.tests.utils;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public final class AuthenticationHelper {
    private AuthenticationHelper() {}

    public static String performLogin(String adminBaseUrl) {
        Response loginResponse = given()
                .baseUri(adminBaseUrl)
                .contentType("application/json")
                .body("{\"username\":\"admin\",\"password\":\"password\"}")
                .when()
                .post("/auth/login");

        if (loginResponse.statusCode() != 200) {
            return null;
        }

        return extractAuthToken(loginResponse);
    }

    public static String extractAuthToken(Response response) {
        String token = response.getCookie("JSESSIONID");
        if (token != null) {
            return token;
        }

        token = response.getHeader("Authorization");
        if (token != null) {
            return token;
        }

        return response.jsonPath().getString("token");
    }

    public static RequestSpecification addAuthToRequest(RequestSpecification requestSpec, String authToken) {
        if (authToken == null) {
            return requestSpec;
        }

        if (authToken.startsWith("Bearer ")) {
            return requestSpec.header("Authorization", authToken);
        }

        return requestSpec.cookie("JSESSIONID", authToken);
    }
}

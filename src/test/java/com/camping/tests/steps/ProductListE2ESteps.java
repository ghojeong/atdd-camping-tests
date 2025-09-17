package com.camping.tests.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static com.camping.tests.utils.AuthenticationHelper.addAuthToRequest;
import static com.camping.tests.utils.AuthenticationHelper.extractAuthToken;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProductListE2ESteps {
    private final String adminBaseUrl;
    private final String kioskBaseUrl;
    private Response productListResponse;
    private String authToken;

    public ProductListE2ESteps() {
        this.adminBaseUrl = System.getProperty(
                "ADMIN_BASE_URL",
                System.getenv().getOrDefault("ADMIN_BASE_URL", "http://localhost:18082")
        );
        this.kioskBaseUrl = System.getProperty(
                "KIOSK_BASE_URL",
                System.getenv().getOrDefault("KIOSK_BASE_URL", "http://localhost:18081")
        );
    }

    @Given("Admin에서 로그인을 한다")
    public void adminLogin() {
        Response loginResponse = given()
                .baseUri(adminBaseUrl)
                .contentType("application/json")
                .body("{\"username\":\"admin\",\"password\":\"password\"}")
                .when()
                .post("/auth/login");

        assertNotNull(loginResponse, "로그인 응답이 null입니다");

        if (loginResponse.statusCode() != 200) {
            return;
        }
        authToken = extractAuthToken(loginResponse);
    }

    private RequestSpecification createAuthenticatedRequest(String baseUrl) {
        RequestSpecification requestSpec = given().baseUri(baseUrl);
        return addAuthToRequest(requestSpec, authToken);
    }

    @When("Kiosk에서 상품 목록을 요청한다")
    public void requestProductList() {
        productListResponse = createAuthenticatedRequest(kioskBaseUrl)
                .when()
                .get("/api/products");

        assertNotNull(productListResponse, "상품 목록 응답이 null입니다");
    }

    @Then("상품 목록을 성공적으로 받는다")
    public void verifyProductListSuccess() {
        CommonSteps.setCurrentResponse(productListResponse);
        productListResponse.then().statusCode(200);
    }

    @Then("상품 목록에는 최소 {int}개의 상품이 있다")
    public void verifyProductCount(int minCount) {
        productListResponse.then()
                .body("size()", greaterThanOrEqualTo(minCount))
                .body("[0]", hasKey("id"))
                .body("[0]", hasKey("name"));
    }
}

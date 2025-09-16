package com.camping.tests.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PaymentE2ESteps {
    private final String adminBaseUrl;
    private final String kioskBaseUrl;
    private Response paymentResponse;
    private Response confirmResponse;
    private String authToken;
    private String paymentKey;
    private String orderId;

    public PaymentE2ESteps() {
        this.adminBaseUrl = System.getProperty(
                "ADMIN_BASE_URL",
                System.getenv().getOrDefault("ADMIN_BASE_URL", "http://localhost:18082")
        );
        this.kioskBaseUrl = System.getProperty(
                "KIOSK_BASE_URL",
                System.getenv().getOrDefault("KIOSK_BASE_URL", "http://localhost:18081")
        );
    }

    private void ensureAuthenticated() {
        if (authToken != null) {
            return;
        }
        Response loginResponse = given()
                .baseUri(adminBaseUrl)
                .contentType("application/json")
                .body("{\"username\":\"admin\",\"password\":\"password\"}")
                .when()
                .post("/auth/login");
        if (loginResponse.statusCode() != 200) {
            return;
        }
        authToken = loginResponse.getCookie("JSESSIONID");
        if (authToken == null) {
            authToken = loginResponse.getHeader("Authorization");
        }
        if (authToken == null) {
            authToken = loginResponse.jsonPath().getString("token");
        }
    }

    @Given("Kiosk에서 상품을 장바구니에 추가한다")
    public void addProductToCart() {
        // 간단한 상품 추가 시나리오 (실제 구현에 따라 조정 필요)
        // 이 단계는 실제로는 상품 선택/장바구니 추가가 필요하지만,
        // 결제 테스트 목적으로 간소화
    }

    @When("Kiosk에서 결제를 요청한다")
    public void requestPayment() {
        ensureAuthenticated();

        var requestSpec = given()
                .baseUri(kioskBaseUrl)
                .contentType("application/json");

        // 인증 정보 추가
        if (authToken != null) {
            if (authToken.startsWith("Bearer ")) {
                requestSpec = requestSpec.header("Authorization", authToken);
            } else {
                requestSpec = requestSpec.cookie("JSESSIONID", authToken);
            }
        }

        String paymentRequestBody = """
                {
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 1,
                            "unitPrice": 10000,
                            "productName": "Test Product"
                        }
                    ],
                    "paymentMethod": "CARD"
                }
                """;

        paymentResponse = requestSpec
                .body(paymentRequestBody)
                .when()
                .post("/api/payments");

        assertNotNull(paymentResponse, "결제 응답이 null입니다");

        // 성공한 경우 결제 키와 주문 ID 저장
        if (paymentResponse.statusCode() == 200) {
            paymentKey = paymentResponse.jsonPath().getString("paymentKey");
            orderId = paymentResponse.jsonPath().getString("orderId");
        }
    }

    @When("Kiosk에서 실패하도록 결제를 요청한다")
    public void requestFailedPayment() {
        ensureAuthenticated();

        var requestSpec = given()
                .baseUri(kioskBaseUrl)
                .contentType("application/json");

        // 인증 정보 추가
        if (authToken != null) {
            if (authToken.startsWith("Bearer ")) {
                requestSpec = requestSpec.header("Authorization", authToken);
            } else {
                requestSpec = requestSpec.cookie("JSESSIONID", authToken);
            }
        }

        // 실패를 유도하는 특별한 금액(99999) 사용
        String paymentRequestBody = """
                {
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 1,
                            "unitPrice": 99999,
                            "productName": "Test Product"
                        }
                    ],
                    "paymentMethod": "CARD"
                }
                """;

        paymentResponse = requestSpec
                .body(paymentRequestBody)
                .when()
                .post("/api/payments");

        assertNotNull(paymentResponse, "결제 응답이 null입니다");
    }

    @When("Kiosk에서 결제 확인을 요청한다")
    public void requestPaymentConfirm() {
        ensureAuthenticated();

        var requestSpec = given()
                .baseUri(kioskBaseUrl)
                .contentType("application/json");

        // 인증 정보 추가
        if (authToken != null) {
            if (authToken.startsWith("Bearer ")) {
                requestSpec = requestSpec.header("Authorization", authToken);
            } else {
                requestSpec = requestSpec.cookie("JSESSIONID", authToken);
            }
        }

        String confirmRequestBody = String.format(
                """
                        {
                            "paymentKey": "%s",
                            "orderId": "%s",
                            "amount": 10000,
                            "items": [
                                {
                                    "productId": 1,
                                    "quantity": 1,
                                    "unitPrice": 10000,
                                    "productName": "Test Product"
                                }
                            ]
                        }
                        """, paymentKey, orderId
        );

        confirmResponse = requestSpec
                .body(confirmRequestBody)
                .when()
                .post("/api/payments/confirm");

        assertNotNull(confirmResponse, "결제 확인 응답이 null입니다");
    }

    @When("Kiosk에서 실패하도록 결제 확인을 요청한다")
    public void requestFailedPaymentConfirm() {
        ensureAuthenticated();

        var requestSpec = given()
                .baseUri(kioskBaseUrl)
                .contentType("application/json");

        // 인증 정보 추가
        if (authToken != null) {
            if (authToken.startsWith("Bearer ")) {
                requestSpec = requestSpec.header("Authorization", authToken);
            } else {
                requestSpec = requestSpec.cookie("JSESSIONID", authToken);
            }
        }

        String confirmRequestBody = String.format(
                """
                        {
                            "paymentKey": "%s",
                            "orderId": "%s",
                            "amount": 99999,
                            "items": [
                                {
                                    "productId": 1,
                                    "quantity": 1,
                                    "unitPrice": 99999,
                                    "productName": "Test Product"
                                }
                            ]
                        }
                        """, paymentKey, orderId
        );

        confirmResponse = requestSpec
                .body(confirmRequestBody)
                .when()
                .post("/api/payments/confirm");

        assertNotNull(confirmResponse, "결제 확인 응답이 null입니다");
    }

    @Then("결제가 성공적으로 처리된다")
    public void verifyPaymentSuccess() {
        CommonSteps.setCurrentResponse(paymentResponse);
        paymentResponse.then().statusCode(200);
    }

    @Then("결제 키와 주문 ID가 응답에 포함된다")
    public void verifyPaymentResponseData() {
        paymentResponse.then()
                .body("success", equalTo(true))
                .body("paymentKey", notNullValue())
                .body("orderId", notNullValue());

        // amount는 success가 true일 때만 0보다 커야 함
        boolean success = paymentResponse.jsonPath().getBoolean("success");
        if (success) {
            paymentResponse.then().body("amount", greaterThan(0));
        }
    }

    @Then("결제가 실패 응답을 받는다")
    public void verifyPaymentFailure() {
        CommonSteps.setCurrentResponse(paymentResponse);
        // 컨트롤러가 항상 200을 반환하므로 success 필드로 실패 여부 확인
        paymentResponse.then()
                .statusCode(200)
                .body("success", equalTo(false));
    }

    @Then("오류 메시지가 응답에 포함된다")
    public void verifyErrorMessage() {
        // 결제 확인 실패 시나리오에서는 confirmResponse를 확인해야 함
        if (confirmResponse != null) {
            confirmResponse.then()
                    .body("success", equalTo(false))
                    .body("message", notNullValue());
        } else {
            // 결제 생성 실패 시나리오에서는 paymentResponse를 확인
            paymentResponse.then()
                    .body("success", equalTo(false))
                    .body("message", notNullValue());
        }
    }

    @Then("결제 확인이 성공적으로 처리된다")
    public void verifyConfirmSuccess() {
        CommonSteps.setCurrentResponse(confirmResponse);
        confirmResponse.then().statusCode(200);
    }

    @Then("승인 정보가 응답에 포함된다")
    public void verifyConfirmResponseData() {
        confirmResponse.then()
                .body("success", equalTo(true))
                .body("transactionId", notNullValue());

        // paidAmount는 success가 true일 때만 0보다 커야 함
        boolean success = confirmResponse.jsonPath().getBoolean("success");
        if (success) {
            confirmResponse.then().body("paidAmount", greaterThan(0));
        }
    }

    @Then("결제 확인이 실패 응답을 받는다")
    public void verifyConfirmFailure() {
        CommonSteps.setCurrentResponse(confirmResponse);
        // 컨트롤러가 항상 200을 반환하므로 success 필드로 실패 여부 확인
        confirmResponse.then()
                .statusCode(200)
                .body("success", equalTo(false));
    }
}

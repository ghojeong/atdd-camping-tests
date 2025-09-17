package com.camping.tests.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static com.camping.tests.utils.AuthenticationHelper.addAuthToRequest;
import static com.camping.tests.utils.AuthenticationHelper.performLogin;
import static com.camping.tests.utils.PaymentRequestHelper.createConfirmRequestBody;
import static com.camping.tests.utils.PaymentRequestHelper.createPaymentRequestBody;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
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
        authToken = performLogin(adminBaseUrl);
    }

    private RequestSpecification createAuthenticatedRequest(String baseUrl) {
        ensureAuthenticated();
        RequestSpecification requestSpec = given()
                .baseUri(baseUrl)
                .contentType("application/json");
        return addAuthToRequest(requestSpec, authToken);
    }


    private void extractPaymentInfo() {
        if (paymentResponse.statusCode() == 200) {
            paymentKey = paymentResponse.jsonPath().getString("paymentKey");
            orderId = paymentResponse.jsonPath().getString("orderId");
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
        String paymentRequestBody = createPaymentRequestBody(10000);

        paymentResponse = createAuthenticatedRequest(kioskBaseUrl)
                .body(paymentRequestBody)
                .when()
                .post("/api/payments");

        assertNotNull(paymentResponse, "결제 응답이 null입니다");
        extractPaymentInfo();
    }

    @When("Kiosk에서 실패하도록 결제를 요청한다")
    public void requestFailedPayment() {
        String paymentRequestBody = createPaymentRequestBody(99999);

        paymentResponse = createAuthenticatedRequest(kioskBaseUrl)
                .body(paymentRequestBody)
                .when()
                .post("/api/payments");

        assertNotNull(paymentResponse, "결제 응답이 null입니다");
    }

    @When("Kiosk에서 결제 확인을 요청한다")
    public void requestPaymentConfirm() {
        String confirmRequestBody = createConfirmRequestBody(paymentKey, orderId, 10000);

        confirmResponse = createAuthenticatedRequest(kioskBaseUrl)
                .body(confirmRequestBody)
                .when()
                .post("/api/payments/confirm");

        assertNotNull(confirmResponse, "결제 확인 응답이 null입니다");
    }

    @When("Kiosk에서 실패하도록 결제 확인을 요청한다")
    public void requestFailedPaymentConfirm() {
        String confirmRequestBody = createConfirmRequestBody(paymentKey, orderId, 99999);

        confirmResponse = createAuthenticatedRequest(kioskBaseUrl)
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

        boolean success = paymentResponse.jsonPath().getBoolean("success");
        if (!success) {
            return;
        }
        paymentResponse.then().body("amount", greaterThan(0));
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
        Response responseToVerify = confirmResponse != null ? confirmResponse : paymentResponse;
        responseToVerify.then()
                .body("success", equalTo(false))
                .body("message", notNullValue());
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

        boolean success = confirmResponse.jsonPath().getBoolean("success");
        if (!success) {
            return;
        }
        confirmResponse.then().body("paidAmount", greaterThan(0));
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

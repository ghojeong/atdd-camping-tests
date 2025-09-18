# Authentication Setup Guide

## 인증 방법 개요

캠핑장 예약 시스템의 인증은 Admin 서비스를 통해 중앙 집중화되어 있습니다. 모든 Kiosk 요청은 Admin에서 발급받은 인증 정보가 필요합니다.

## 인증 흐름

1. **Admin 로그인**: `/auth/login` 엔드포인트로 로그인
2. **토큰/쿠키 추출**: 응답에서 인증 정보 추출
3. **인증된 요청**: 후속 요청에 인증 정보 포함

## 인증 방법별 설정

### 1. 쿠키 기반 인증 (JSESSIONID)

#### 로그인 요청

```http
POST http://localhost:18082/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

#### 응답 예시

```http
HTTP/1.1 200 OK
Set-Cookie: JSESSIONID=ABC123DEF456; Path=/; HttpOnly
Content-Type: application/json

{
  "status": "success",
  "message": "로그인 성공"
}
```

#### 인증된 요청

```http
GET http://localhost:18081/api/products
Cookie: JSESSIONID=ABC123DEF456
```

### 2. Bearer 토큰 인증

#### 로그인 응답 (토큰 포함)

```json
{
  "status": "success",
  "token": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "로그인 성공"
}
```

#### 인증된 요청

```http
GET http://localhost:18081/api/products
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. Authorization 헤더

#### 로그인 응답 (헤더 포함)

```http
HTTP/1.1 200 OK
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

## RestAssured를 사용한 인증 구현

### AuthenticationHelper 클래스

```java
public final class AuthenticationHelper {

    /**
     * Admin 서비스에 로그인하여 인증 토큰을 발급받습니다.
     *
     * @param adminBaseUrl Admin 서비스 기본 URL
     * @return 인증 토큰 (실패 시 null)
     */
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

    /**
     * 응답에서 인증 토큰을 추출합니다.
     * 우선순위: 쿠키 > Authorization 헤더 > JSON 바디
     */
    public static String extractAuthToken(Response response) {
        // 1. 쿠키에서 JSESSIONID 추출 시도
        String token = response.getCookie("JSESSIONID");
        if (token != null) {
            return token;
        }

        // 2. Authorization 헤더 추출 시도
        token = response.getHeader("Authorization");
        if (token != null) {
            return token;
        }

        // 3. JSON 바디에서 token 필드 추출 시도
        return response.jsonPath().getString("token");
    }

    /**
     * RequestSpecification에 인증 정보를 추가합니다.
     */
    public static RequestSpecification addAuthToRequest(
            RequestSpecification requestSpec, String authToken) {
        if (authToken == null) {
            return requestSpec;
        }

        // Bearer 토큰인 경우 Authorization 헤더 사용
        if (authToken.startsWith("Bearer ")) {
            return requestSpec.header("Authorization", authToken);
        }

        // 그 외의 경우 JSESSIONID 쿠키 사용
        return requestSpec.cookie("JSESSIONID", authToken);
    }
}
```

### 테스트 스텝에서 인증 사용

```java
public class PaymentE2ESteps {
    private String authToken;
    private final TestConfiguration testConfiguration;

    /**
     * 인증이 필요한 경우 자동으로 로그인을 수행합니다.
     */
    private void ensureAuthenticated() {
        if (authToken != null) {
            return; // 이미 인증됨
        }
        authToken = performLogin(testConfiguration.getAdminBaseUrl());
    }

    /**
     * 인증된 요청 스펙을 생성합니다.
     */
    private RequestSpecification createAuthenticatedRequest(String baseUrl) {
        ensureAuthenticated();
        RequestSpecification requestSpec = given()
                .baseUri(baseUrl)
                .contentType("application/json");
        return addAuthToRequest(requestSpec, authToken);
    }

    @When("Kiosk에서 상품 목록을 요청한다")
    public void requestProductList() {
        Response response = createAuthenticatedRequest(testConfiguration.getKioskBaseUrl())
                .when()
                .get("/api/products");

        // 응답 처리...
    }
}
```

## 환경변수 키 목록

### 서비스 URL 설정

- `ADMIN_BASE_URL`: Admin 서비스 URL (기본값: http://localhost:18082)
- `KIOSK_BASE_URL`: Kiosk 서비스 URL (기본값: http://localhost:18081)
- `RESERVATION_BASE_URL`: Reservation 서비스 URL (기본값: http://localhost:18083)

### 인증 정보 설정 (필요시)

- `ADMIN_USERNAME`: Admin 로그인 사용자명 (기본값: admin)
- `ADMIN_PASSWORD`: Admin 로그인 비밀번호 (기본값: password)

### 환경변수 설정 방법

#### Bash/Zsh

```bash
export ADMIN_BASE_URL=http://localhost:18082
export KIOSK_BASE_URL=http://localhost:18081
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=password
```

#### Docker Compose

```yaml
services:
  test-runner:
    environment:
      - ADMIN_BASE_URL=http://admin:8080
      - KIOSK_BASE_URL=http://kiosk:8080
      - ADMIN_USERNAME=admin
      - ADMIN_PASSWORD=password
```

#### Gradle 테스트 실행

```bash
# 환경변수로 설정
./gradlew test

# 시스템 프로퍼티로 설정
./gradlew test -DADMIN_BASE_URL=http://localhost:18082 -DKIOSK_BASE_URL=http://localhost:18081
```

## 인증 실패 처리

### 로그인 실패 처리

```java

@Given("Admin에서 로그인을 한다")
public void performAdminLogin() {
    authToken = performLogin(testConfiguration.getAdminBaseUrl());

    if (authToken == null) {
        throw new IllegalStateException(
            "Admin 로그인에 실패했습니다. URL: " + testConfiguration.getAdminBaseUrl()
        );
    }
}
```

### 인증 만료 처리

```java
private RequestSpecification createAuthenticatedRequest(String baseUrl) {
    ensureAuthenticated();

    RequestSpecification requestSpec = given()
            .baseUri(baseUrl)
            .contentType("application/json");

    RequestSpecification authSpec = addAuthToRequest(requestSpec, authToken);

    // 401 응답 시 재인증 시도
    return authSpec.filter((requestSpec1, responseSpec, ctx) -> {
        Response response = ctx.next(requestSpec1, responseSpec);
        if (response.statusCode() == 401) {
            // 토큰 만료로 판단, 재인증 시도
            authToken = null;
            ensureAuthenticated();
            requestSpec1 = addAuthToRequest(
                given().baseUri(baseUrl).contentType("application/json"),
                authToken
            );
            response = ctx.next(requestSpec1, responseSpec);
        }
        return response;
    });
}
```

## 쿠키 vs 토큰 선택 가이드

### 쿠키 기반 인증 (JSESSIONID)

**장점:**

- 브라우저와 유사한 동작
- 자동 만료 관리
- CSRF 보호 가능

**단점:**

- 상태 유지 필요
- 분산 환경에서 복잡

**사용 권장 상황:**

- 세션 기반 애플리케이션
- 브라우저 테스트와 일관성 필요

### 토큰 기반 인증 (JWT/Bearer)

**장점:**

- 무상태(Stateless)
- 분산 환경 친화적
- 만료 시간 제어 용이

**단점:**

- 토큰 관리 복잡
- 크기가 큰 경우 성능 영향

**사용 권장 상황:**

- API 중심 애플리케이션
- 마이크로서비스 환경
- 모바일/SPA 클라이언트

## 디버깅 가이드

### 인증 문제 진단

#### 1. **로그인 응답 확인**

```java
Response loginResponse = performLoginWithLogging(adminBaseUrl);
System.out.println("Status: " + loginResponse.statusCode());
System.out.println("Headers: " + loginResponse.headers());
System.out.println("Cookies: " + loginResponse.cookies());
System.out.println("Body: " + loginResponse.body().asString());
```

#### 2. **인증 토큰 검증**

```java
String token = extractAuthToken(loginResponse);
System.out.println("Extracted token: " + token);
```

#### 3. **인증된 요청 확인**

```java
Response authResponse = createAuthenticatedRequest(kioskBaseUrl)
        .log().all() // 요청 로그 출력
        .when()
        .get("/api/products")
        .then()
        .log().all() // 응답 로그 출력
        .extract().response();
```

### 일반적인 인증 오류

| 오류                        | 원인           | 해결방법           |
|---------------------------|--------------|----------------|
| 401 Unauthorized          | 인증 정보 없음/잘못됨 | 로그인 재시도, 토큰 확인 |
| 403 Forbidden             | 권한 부족        | 사용자 권한 확인      |
| 404 Not Found             | 잘못된 엔드포인트    | URL 확인         |
| 500 Internal Server Error | 서버 오류        | 서버 로그 확인       |

이 가이드를 참고하여 테스트에서 일관되고 안정적인 인증을 구현하시기 바랍니다.

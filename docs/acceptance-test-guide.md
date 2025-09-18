# Acceptance Test Guide

## 서비스 개요

캠핑장 예약 시스템은 3개의 마이크로서비스로 구성된 분산 환경입니다:

### 서비스 구성

- **admin**: 관리자 기능, 인증, 상품 관리
- **kiosk**: 고객 대면 인터페이스, 예약 및 결제 처리
- **reservation**: 예약 관리 및 처리
- **payments-mock**: 외부 결제 서비스 모킹 (WireMock)

### 도메인 및 핵심 흐름

1. **인증 흐름**: Admin 로그인 → 세션/토큰 발급 → 인증된 요청
2. **상품 조회 흐름**: Kiosk → Admin → DB → 상품 목록 반환
3. **결제 흐름**: Kiosk → Payments → 승인/거절 → 결제 완료/실패
4. **예약 흐름**: Kiosk → Reservation → DB → 예약 생성/확인

### 시스템 제약사항

- 모든 Kiosk 요청은 Admin 인증이 필요
- 결제는 외부 시스템(WireMock)을 통해 처리
- 데이터베이스는 3개 서비스가 공유 (atdd-db)
- 서비스 간 통신은 Docker 네트워크 (atdd-net) 사용

## 엔드포인트 요약

### Admin Service (포트: 18082)

| 메서드  | 경로            | 주요 필드              | 사전조건  |
|------|---------------|--------------------|-------|
| POST | /auth/login   | username, password | 없음    |
| GET  | /api/products | -                  | 인증 필요 |

**로그인 요청 예시:**

```json
{
  "username": "admin",
  "password": "password"
}
```

### Kiosk Service (포트: 18081)

| 메서드  | 경로                    | 주요 필드                       | 사전조건            |
|------|-----------------------|-----------------------------|-----------------|
| GET  | /api/products         | -                           | 인증 필요           |
| POST | /api/payments         | items[], paymentMethod      | 인증 필요           |
| POST | /api/payments/confirm | paymentKey, orderId, amount | 인증 필요, 사전 결제 요청 |

**결제 요청 예시:**

```json
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
```

**결제 확인 요청 예시:**

```json
{
  "paymentKey": "pay_12345",
  "orderId": "ord_12345",
  "amount": 10000,
  "items": [...]
}
```

### Payments Mock (포트: 18090)

| 메서드  | 경로                   | 주요 필드                       | 응답 조건                     |
|------|----------------------|-----------------------------|---------------------------|
| POST | /v1/payments         | paymentKey, orderId, amount | amount=99999이면 실패, 그 외 성공 |
| POST | /v1/payments/confirm | paymentKey, orderId, amount | amount=99999이면 실패, 그 외 성공 |

## 기존 Gherkin 예시

### 좋은 예시 (권장)

```gherkin
# language: ko
기능: E2E 테스트 - 상품 목록 조회

  @e2e
  시나리오: Admin 인증 후 Kiosk에서 상품 목록 조회
    만약 Admin에서 로그인을 한다
    그리고 Kiosk에서 상품 목록을 요청한다
    그러면 상품 목록이 정상적으로 조회된다
    그리고 상품 목록에는 최소 1개 이상의 상품이 포함된다
```

**좋은 점:**

- 비즈니스 의도가 명확함
- 기술적 세부사항 숨김
- 검증 조건이 구체적

### 나쁜 예시 (지양)

```gherkin
# 지양할 패턴
시나리오: HTTP 200 응답 확인
만약 "http://localhost:18081/api/products"에 GET 요청을 보낸다
그러면 응답 코드는 200이다
그리고 응답 헤더 "Content-Type"은 "application/json"이다
```

**문제점:**

- URL 하드코딩으로 환경 의존성 발생
- 비즈니스 가치보다 기술적 세부사항에 집중
- 의도가 불분명함

## 베이스 URL 외부화 가이드

### 환경변수 키 목록

- `ADMIN_BASE_URL`: Admin 서비스 URL (기본값: http://localhost:18082)
- `KIOSK_BASE_URL`: Kiosk 서비스 URL (기본값: http://localhost:18081)
- `RESERVATION_BASE_URL`: Reservation 서비스 URL (기본값: http://localhost:18083)

### 설정 방법

```bash
# 로컬 테스트 실행 시
export ADMIN_BASE_URL=http://localhost:18082
export KIOSK_BASE_URL=http://localhost:18081
./gradlew test

# 또는 시스템 프로퍼티로 설정
./gradlew test -DADMIN_BASE_URL=http://localhost:18082
```

### TestConfiguration 활용

```java
// TestConfiguration에서 자동으로 환경변수/시스템 프로퍼티 조회
public class TestConfiguration {
    private final String adminBaseUrl;

    public TestConfiguration() {
        this.adminBaseUrl = getConfigValue("ADMIN_BASE_URL", "http://localhost:18082");
    }

    private String getConfigValue(String key, String defaultValue) {
        return System.getProperty(key, System.getenv().getOrDefault(key, defaultValue));
    }
}
```

## 테스트 작성 가이드라인

### 1. 시나리오 명명 규칙

- **기능**: 비즈니스 기능 중심으로 명명
- **시나리오**: "상황 + 행동 + 기대결과" 패턴 사용

### 2. 태그 활용

- `@smoke`: 기본 연결성 확인 테스트
- `@e2e`: 검증된 종단간 테스트
- `@ai-candidate`: AI 생성 후 검토 대기 중인 테스트

### 3. 단계 작성 원칙

- **Given**: 사전 조건 설정 (데이터, 인증 상태)
- **When**: 실제 행동 (API 호출, 사용자 행동)
- **Then**: 검증 (비즈니스 결과, 데이터 상태)

### 4. 검증 수준

- **최소 검증**: HTTP 상태 코드, 필수 필드 존재 여부
- **비즈니스 검증**: 도메인 규칙, 데이터 일관성
- **경계 검증**: 제한값, 예외 상황

## 경계/예외 시나리오 패턴

### 1. 재고 관련

```gherkin
시나리오: 재고 0인 상품 예약 시도
만약 Admin에서 로그인을 한다
그리고 재고가 0인 상품을 선택한다
그리고 예약을 시도한다
그러면 재고 부족 오류가 발생한다
그리고 오류 메시지에는 "재고가 부족합니다"가 포함된다
```

### 2. 결제 실패

```gherkin
시나리오: 결제 서비스 오류로 인한 결제 실패
만약 Admin에서 로그인을 한다
그리고 Kiosk에서 상품을 장바구니에 추가한다
그리고 Kiosk에서 실패하도록 결제를 요청한다
그러면 결제가 실패 응답을 받는다
그리고 오류 메시지가 응답에 포함된다
```

### 3. 유효성 검증

```gherkin
시나리오: 필수 필드 누락으로 인한 요청 실패
만약 Admin에서 로그인을 한다
그리고 필수 필드가 누락된 결제 요청을 보낸다
그러면 유효성 검증 오류가 발생한다
그리고 누락된 필드 정보가 오류 메시지에 포함된다
```

### 4. 중복 요청 처리

```gherkin
시나리오: 동일한 주문 ID로 중복 결제 시도
만약 Admin에서 로그인을 한다
그리고 Kiosk에서 결제를 완료한다
그리고 동일한 주문 ID로 다시 결제를 시도한다
그러면 중복 주문 오류가 발생한다
그리고 "이미 처리된 주문입니다" 메시지가 표시된다
```

## AI 생성 테스트 품질 기준

### 의도-검증 일치성

- When 단계의 행동이 Then 단계의 검증과 논리적으로 연결되어야 함
- 비즈니스 의도가 기술적 구현 세부사항보다 우선되어야 함

### 환경 독립성

- 하드코딩된 URL, 포트 번호 사용 금지
- 환경변수나 설정을 통한 외부화 필수

### 데이터 격리

- 테스트 간 데이터 간섭 방지
- 고유 식별자 사용 (UUID, 타임스탬프 등)
- 테스트 후 정리 또는 격리된 데이터 사용

### 재현 가능성

- 외부 의존성 최소화
- 모킹된 서비스 활용
- 일관된 테스트 데이터

이 가이드를 참고하여 AI 도구로 테스트를 생성할 때 품질과 일관성을 확보하시기 바랍니다.

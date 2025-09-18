# Data Seed Guide

## 데이터 시드 개요

캠핑장 예약 시스템의 데이터 시드는 일관되고 재현 가능한 테스트 환경을 제공하기 위해 설계되었습니다. 초기 데이터는 `infra/db/init.sql`을 통해 자동으로 로드되며, 테스트 격리와 안정성을 위한 다양한 전략을 제공합니다.

## 초기 데이터 구조

### 1. 상품 데이터 (Products)

```sql
-- 대여용 상품
랜턴 (20개, 30,000원)
코펠 세트 (15개, 20,000원)
의자 (25개, 15,000원)
테이블 (10개, 25,000원)
버너 (12개, 18,000원)
취사도구 세트 (30개, 12,000원)

-- 판매용 상품
장작팩 (50개, 10,000원)
생수 2L (100개, 2,000원)
라면 세트 (80개, 4,000원)
스낵팩 (60개, 3,000원)
휴지 (70개, 2,500원)
아이스팩 (90개, 1,500원)
```

### 2. 캠핑장 사이트 (Campsites)

```sql
-- A구역: 대형 사이트 (A-1 ~ A-20, 6인용)
-- B구역: 소형 사이트 (B-1 ~ B-15, 6인용)
-- 각 사이트마다 다양한 특징 (전기, 화장실 인근, 계곡 전망 등)
```

### 3. 예약 데이터 (Reservations)

```sql
-- 현재/미래 예약
홍길동 (오늘~내일, A-1)
김철수 (내일~모레, A-2)

-- 과거 예약 (최근 1개월)
이영희, 박민수, 최수정, 정하늘, 오세훈, 유지민, 선우진, 배수아, 고다빈, 한도윤

-- 확정 예약
홍길동 (다음주), 김철수 (2주 후), 이영희 (3주 후)
```

### 4. 매출 기록 (Sales Records)

```sql
-- 최근 판매 기록
장작팩 3개 (어제), 장작팩 1개 (오늘)
생수 5개 (일주일 전), 라면 세트 2개 (2주 전)
스낵팩 10개 (한달 전)
```

### 5. 대여 기록 (Rental Records)

```sql
-- 현재 대여 중
코펠 세트 2개 (이영희), 테이블 3개 (최수정), 취사도구 세트 4개 (오세훈)

-- 반납 완료
의자 1개 (박민수), 버너 1개 (정하늘)

-- 워크인 대여 (예약 없음)
코펠 세트 1개 (3일 전)
```

## 데이터 격리 전략

### 1. 고유 식별자 사용

```java
// UUID를 활용한 고유한 주문 ID 생성
public static String generateUniqueOrderId() {
    return "TEST_" + UUID.randomUUID().toString().substring(0, 8);
}

// 타임스탬프를 활용한 고유한 확인 코드 생성
public static String generateUniqueConfirmationCode() {
    return "CONF_" + System.currentTimeMillis();
}
```

### 2. 네임스페이스 활용

```java
// 테스트별 네임스페이스 구분
public class TestDataHelper {
    private static final String TEST_PREFIX = "ATDD_";

    public static String createTestCustomerName(String testCase) {
        return TEST_PREFIX + testCase + "_" + System.currentTimeMillis();
    }

    public static String createTestPhoneNumber(String testCase) {
        return "010-TEST-" + testCase.hashCode() % 10000;
    }
}
```

### 3. 시간 기반 격리

```java
// 미래 날짜 사용으로 기존 데이터와 충돌 방지
public static LocalDate generateFutureDate(int daysFromNow) {
    return LocalDate.now().plusDays(30 + daysFromNow); // 30일 이후부터 사용
}

// 테스트별 시간 슬롯 할당
public static LocalDate generateTestSlotDate(String testCase) {
    int hash = Math.abs(testCase.hashCode());
    int dayOffset = 100 + (hash % 265); // 100일 ~ 365일 범위
    return LocalDate.now().plusDays(dayOffset);
}
```

## 사후 정리 원칙

### 1. 테스트 데이터 정리

```java

@AfterEach
public void cleanupTestData() {
    // 테스트 중 생성된 데이터 정리
    cleanupReservationsByPrefix("ATDD_");
    cleanupSalesRecordsByTimeRange(testStartTime, System.currentTimeMillis());
}

private void cleanupReservationsByPrefix(String prefix) {
    String cleanupSql = """
        DELETE FROM reservations
        WHERE customer_name LIKE ?
        AND created_at >= ?
        """;
    // 정리 로직 구현
}
```

### 2. 트랜잭션 롤백 활용

```java

@Transactional
@Rollback
@Test
public void testReservationCreation() {
    // 테스트 로직
    // 테스트 종료 시 자동 롤백
}
```

### 3. 재사용 안전성 확보

```java
// 멱등성을 보장하는 데이터 생성
public static void ensureTestData() {
    createProductIfNotExists("테스트상품_" + testCase, 100, 10000);
    createCampsiteIfNotExists("TEST-" + testCase, "테스트용 사이트");
}

private static void createProductIfNotExists(String name, int stock, int price) {
    String sql = """
        INSERT INTO products (name, stock_quantity, price, product_type)
        VALUES (?, ?, ?, 'SALE')
        ON DUPLICATE KEY UPDATE stock_quantity = VALUES(stock_quantity)
        """;
    // 구현
}
```

## 환경별 시드 전략

### 1. 로컬 개발 환경

```sql
-- 풍부한 테스트 데이터
-- 다양한 시나리오 커버
-- 개발자 친화적 데이터
```

### 2. CI/CD 환경

```sql
-- 최소한의 필수 데이터
-- 빠른 초기화
-- 가벼운 데이터셋
```

### 3. 스테이징 환경

```sql
-- 프로덕션 유사 데이터
-- 성능 테스트 가능한 볼륨
-- 실제 시나리오 반영
```

## 시드 규칙 (Seeding Rules)

### 1. 기본 시드 데이터

- **시점**: 인프라 컨테이너 시작 시 (`init.sql`)
- **범위**: 모든 테스트에서 공통으로 필요한 기본 데이터
- **특징**: 읽기 전용, 수정하지 않음

### 2. 테스트별 시드 데이터

- **시점**: 각 테스트 시작 전 (`@BeforeEach`)
- **범위**: 특정 테스트에서만 필요한 데이터
- **특징**: 테스트 완료 후 정리

### 3. 시나리오별 시드 데이터

- **시점**: 시나리오 실행 중 (`Given` 단계)
- **범위**: 특정 시나리오 조건을 위한 데이터
- **특징**: 시나리오 완료 후 격리

## 데이터 시드 유틸리티

### TestDataFactory 클래스

```java
public class TestDataFactory {

    public static class ProductData {
        public static final String LANTERN = "랜턴";
        public static final String WOOD_PACK = "장작팩";
        public static final String CHAIR = "의자";
        public static final int DEFAULT_STOCK = 20;
    }

    public static class CampsiteData {
        public static final String SITE_A1 = "A-1";
        public static final String SITE_A2 = "A-2";
        public static final String SITE_B1 = "B-1";
    }

    public static class CustomerData {
        public static final String DEFAULT_NAME = "홍길동";
        public static final String DEFAULT_PHONE = "010-1111-2222";

        public static String generateTestName(String testCase) {
            return "TEST_" + testCase + "_" + System.currentTimeMillis();
        }
    }
}
```

### DatabaseSeeder 클래스

```java
public class DatabaseSeeder {

    public static void seedBasicProducts() {
        // 기본 상품 데이터 시드
    }

    public static void seedTestReservation(String customerName, String siteNumber, LocalDate date) {
        // 테스트용 예약 데이터 시드
    }

    public static void seedLowStockProduct(String productName, int stockQuantity) {
        // 재고 부족 테스트용 상품 시드
    }

    public static void cleanupTestData(String testNamespace) {
        // 테스트 데이터 정리
    }
}
```

## 데이터 검증 가이드

### 1. 시드 데이터 검증

```java

@Test
public void verifySeedData() {
    // 기본 상품 데이터 존재 확인
    assertThat(getProductCount()).isGreaterThan(10);

    // 기본 캠핑장 사이트 존재 확인
    assertThat(getCampsiteCount()).isEqualTo(35);

    // 예약 데이터 일관성 확인
    assertThat(getActiveReservationCount()).isGreaterThan(0);
}
```

### 2. 격리 검증

```java

@Test
public void verifyDataIsolation() {
    String testId = "ISOLATION_TEST_" + UUID.randomUUID();

    // 테스트 데이터 생성
    createTestReservation(testId);

    // 다른 테스트 데이터와 격리 확인
    assertThat(getReservationByCustomerName(testId)).isNotNull();
    assertThat(getReservationByCustomerName("OTHER_TEST")).isNull();
}
```

### 3. 정리 검증

```java

@Test
public void verifyDataCleanup() {
    String testId = "CLEANUP_TEST_" + UUID.randomUUID();

    // 테스트 데이터 생성
    createTestReservation(testId);
    assertThat(getReservationByCustomerName(testId)).isNotNull();

    // 데이터 정리
    cleanupTestData(testId);

    // 정리 확인
    assertThat(getReservationByCustomerName(testId)).isNull();
}
```

## 성능 고려사항

### 1. 시드 데이터 최적화

- 인덱스가 있는 컬럼 우선 활용
- 대량 INSERT 시 배치 처리
- 불필요한 외래키 제약 조건 최소화

### 2. 정리 작업 최적화

- 범위 삭제 우선 (시간 범위, ID 범위)
- 트랜잭션 크기 제한
- 비동기 정리 고려

### 3. 메모리 효율성

- 테스트 간 데이터 재사용
- 공통 참조 데이터 캐싱
- 대용량 데이터는 지연 로딩

이 가이드를 참고하여 안정적이고 격리된 테스트 환경을 구축하시기 바랍니다.

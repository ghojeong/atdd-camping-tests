# [미션 소개] 캠핑장 통합 시스템

## 미션 배경

레거시 기반의 다중 서비스 환경에서 인수 테스트를 중심으로 동작하는 품질 보호막을 단계적으로 구축한다. 테스트 허브는 atdd-tests이며, 서비스 코드는 필요 시 개인 브랜치에서 수정 후 pull 하여 진행한다. 구현 방식은 자율이되, 각 단계의 목표와 완료 기준(DoD)은 명확히 따른다.

## 시스템 현황

* 애플리케이션: admin, kiosk, reservation
* 결제 연동: payment는 대체 가능(mock/stub)
* 데이터베이스: 별도 docker compose로 기동(종류 자율)
* 테스트 허브: atdd-tests (Cucumber/RestAssured/JUnit 기반)

## 학습 목표

* 분산 서비스 환경에서 재현 가능한 테스트 인프라 설계
* Docker Compose 분리(인프라 vs 앱)와 기동 안정화(헬스체크/대기)
* 외부 의존성 대체(WireMock 또는 Testcontainers)와 계약 기반 연동
* 단계적 테스트 레이어 구축: smoke → e2e → 인수(ATDD)
* 브랜치 전략과 자동화로 반복 가능성/신뢰성 강화

## 미션 구성

### Step 1: Kiosk만 띄워서 Smoke 테스트 확보하기

#### 목표

* kiosk 애플리케이션을 컨테이너로 기동하고, atdd-tests에서 200 응답 스모크 테스트 1건을 확보한다. DB 없이도 기동 가능하도록 구성(필요 시 인프라 compose로 DB 포함은 선택).

#### 핵심 활동

* dockerfiles/Dockerfile-kiosk 준비, atdd-tests/docker/docker-compose.yml에 kiosk 단일 서비스 정의.
* 원커맨드(up → test → down) 또는 Gradle 태스크로 기동·종료 표준화.
* 베이스 URL 외부화(KIOSK_BASE_URL), 짧은 폴링/리트라이로 준비 대기 후 /actuator/health 등 200 확인.

#### 기대 결과

* 로컬에서 kiosk 컨테이너 접근 가능, 스모크 테스트 1건이 안정적으로 200 통과.
* 포트 충돌/플래키 없음, 실행 방법이 간단히 문서화.

### Step 2: 다중 앱 기동 확장 + 단일 DB 전환 + E2E 테스트

#### 목표

* admin, reservation을 추가해 3개 앱을 컨테이너로 기동하고, 초기에는 각 앱 전용 DB로 스모크를 통과시킨 뒤 공용 인프라 DB로 전환한다. kiosk → admin → DB 흐름의 E2E 1건을 구축한다.

#### 핵심 활동

* dockerfiles/Dockerfile-admin, dockerfiles/Dockerfile-reservation 추가. 앱 compose에 서비스/전용 DB 구성.
* atdd-tests에 각 서비스 스모크 테스트(200) 추가, 베이스 URL 외부화.
* 인프라 compose(DB) 기동, 외부 네트워크(atdd-net) 사용, 각 앱을 공용 DB(atdd-db)로 전환.
* kiosk의 상품 목록 등 간단 시나리오로 E2E 테스트 작성.

#### 기대 결과

* 세 서비스 스모크 테스트 모두 통과, 공용 DB 전환 후에도 재통과.
* kiosk → admin → DB E2E 테스트 통과 및 재현 가능한 실행 문서.

### Step 3: 결제 연동 모킹(WireMock) + 통합 E2E 구축

#### 목표

* kiosk가 결제 호출을 WireMock으로 대체하고, 성공/실패 흐름을 포함한 통합 E2E를 완성한다.

#### 핵심 활동

* payments-mock(WireMock) 서비스 추가 또는 단독 실행, 스텁 정적/동적 등록.
* kiosk 결제 베이스 URL 외부화(PAYMENTS_BASE_URL) 및 compose/테스트에서 일관 주입.
* atdd-tests에 kiosk → admin → DB와 kiosk → payments(WireMock) happy/sad-path E2E 추가.

#### 기대 결과

* payments-mock이 기동되고 kiosk가 해당 URL을 참조한다.
* 두 흐름(happy/sad)이 안정적으로 녹색 통과, 실행·설정 문서화.

# 3단계 - 외부 시스템 격리

## 학습 목표

* kiosk가 결제 호출을 실서비스 대신 WireMock으로 연동한다.
* Step 2의 kiosk → admin → DB 흐름을 유지하면서, kiosk → payment(WireMock) 흐름을 추가한다.
* atdd-tests에서 두 흐름(happy/sad-path)을 검증하는 E2E 테스트를 작성한다.

## 배경 및 범위

* 실제 결제 시스템 없이도 안정적인 시나리오 검증을 위해 결제 API를 모킹한다.
* 모킹은 응답 속도/내용을 고정해 플래키를 줄이고, 실패 케이스를 쉽게 재현할 수 있다.

## 해야 하는 것

* [] atdd-payments(계약/엔드포인트) 확인: 결제 요청 경로/메서드/응답 스키마 파악
* [] 앱 compose에 payments-mock(WireMock) 서비스 추가 또는 별도 실행 전략 확정
* [] kiosk의 결제 API 베이스 URL을 외부화하고 WireMock을 바라보도록 주입
* [] 결제 성공/실패 기본 스텁 등록(WireMock)
* [] atdd-tests에 E2E 테스트 추가:
  * [] kiosk → admin → DB(상품 목록 등)
  * [] kiosk → payments(WireMock)(결제 성공/실패)
* [] 실행 순서/설정 문서화

## 요구사항

### 1. WireMock 구성(서비스 추가 또는 단독 실행)

* 실행 방식 선택:
  * 앱 compose 내 서비스(payments-mock)로 띄우기, 공용 네트워크(atdd-net) 합류
  * 혹은 로컬 포트로 단독 실행 후 베이스 URL만 주입
* 스텁 관리:
  * 정적 매핑 파일(mappings/*.json)로 기동 시 자동 로드
  * 혹은 런타임에 Admin API로 스텁 등록

### 2. kiosk 결제 연동 설정

* 결제 API 베이스 URL을 환경변수/프로퍼티로 외부화(예: PAYMENTS_BASE_URL 또는 PAYMENT_API_URL).
* 로컬/CI에서도 동일 키로 주입 가능하도록 compose/테스트 양쪽에서 일관되게 사용.

### 3. E2E 테스트 작성(atdd-tests)

* 기존(상품 목록) 시나리오 유지: kiosk → admin → DB.
* 결제 시나리오 추가:
  * Happy-path: WireMock이 승인 응답(200/승인 바디)을 반환 → kiosk가 성공 흐름으로 처리
  * Sad-path: WireMock이 거절/오류 응답(4xx/5xx)을 반환 → kiosk가 실패 흐름/메시지 처리
* 베이스 URL은 환경변수로 외부화(KIOSK_BASE_URL, ADMIN_BASE_URL, PAYMENTS_BASE_URL).
* 준비 대기(폴링) 후 테스트 실행, 결과의 핵심 필드/상태만 단순 검증.

#### 실행 순서 제안

1. 인프라(DB) compose 기동
2. 앱 compose(kiosk, admin, 필요시 reservation) + payments-mock 기동
3. WireMock 스텁 확인/등록
4. E2E 테스트 실행
5. 종료/정리

## 완료 기준(Definition of Done)

* payments-mock이 기동되고, kiosk가 해당 베이스 URL을 참조한다.
* kiosk → admin → DB 시나리오와 kiosk → payments(WireMock) 시나리오(happy/sad)가 모두 통과한다.
* 실행/설정/테스트 방법이 문서화되어 재현 가능하다.

## 체크리스트

* [] WireMock 서비스가 공용 네트워크(atdd-net)에 합류했고 포트 충돌이 없다.
* [] 결제 베이스 URL이 외부화되어 환경별로 손쉽게 전환된다.
* [] 스텁이 기동 시 자동 로드되거나, 테스트 전 선등록된다.
* [] 준비 대기(healthcheck/폴링/리트라이)로 플래키가 없다.
* [] 성공/실패 경로 모두 테스트로 커버된다.

## 힌트

* WireMock 컨테이너 실행(개념)

```sh
docker run --rm -p 18090:8080 \
  -v "$PWD/mappings:/home/wiremock/mappings" \
  --name payments-mock wiremock/wiremock:latest
```

* 스텁 JSON(요청/응답 최소 예)

```json
{
  "request": { "method": "POST", "urlPath": "/payments" },
  "response": { "status": 200, "jsonBody": { "status": "APPROVED" } }
}
```

* Admin API로 동적 스텁 등록(개념)

```sh
curl -X POST "$PAYMENTS_BASE_URL/__admin/mappings" \
  -H 'Content-Type: application/json' \
  -d '{
    "request": {"method":"POST","urlPath":"/payments"},
    "response": {"status":200,"jsonBody":{"status":"APPROVED"}}
  }'
```

* kiosk 결제 베이스 URL 주입 예

```env
PAYMENTS_BASE_URL=http://payments-mock:8080
```

Compose 외부 네트워크 합류(개념)

```docker-compose.yml
networks:
  atdd-net:
    name: atdd-net
    external: true
```

## 참고 문서

* [WireMock](https://wiremock.org/docs/)
* [WireMock Docker 이미지](https://hub.docker.com/r/wiremock/wiremock)
* [Compose 파일 스펙](https://docs.docker.com/compose/compose-file/)

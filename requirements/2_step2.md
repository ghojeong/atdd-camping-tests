# 2단계 - 다중 서비스 테스트 환경

## 학습 목표

* admin, reservation을 Step 1의 틀에 추가해 3개 앱을 모두 컨테이너로 기동한다.
* 우선 각 앱이 자체 DB를 사용하도록 구성해 200 응답 스모크 테스트를 통과시킨다.
* 이후 공용 인프라 DB(atdd-tests/docker/docker-compose-infra.yml)로 전환하도록 코드/환경 구성을 수정한다.
* atdd-tests에서 kiosk → admin → DB 흐름을 검증하는 E2E 테스트를 작성한다(상품 목록 조회 등).

## 배경 및 범위

* 1단계에서 kiosk 단일 앱 기동과 스모크 테스트를 확보했다. 이제 다중 서비스 환경으로 확장하고, DB 구성을 단계적으로 단순화한다.
* 결제 모킹은 본 단계 범위가 아니다. 네트워크/DB 연결성, 기본 엔드포인트 응답, 간단한 시나리오 검증에 집중한다.

## 해야 하는 것

* [] repos/에 atdd-camping-admin, atdd-camping-reservation 동기화
* [] dockerfiles/Dockerfile-admin, dockerfiles/Dockerfile-reservation 준비
* [] 앱 compose에 admin, reservation 추가(초기: 각 앱 전용 DB 포함)
* [] atdd-tests에서 admin, reservation 스모크 테스트(200 응답) 추가/통과
* [] 인프라 compose(DB) 기동 후, 앱 compose가 공용 네트워크 atdd-net을 사용하도록 전환
* [] 각 앱이 공용 DB atdd-db를 참조하도록 코드/환경 수정 후 스모크 재통과
* [] kiosk → admin → DB E2E 테스트 작성·통과(예: 상품 목록 조회)

## 요구사항

### 1. admin/reservation 추가

* repos/ 하위에 두 서비스 클론/동기화
* Dockerfile 준비: dockerfiles/Dockerfile-admin, dockerfiles/Dockerfile-reservation.
* 앱 compose(atdd-tests/docker/docker-compose.yml)에 서비스를 추가한다.
* DB는 아래 3번에서 진행할 예정이니 지금은 무시한다.
* 200 응답 스모크 테스트를 통과시키다.

### 2. 애플리케이션 기동 및 e2e 테스트 통과

* 목적: kiosk → admin → DB 동작 여부를 확인한다. 각 애플리케이션을 기동해 전용 DB에 정상 연결되는지 확인하고 스모크/E2E 테스트를 통과시킨다.
* 시나리오 예시
  * admin 로그인 API(/auth/login)를 호출해 인증 토큰/쿠키를 발급받는다.
  * kiosk의 상품 목록 엔드포인트(/api/products)를 호출한다(내부적으로 admin을 통해 DB 조회).
  * 기대값: 상태코드 200, 응답 배열 길이 ≥ 1, 주요 필드 존재 여부 확인.
* 힌트: kiosk는 인증이 필요하므로 사전에 인증 정보를 받아와야 한다. admin 로그인으로 발급된 AUTH_TOKEN(쿠키/헤더)을 이후 요청에 포함한다.

### 3. 공용 인프라 DB로 전환

* 공용 인프라 DB는 @docker-compose-infra.yml에 정의된 구성을 사용한다(atdd-tests/docker/docker-compose-infra.yml).
* 인프라 compose를 먼저 기동해 공용 네트워크(atdd-net)와 DB(atdd-db)를 준비한다.
* 앱 compose에서 각 서비스를 공용 네트워크 atdd-net에 합류하도록 수정한다.
* 각 앱의 DB 접속 정보는 공용 DB atdd-db를 가리키도록 변경한다(환경변수 오버라이드 권장: SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD).
* DB 초기화는 기존 infra/db/init.sql을 활용한다.
* 힌트: 데이터 초기화 시점은 infra를 띄우고 app을 실행하기 전에 수행해야 한다. 또 디비가 뜨기 전에 초기화 쿼리를 실행할 경우 동작하지 않으니 이 부분도 참고해야 한다.

## 완료 기준

* 3개 앱(kiosk, admin, reservation)의 스모크 테스트가 모두 200 응답으로 통과한다.
* 공용 인프라 DB로 전환된 구성에서 스모크 테스트가 재통과한다.
* E2E 테스트(상품 목록 조회)가 통과한다.
* 실행/전환/테스트 방법이 문서화되어 재현 가능하다.

## 체크리스트

* [] 앱 compose의 포트/네트워크 충돌이 없다.
* [] 공용 네트워크 atdd-net을 사용하고, DB 호스트는 atdd-db로 설정했다.
* [] 준비 대기(healthcheck/폴링/리트라이)로 플래키가 없다.
* [] 테스트의 베이스URL과 DB 크리덴셜이 외부화되어 있다.
* [] DB 시드 전략(SQL/관리 API)이 정해져 있다.

## 힌트

### 다중 앱 + 전용 DB 구성

* 최소 환경변수 키

```env
SPRING_DATASOURCE_URL=jdbc:mysql://<db-service>:3306/<db>?characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=secret
```

* DB 준비 후 앱 기동(의미만 전달)

```docker-compose.yml
depends_on:
  <db-service>:
    condition: service_healthy
```

### 공용 DB 전환

* 인프라 기동 예

```sh
docker compose -f atdd-tests/docker/docker-compose-infra.yml up -d
```

* 외부 네트워크 합류 선언

```docker-compose.yml
networks:
  atdd-net:
    name: atdd-net
    external: true
```

* 공용 DB URL 예

```env
SPRING_DATASOURCE_URL=jdbc:mysql://atdd-db:3306/atdd?characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
```

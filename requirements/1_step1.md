# 1단계 - 단일 서비스 테스트 환경

## 학습 목표

* kiosk 애플리케이션을 컨테이너로 기동한다.
* atdd-tests에서 kiosk 대상 200 응답 여부를 확인하는 smoke 테스트를 작성한다.
* 복잡한 의존 관계(admin 등)는 건너뛰고, “앱이 떠서 200을 돌려준다”에만 집중한다.

## 배경 및 범위

* 실제 정상 동작을 위해서는 admin 등이 함께 떠야 하지만, 본 단계에서는 서비스 기동과 200 응답 확인만 수행한다.
* DB가 없어도 기동이 가능하도록 설정하는 것을 권장한다(필요 시 인프라 compose로 DB를 함께 띄우는 것은 선택).

## 해야 하는 것

* [] kiosk 서비스 소스 최신화(클론/브랜치 동기화)
* [] kiosk용 Dockerfile 작성(atdd-tests/infra/dockerfiles/Dockerfile-kiosk)
* [] 단일 서비스용 Docker Compose 작성(atdd-tests/infra/docker-compose.yml)
* [] atdd-tests에 kiosk 200 응답 smoke 테스트 성공

## 요구사항

### 1. 서비스 코드 준비(repos/에 클론·동기화)

* atdd-tests 루트에서 repos/ 폴더를 만든다.
* repos/ 하위에 atdd-camping-kiosk를 클론한다.
  * https://github.com/next-step/atdd-camping-kiosk 레포지토리를 사용한다.
  * main 브랜치를 기본으로 사용한다.
* 저장소 URL과 브랜치는 수강생 환경에 맞춰 설정한다(기본 브랜치 main).
* 리뷰 요청 시 repo/ 디렉토리는 무시(.gitignore 추가)하고 push한다.
* 간단한 Shell/Gradle Task로 자동화한다.

### 2. kiosk Dockerfile 준비

* 기대 위치: dockerfiles/Dockerfile-kiosk
* 이미지 전략은 자유(예: bootBuildImage, 직접 docker build, JAR 바인드). 팀 상황에 맞게 선택한다.

### 3. 애플리케이션 compose 작성(단일 서비스)

* 기대 위치: atdd-tests/infra/docker-compose.yml
* 내용: kiosk 컨테이너 1개만 정의(포트 매핑, 환경변수 등). 외부 네트워크 사용은 선택.

### 4. 기동 및 종료 자동화

```sh
# 애플리케이션 기동(이미지 빌드 포함)
docker compose -f atdd-tests/infra/docker-compose.yml up -d --build

# 상태 확인
docker compose -f atdd-tests/infra/docker-compose.yml ps
docker logs <kiosk-container-name> --tail 100

# 종료(필요 시)
docker compose -f atdd-tests/infra/docker-compose.yml down -v
```

* docker compose 동작에 대한 자동화를 진행한다.

### 5. Smoke 테스트 작성·실행(200 응답 확인)

* 위치: atdd-tests 테스트 프로젝트(각 서비스 저장소가 아니 테스트용 저장소)
* 외부 프로세스로 기동한 kiosk를 HTTP로 호출한다.
* 베이스 URL은 환경변수/시스템 프로퍼티로 주입(예: KIOSK_BASE_URL=http://localhost:18081).
* 엔드포인트는 간단한 것으로 선택(예: /actuator/health 또는 /).
* 짧은 폴링/리트라이로 준비 대기 후 200 응답을 확인한다.

## 완료 기준

* 로컬에서 kiosk 컨테이너가 기동되어 접근 가능하다.
* atdd-tests의 kiosk smoke 테스트가 200 응답으로 통과한다.
* 구성과 실행 방법이 자동화(스크립트 등)가 되어있고, 짧게 문서화되어 재현이 가능하다.

## 힌트

### 1. 코드 클론/동기화 시 참고할 Git 명령

* 특정 브랜치만 클론: 필요한 브랜치만 얕게 받으면 빠르고 가볍다.

```sh
# 특정 브랜치만, 히스토리 최소화(깊이 1)
git clone --branch <branch> --single-branch --depth 1 <repo_url> repos/atdd-camping-kiosk

# 전체 히스토리가 필요하면 --depth 1 옵션을 제거
git clone --branch <branch> --single-branch <repo_url> repos/atdd-camping-kiosk
```

* 특정 브랜치만 fetch: 전체를 받지 않고 관심 브랜치만 업데이트한다.

```sh
cd repos/atdd-camping-kiosk
git fetch origin <branch>
git switch -c <branch> --track origin/<branch>  # 최초 체크아웃
# 이후 업데이트
git switch <branch>
git pull --rebase
```

* 참고: [Git 공식 문서: git clone](https://git-scm.com/docs/git-clone), [git fetch](https://git-scm.com/docs/git-fetch), [git switch](https://git-scm.com/docs/git-switch)

### 2. Dockerfile 소개

* 무엇인가? 컨테이너 이미지를 어떻게 빌드할지 선언하는 스크립트 파일.
* 어떤 내용이 필요한가?
  * 베이스 이미지(Java 런타임 등) 지정
  * 작업 디렉터리 설정, 애플리케이션 JAR 복사
  * 환경변수/포트 노출
  * 실행 명령(ENTRYPOINT/CMD)

```Dockerfile
# dockerfiles/Dockerfile-kiosk (예시)
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드된 JAR 복사(예: kiosk 프로젝트에서 만든 JAR)
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

* 참고: [Dockerfile Reference](https://docs.docker.com/reference/dockerfile/), [Spring Boot Containerization](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#container-images) 가이드

### 3. Docker Compose 소개

* 무엇인가? 여러 컨테이너 서비스를 하나의 파일에서 정의·기동하는 도구. 본 단계에서는 kiosk 단일 서비스만 정의.
* 어떤 내용이 필요한가? 서비스 이름, 이미지/빌드, 포트 매핑, 환경변수, 필요 시 depends_on.

```docker-compose.yml
# atdd-tests/infra/docker-compose.yml (예시)
services:
  kiosk:
    build:
      context: ../repos/atdd-camping-kiosk
      dockerfile: ../../dockerfiles/Dockerfile-kiosk
    ports:
      - "18081:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=local
      - SERVER_PORT=8080
```

* 참고: [Compose 파일 스펙](https://docs.docker.com/compose/compose-file/), [Docker Compose 사용법](https://docs.docker.com/compose/)

### 4. build.gradle.kts에서 task 만드는 방법

* 무엇인가? 반복되는 명령을 코드로 표준화하는 Gradle 작업 정의. 예: Compose 기동/종료 래핑.
* 어떤 내용이 필요한가? 작업 이름, 타입(예: Exec), 실행 커맨드, 설명/그룹.

```build.gradle.kts
// 예: 프로젝트 루트 또는 atdd-tests의 build.gradle.kts에 추가
tasks.register<Exec>("kioskComposeUp") {
    group = "infra"
    description = "Run kiosk via docker compose (build + up)"
    commandLine(
        "docker", "compose",
        "-f", "atdd-tests/infra/docker-compose.yml",
        "up", "-d", "--build"
    )
}

tasks.register<Exec>("kioskComposeDown") {
    group = "infra"
    description = "Stop kiosk compose and remove volumes"
    commandLine(
        "docker", "compose",
        "-f", "atdd-tests/infra/docker-compose.yml",
        "down", "-v"
    )
}
```

* 참고: [Gradle Kotlin DSL: Tasks](https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin_tasks), [Gradle Exec 작업](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/Exec.html)

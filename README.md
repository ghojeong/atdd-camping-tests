# 캠핑장 통합 시스템 테스트

## 실행방법

```sh
# 깃 클론
git clone --branch main --single-branch --depth 1 https://github.com/next-step/atdd-camping-kiosk.git repos/atdd-camping-kiosk

# 컨테이너 기동
./gradlew kioskComposeUp

# 테스트 실행 (KIOSK_BASE_URL 환경변수 설정)
KIOSK_BASE_URL=http://localhost:18081 ./gradlew test

# 컨테이너 종료
./gradlew kioskComposeDown
```

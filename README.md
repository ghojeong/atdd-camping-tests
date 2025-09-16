# 캠핑장 통합 시스템 테스트

## 실행방법

```sh
# 깃 클론
./gradlew cloneRepos

# 컨테이너 기동
./gradlew allUp

# 테스트 실행 (KIOSK_BASE_URL 환경변수 설정)
./gradlew test

# 컨테이너 종료
./gradlew allDown
```

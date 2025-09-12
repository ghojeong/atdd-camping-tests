###########
# Builder #
###########
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

# 프로젝트 소스 전체를 빌드 컨텍스트로 받아와 부트 JAR 빌드
COPY . .
RUN chmod +x ./gradlew \
    && ./gradlew --no-daemon clean bootJar -x test

############
# Runtime  #
############
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드 결과 JAR 복사
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

# 실행
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar"]

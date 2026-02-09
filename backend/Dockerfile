FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew assemble --no-daemon

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache tzdata
ENV TZ=Asia/Seoul
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "-Duser.timezone=Asia/Seoul", "app.jar"]

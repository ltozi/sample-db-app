# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

COPY .m2/repository /root/.m2/repository

RUN pwd && ls -larth
RUN mvn clean package -DskipTests -B

RUN pwd && ls -larth
# Mount Maven's local repo as a cache - persists across builds
# RUN --mount=type=cache,target=/root/.m2/repository,id=mvn-repo \
#     mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
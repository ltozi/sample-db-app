# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy cached m2 from named context (mounted, not part of build context tarball)
RUN --mount=type=bind,from=m2cache,target=/tmp/m2cache \
    mkdir -p /root/.m2 && cp -r /tmp/m2cache /root/.m2/repository

COPY pom.xml .
COPY src ./src

# Force snapshot updates with -U
RUN mvn clean package -DskipTests -B -U

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
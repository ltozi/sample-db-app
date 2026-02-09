# Stage 1: Cache Maven dependencies (changes only if pom.xml changes)
FROM maven:3.9.10-eclipse-temurin-17 AS dependencies
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Stage 2: Build the app (reuses deps cache; rebuilds only if src changes)
FROM dependencies AS build
WORKDIR /build
COPY src ./src
RUN mvn package -B -DskipTests

# Stage 3: Runtime image (slim JRE Alpine, ~120MB final size)
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -g 1001 -S appuser && \
    adduser -S appuser -u 1001 -G appuser
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]

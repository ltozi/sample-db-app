# APPROACH 1: BEST - Copy .m2 into a layer (not cache mount)
# This ensures dependencies are cached as Docker layers in GitHub Actions

# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 1. Copy POM file
COPY pom.xml .

# 2. Download dependencies (this layer will be cached!)
# Remove the cache mount - we want this in a layer instead
RUN --mount=type=cache,target=/root/.m2/repository mvn dependency:go-offline dependency:resolve-plugins -B

# 3. Copy source code (only this layer invalidates when code changes)
COPY src ./src

# 4. Build the application
RUN --mount=type=cache,target=/root/.m2/repository mvn package -B -DskipTests

# Optimizer stage: Extracts JAR layers using layertools
FROM eclipse-temurin:17-jre AS optimizer
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Final stage: Minimal runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy extracted layers from the optimizer stage
# Ordered by frequency of change (rarely -> frequently)
COPY --from=optimizer /app/dependencies/ ./
COPY --from=optimizer /app/spring-boot-loader/ ./
COPY --from=optimizer /app/snapshot-dependencies/ ./
COPY --from=optimizer /app/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
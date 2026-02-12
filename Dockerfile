# Stage 1: Dependency Cache
FROM maven:3.9-eclipse-temurin-17 AS cache
WORKDIR /app

# Only copy the pom.xml to leverage Docker layer caching
COPY pom.xml .

# Download dependencies (using -B for batch mode)
# Option A: Standard Maven (might miss some plugins)
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B

# Option B: Robust alternative (requires adding the plugin to pom.xml)
# RUN mvn de.qaware.maven:go-offline-maven-plugin:resolve-dependencies -B

# Stage 2: Actual Build
FROM cache AS builder
COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn package -DskipTests -B

# Stage 3: Final Image
FROM eclipse-temurin:17-jre
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

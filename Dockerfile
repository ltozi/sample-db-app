FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Copy only pom.xml first for better layer caching
COPY pom.xml .

# Use BuildKit cache mount to reuse Maven dependencies across builds
# This mounts a cache directory that persists between builds
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B || true

# Copy source code
COPY src src

# Build the application, reusing cached dependencies
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn package -DskipTests -B

# Optimizer stage: Extracts JAR layers using layertools
FROM eclipse-temurin:17-jre AS optimizer
WORKDIR /app
COPY --from=build /workspace/app/target/*.jar app.jar
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
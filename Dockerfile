# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 1. Copy POM files
COPY pom.xml .

# 2. Aggressive Dependency Download (Cache Mount + Offline Prep)
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline dependency:resolve-plugins -B

# 3. Copy source code
COPY src ./src

# 4. Build (Offline)
RUN --mount=type=cache,target=/root/.m2 mvn package -B -o -DskipTests

# Optimizer stage: Extracts JAR layers using layertools
FROM eclipse-temurin:17-jre AS optimizer
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar
# Extract the layers into directories
RUN java -Djarmode=layertools -jar app.jar extract

# Final stage: Minimal runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# 5. Copy extracted layers from the optimizer stage
# Ordered by frequency of change (rarely -> frequently)
COPY --from=optimizer /app/dependencies/ ./
COPY --from=optimizer /app/spring-boot-loader/ ./
COPY --from=optimizer /app/snapshot-dependencies/ ./
COPY --from=optimizer /app/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
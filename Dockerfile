FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ls -lart /Users/freename/.m2/repository || true

# This now uses the volume mounted from the host
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw package -DskipTests

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
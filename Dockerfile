# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only dependency files first - these change less frequently
COPY pom.xml .

# Download dependencies - this layer will be cached unless pom.xml changes
RUN mvn dependency:go-offline dependency:resolve-plugins dependency:resolve -B

# Copy source code - this changes more frequently
COPY src ./src

# Build the application
RUN mvn package -B -o -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built artifact from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

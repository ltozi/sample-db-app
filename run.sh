#!/bin/bash

# Build the application
echo "Building the application..."
mvn clean package -DskipTests

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful. Starting application..."
    
    # Run with external configuration
    java -jar target/sample-db-app-1.0.0.jar \
        --spring.config.location=classpath:/,file:./config/
else
    echo "Build failed. Please check the errors above."
    exit 1
fi

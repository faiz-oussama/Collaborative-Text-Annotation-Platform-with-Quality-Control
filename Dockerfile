# Build stage
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /workspace/app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /workspace/app/target/demo-0.0.1-SNAPSHOT.war app.war

# Create necessary directories
RUN mkdir -p /app/public

# Expose the application port
EXPOSE 8080

# Set the entry point
ENTRYPOINT ["java", "-jar", "app.war"]

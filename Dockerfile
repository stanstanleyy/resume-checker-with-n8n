# Lightweight Java runtime
FROM eclipse-temurin:17-jre-alpine

# App directory
WORKDIR /app

# Copy built jar
COPY target/*.jar app.jar

# Expose port (Spring Boot default)
EXPOSE 8080

# Run app
ENTRYPOINT ["java","-jar","app.jar"]
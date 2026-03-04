# Stage 1: Build the application
FROM alpine:3.23 AS build_stage

# Add the necessary extra packages
RUN apk add --no-cache maven openjdk21-jdk

# Copy the source code and Maven resources
COPY ./task-service /build/task-service
COPY ./.m2 /build/.m2
COPY ./.mvn /build/.mvn

# Build the application
RUN ls -al /build && cd /build/task-service && mvn -f pom.xml -Dmaven.repo.local=/build/.m2/repository clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the JAR from builder stage
COPY --from=build_stage /build/task-service/target/task-service-1.0.0.jar app.jar

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

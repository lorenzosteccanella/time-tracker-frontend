# Stage 1: Build the application
FROM maven:3.8.3-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven POM file and source code
COPY pom.xml ./
COPY src ./src

# Package the application
RUN mvn clean package

# Stage 2: Run the application
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/timetracker-frontend-0.0.1-SNAPSHOT.jar /app/app.jar

# Run the JAR file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
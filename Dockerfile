# Part 1: Build the App using Maven
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Part 2: Run the App using Java
FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/nagar-alert-hub-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
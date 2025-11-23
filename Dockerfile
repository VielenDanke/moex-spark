# --- Stage 1: Build the Application ---
# Use Maven with Java 11 for the build process
FROM maven:3.8.4-eclipse-temurin-11 AS builder

WORKDIR /app

# Copy project definition to cache dependencies
COPY pom.xml .

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Run the Application ---
# Use the Java 11 JRE for the runtime
FROM eclipse-temurin:11-jre-ubi9-minimal

WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/target/moex-spark-1.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
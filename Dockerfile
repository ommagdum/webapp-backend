# ---------- Stage 1: Build ----------
FROM maven:3.9.6-eclipse-temurin-17 as builder

WORKDIR /app

# Copy source code
COPY . .

# Build the jar
RUN mvn clean package -DskipTests

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

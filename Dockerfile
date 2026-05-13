# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy project files
COPY pom.xml .
COPY src ./src
COPY mvnw* ./

# Build the application, skipping tests for faster build
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S marketnest && adduser -S marketnest -G marketnest

# Copy JAR from builder stage
COPY --from=builder /build/target/MarketNest-0.0.1-SNAPSHOT.jar app.jar

# Change ownership to non-root user
RUN chown -R marketnest:marketnest /app

# Switch to non-root user
USER marketnest

# Expose port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


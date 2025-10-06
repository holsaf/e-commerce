# Multi-stage Dockerfile for Spring Boot Application
# Optimized for AWS Fargate deployment

# Stage 1: Build the application
FROM gradle:8.5-jdk17-alpine AS builder

# Set working directory
WORKDIR /app

# Copy gradle configuration files first (for layer caching)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Clean Gradle cache and download fresh dependencies
RUN rm -rf /root/.gradle/caches/* && \
    gradle dependencies --no-daemon --refresh-dependencies || true

# Copy source code
COPY src ./src

# Clean build and create fresh JAR
RUN gradle clean bootJar --no-daemon -x test --refresh-dependencies

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Set working directory
WORKDIR /app

# Copy the JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port 8080
EXPOSE 8080

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

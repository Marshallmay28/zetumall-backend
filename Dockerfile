# Build Stage
FROM gradle:jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Run Stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/zetumall-backend-1.0.0.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

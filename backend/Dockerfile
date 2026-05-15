# ── Stage 1: Dependency cache ─────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS deps
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q

# ── Stage 2: Build ────────────────────────────────────────────────────────────
FROM deps AS build
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 3: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

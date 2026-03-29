# ── Stage 1: Build the JAR ────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Download dependencies first (layer-cache friendly)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY src ./src
RUN ./mvnw package -DskipTests -q

# ── Stage 2: Runtime image with pdflatex ──────────────────────────────────
# Only the 3 texlive packages actually used by the resume templates.
# Dropping texlive-fonts-extra (~800 MB) cuts image from 2.2 GB → ~420 MB.
FROM eclipse-temurin:17-jre-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    texlive-latex-base \
    texlive-latex-extra \
    texlive-fonts-recommended \
    lmodern \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

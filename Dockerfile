FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.source="https://github.com/jcidvidal/tarea2"

RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/target/*.jar /app/app.jar

ENV APP_PORT=3001 \
    APP_ENV=production \
    CSV_PATH=/data/compras.csv

EXPOSE 3001
USER app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

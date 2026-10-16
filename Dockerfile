FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src

RUN ./mvnw -B clean package -DskipTests

FROM azul/zulu-openjdk-alpine:21-jre

WORKDIR /app

RUN adduser -D -u 10001 appuser

COPY --from=build /app/target/tarea2-0.0.1-SNAPSHOT.jar /app/app.jar

USER 10001

ENV APP_PORT=3001
ENV CSV_PATH=/data/compras.csv
ENV APP_ENV=production

EXPOSE 3001

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${APP_PORT} -jar /app/app.jar"]

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/stock-management-1.0.0.jar /app/app.jar
# Conversion DATABASE_URL (postgres://...) -> DB_URL (JDBC) avant demarrage.
COPY entrypoint.sh /app/entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["sh", "/app/entrypoint.sh"]
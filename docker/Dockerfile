FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml ./
COPY proto/pom.xml ./proto/
COPY server/pom.xml ./server/

RUN mvn dependency:resolve

COPY proto ./proto/
COPY server ./server/

RUN mvn clean install -DskipTests

WORKDIR /app/server
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/server/target/*.jar ./app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
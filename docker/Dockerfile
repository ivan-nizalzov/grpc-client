FROM eclipse-temurin:17-jre
WORKDIR /app

COPY ./server/target/*.jar ./app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
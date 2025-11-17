FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=target/*.jar
WORKDIR /app
COPY ${JAR_FILE} app.jar
EXPOSE 9005
ENTRYPOINT ["java","-jar","/app/app.jar"]
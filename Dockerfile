FROM maven:3.9.9

WORKDIR /
COPY . .
RUN mvn clean package -DskipTests

#CMD mvn spring-boot:run

FROM openjdk:17-jdk-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
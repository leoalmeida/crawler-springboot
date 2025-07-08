FROM maven:3.8.5-openjdk-17-slim

ADD . /usr/src/crawler
WORKDIR /usr/src/crawler
EXPOSE 8081
ENTRYPOINT ["mvn", "clean", "package", "spring-boot:run"]

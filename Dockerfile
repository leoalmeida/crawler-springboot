FROM maven:3.8.5-openjdk-17-slim

ADD . /usr/src/crawler
WORKDIR /usr/src/crawler
EXPOSE 4567
ENTRYPOINT ["mvn", "clean", "verify", "exec:java"]
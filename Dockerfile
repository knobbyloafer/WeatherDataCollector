FROM anapsix/alpine-java
MAINTAINER Tom Knobloch 
COPY ./target/WeatherDataCollector-1.0-SNAPSHOT-jar-with-dependencies.jar /home/WeatherDataCollector-1.0-SNAPSHOT-jar-with-dependencies.jar
COPY ./weatherdata.properties /home/weatherdata.properties
WORKDIR /home
CMD ["java", "-jar", "/home/WeatherDataCollector-1.0-SNAPSHOT-jar-with-dependencies.jar"]
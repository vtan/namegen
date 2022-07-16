FROM --platform=$TARGETPLATFORM amazoncorretto:17.0.3

COPY target/scala-2.13/namegen-assembly-0.1.jar ./
COPY data/ ./data

CMD java -jar namegen-assembly-0.1.jar
EXPOSE 8081

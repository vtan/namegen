FROM --platform=$BUILDPLATFORM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1 as scala

COPY build.sbt ./
COPY project/build.properties ./project/
COPY project/assembly.sbt ./project/
COPY src/ ./src

RUN sbt assembly

FROM --platform=$TARGETPLATFORM amazoncorretto:17.0.4 as corretto-jdk

RUN yum -y install binutils

RUN $JAVA_HOME/bin/jlink \
    --verbose \
    --add-modules java.base,java.management,java.naming,java.net.http,java.security.jgss,java.security.sasl,java.sql,jdk.httpserver,jdk.unsupported \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /customjre

FROM --platform=$TARGETPLATFORM debian:11.4-slim

ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY --from=corretto-jdk /customjre $JAVA_HOME

USER 1000

COPY --from=scala --chown=1000:1000 /root/target/scala-3.1.3/namegen-assembly-0.1.jar ./
COPY --chown=1000:1000 data/ ./data

CMD java -jar namegen-assembly-0.1.jar
EXPOSE 8081

FROM applemann/java:8 as prepare

COPY . /opt/project
WORKDIR /opt/project

ADD https://github.com/glowroot/glowroot/releases/download/v0.13.6/glowroot-0.13.6-dist.zip .
RUN apt-get update \
    && apt-get install -y unzip \
    && unzip glowroot-0.13.6-dist.zip

RUN ./gradlew clean build 



FROM applemann/java:8
WORKDIR /opt/project

COPY --from=prepare /opt/project/build/libs/learn-english-words-*.jar learn-english-words.jar
COPY --from=prepare /opt/project/glowroot/ ./glowroot/
RUN echo '{ "web": { "port": 4000, "bindAddress": "0.0.0.0" } }' > ./glowroot/admin.json

EXPOSE 8080 4000
ENTRYPOINT java -javaagent:./glowroot/glowroot.jar -jar ./learn-english-words.jar

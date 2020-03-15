FROM applemann/java:8

COPY . /opt/project
WORKDIR /opt/project

RUN ./gradlew clean build 

EXPOSE 8080

ENTRYPOINT ./gradlew run


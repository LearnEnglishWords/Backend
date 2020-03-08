#FROM oracle/graalvm-ce:20.0.0-java8 as graalvm
FROM oracle/graalvm-ce:20.0.0-java11 as graalvm 
RUN gu install native-image

COPY . /home/app/micronaut-graal-app
WORKDIR /home/app/micronaut-graal-app

RUN native-image --no-server --no-fallback -H:+ReportExceptionStackTraces \
--initialize-at-build-time=com.mysql.cj.jdbc.Driver\
--initialize-at-run-time=com.mysql.cj.jdbc.AbandonedConnectionCleanupThread \
-cp build/libs/learn-english-words-*.jar


FROM frolvlad/alpine-glibc
RUN apk update && apk add libstdc++
COPY --from=graalvm /home/app/micronaut-graal-app/micronaut-graal-app /micronaut-graal-app/micronaut-graal-app
EXPOSE 8080
ENTRYPOINT ["/micronaut-graal-app/micronaut-graal-app"]

FROM gradle:8.8-jdk21 AS build
COPY --chown=gradle:gradle . /logbot
WORKDIR /logbot
RUN gradle shadowJar --no-daemon

FROM openjdk:11.0.16-jre-slim
RUN mkdir /config/
COPY --from=build /logbot/build/libs/*.jar /

ENTRYPOINT ["java", "-jar", "/LogBot.jar"]
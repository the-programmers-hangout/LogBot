FROM gradle:6.5.1-jdk14 AS build
COPY --chown=gradle:gradle . /starter
WORKDIR /logbot
RUN gradle shadowJar --no-daemon

FROM openjdk:11.0.8-jre-slim
RUN mkdir /config/
COPY --from=build /logbot/build/libs/*.jar /

ENTRYPOINT ["java", "-jar", "/LogBot.jar"]
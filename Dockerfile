FROM gradle:8.8-jdk17 AS build
COPY --chown=gradle:gradle . /logbot
WORKDIR /logbot
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:17-jre
RUN mkdir /config/
COPY --from=build /logbot/build/libs/*.jar /

ENTRYPOINT ["java", "-jar", "/LogBot.jar"]
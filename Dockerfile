FROM maven:3.9.2-eclipse-temurin-17-alpine AS build
WORKDIR /workspace/app

COPY pom.xml .
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn install -DskipTests
RUN mv target/*.jar target/application.jar

RUN java -Djarmode=layertools -jar target/application.jar extract --destination target/extracted

FROM eclipse-temurin:17-jre-alpine
ARG EXTRACTED=/workspace/app/target/extracted
WORKDIR /application

COPY --from=build ${EXTRACTED}/dependencies/ ./
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=build ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=build ${EXTRACTED}/application/ ./

CMD ["java","-noverify","org.springframework.boot.loader.JarLauncher"]
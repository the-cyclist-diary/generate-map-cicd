FROM maven:3.9 AS maven_build

COPY ./ ./

RUN mvn clean package

FROM eclipse-temurin:21-alpine

COPY --from=maven_build target/map-generator.jar /

ENTRYPOINT ["java", "-jar", "/map-generator.jar"]
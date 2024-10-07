FROM maven:3.9 AS maven_build

COPY ./ ./

RUN mvn clean package

FROM eclipse-temurin:21-alpine

COPY --from=maven_build target/map-generator.jar /

ENV GITHUB_SERVER_URL=""
ENV GITHUB_REPOSITORY=""
ENV CONTENT_FOLDER="./content"
ENV REPO_URL="${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}.git"

ENTRYPOINT ["java", "-jar", "/map-generator.jar", "${CONTENT_FOLDER}", "${REPO_URL}", "${GITHUB_REPOSITORY_OWNER}", "${GITHUB_TOKEN}"]
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY config config
COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S bookflow && adduser -S bookflow -G bookflow
WORKDIR /app
COPY --from=build /workspace/target/bookflow-0.0.1-SNAPSHOT.jar app.jar
USER bookflow
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]


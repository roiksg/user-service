FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src ./src

RUN ./mvnw dependency:go-offline -B

RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8092

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]
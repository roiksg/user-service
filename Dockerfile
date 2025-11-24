FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src ./src

RUN ./mvnw dependency:go-offline -B

RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
    wget ca-certificates unzip \
    && rm -rf /var/lib/apt/lists/* \
    && wget -q https://github.com/async-profiler/async-profiler/releases/latest/download/async-profiler-linux-x64.tar.gz \
    && tar -xzf async-profiler-linux-x64.tar.gz -C /opt \
    && mv /opt/async-profiler-* /opt/async-profiler \
    && rm async-profiler-linux-x64.tar.gz

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8092

EXPOSE 12345

ENTRYPOINT ["java", \
    "-XX:+UnlockDiagnosticVMOptions", \
    "-XX:+DebugNonSafepoints", \
    "-agentpath:/opt/async-profiler/lib/libasyncProfiler.so=port=12345,start,server=/tmp/ap", \
    "-jar", "/app/app.jar", \
    "--spring.profiles.active=docker"]
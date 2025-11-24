# ========== BUILDER ==========
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src ./src

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN apt-get update && \
    apt-get install -y --no-install-recommends wget ca-certificates && \
    wget -q https://github.com/async-profiler/async-profiler/releases/download/v2.10/async-profiler-2.10-linux-x64.tar.gz && \
    tar -xzf async-profiler-2.10-linux-x64.tar.gz -C /opt && \
    mv /opt/async-profiler-2.10-linux-x64 /opt/async-profiler && \
    rm async-profiler-2.10-linux-x64.tar.gz && \
    apt-get remove -y wget ca-certificates && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

EXPOSE 8092
EXPOSE 12345

ENTRYPOINT ["java", \
    "-XX:+UnlockDiagnosticVMOptions", \
    "-XX:+DebugNonSafepoints", \
    "-agentpath:/opt/async-profiler/lib/libasyncProfiler.so=port=12345,start,server=/tmp/ap", \
    "-jar", "/app/app.jar", \
    "--spring.profiles.active=docker"]
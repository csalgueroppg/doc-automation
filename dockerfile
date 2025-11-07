# Stage 1: Builder
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpha
WORKDIR /app

RUN apk add --no-cache curl
RUN addgroup -g 1001 appuser && \
    adduser -D -u 1001 -G appuser appuser

COPY --from=build /app/target/iics-doc-gen-*.jar app.jar
RUN chown -R appuser:appuser /app
USER appuser 

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+DisableExplicitGC \
    -XX:+UserStringDeduplication \
    -Djava.security.edg=file:/dev/.urandom"

ENTRYPOINT [ "sh", "-c", "java JAVA_OPTS -jar app.jar" ]
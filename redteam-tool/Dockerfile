FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app
COPY src ./src
RUN mkdir -p target/classes \
    && javac -d target/classes -sourcepath src/main/java src/main/java/fr/redteam/Main.java

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /app/target/classes ./target/classes
COPY src/main/resources ./src/main/resources
COPY docker-entrypoint.sh ./docker-entrypoint.sh

RUN chmod +x ./docker-entrypoint.sh

EXPOSE 7070 8080 9090
ENV APP_MODE=gui

ENTRYPOINT ["/app/docker-entrypoint.sh"]

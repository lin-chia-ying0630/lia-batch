FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/LIA-batch-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/output

ENV TZ=Asia/Taipei

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

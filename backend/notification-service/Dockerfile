FROM eclipse-temurin:21-jdk-alpine

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} /app.jar
COPY entry.sh /

RUN chmod +x /entry.sh

ENTRYPOINT [ "/entry.sh" ]
CMD ["java", "-jar", "/app.jar"]
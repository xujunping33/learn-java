FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/*.jar app.jar

ENV JAVA_OPTS=""
EXPOSE 9084

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]


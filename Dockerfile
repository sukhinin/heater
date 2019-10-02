FROM amazoncorretto:8

RUN mkdir /app
COPY build/libs/heater-*-all.jar /app/heater.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/heater.jar"]

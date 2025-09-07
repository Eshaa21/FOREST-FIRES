FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/forest-fires-1.0.0-shaded.jar app.jar
COPY datafile.csv ./
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

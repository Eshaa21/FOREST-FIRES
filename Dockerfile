FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY pom.xml ./
COPY src ./src
COPY datafile.csv ./
RUN apt-get update && apt-get install -y maven
RUN mvn -q -DskipTests clean package
EXPOSE 8080
CMD ["java", "-jar", "target/forest-fires-1.0.0-shaded.jar"]

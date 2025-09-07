FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY pom.xml ./
COPY src ./src
COPY datafile.csv ./
RUN apt-get update && apt-get install -y maven
RUN mvn -q -DskipTests clean package
RUN ls -la target/
RUN ls -la target/dependency/
EXPOSE 8080
CMD ["java", "-cp", "target/classes:target/dependency/*", "org.forest.web.WebMain"]

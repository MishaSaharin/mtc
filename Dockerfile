#FROM maven:3.9.8-eclipse-temurin-17-alpine as bilder
#WORKDIR /mtc
#COPY . /mtc/.
#RUN mvn -f /mtc/pom.xml clean package -D maven.test.skip=true
#FROM eclipse-temurin:17-jre-alpine
#WORKDIR /mtc
#COPY --from=bilder /mtc/target/*.jar /mtc/*.jar
#EXPOSE 9000
#ENTRYPOINT ["java", "-jar", "/mtc/*.jar"]
#Java 17
#JSoup 1.12.1
#PostgreSQL JDBC Driver 42.3.3
#Log4j 1.2.17
#SLF4J Log4j 1.7.30

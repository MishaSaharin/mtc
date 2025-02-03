FROM eclipse-temurin:17-jdk-jammy
WORKDIR /mtc
COPY target/*.jar mtc.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "mtc.jar"]
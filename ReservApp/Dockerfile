# Utilizamos una imagen base de OpenJDK 21
FROM openjdk:21-jdk-slim

# Establecemos el directorio de trabajo en el contenedor
WORKDIR /app

# Copia el archivo JAR de tu aplicación
# El JAR se genera cuando ejecutas: ./mvnw clean package -DskipTests
COPY target/*.jar reservApp.jar

# Expone el puerto donde corre tu aplicación Spring Boot (por defecto 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/reservApp.jar"]
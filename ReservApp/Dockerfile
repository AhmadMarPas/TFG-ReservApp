# Utilizamos una imagen base de OpenJDK 21
FROM openjdk:21-jdk-slim

# Establecemos el directorio de trabajo en el contenedor
WORKDIR /app

# Copia el archivo JAR de tu aplicación
# El JAR se genera cuando se ejecuta: ./mvnw clean package -DskipTests
COPY target/reservApp.jar app.jar

# Expone el puerto donde corre tu aplicación Spring Boot (por defecto 8080, pero en nuestra configuración es 8090)
EXPOSE 8090

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
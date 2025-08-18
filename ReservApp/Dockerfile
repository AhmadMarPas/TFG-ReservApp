# Utilizamos una imagen base de OpenJDK 21
FROM openjdk:21-jdk-slim
FROM eclipse-temurin:21-jdk AS builde

# Establecemos el directorio de trabajo en el contenedor
WORKDIR /app

# Copiamos el código de la aplicación al contenedor
COPY . .

# Damos los permisos necesarios al script mvnw para que sea ejecutable
RUN chmod +x mvnw

# Ejecutamos Maven para compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Ejecutamos la aplicación y la empaquetamos en un JAR
FROM eclipse-temurin:21-jre

# Establecemos el directorio de trabajo en el contenedor
WORKDIR /app


# Copia el archivo JAR de tu aplicación
# El JAR se genera cuando se ejecuta: ./mvnw clean package -DskipTests
COPY --from=builder /app/target/*.jar app.jar

# Expone el puerto donde corre tu aplicación Spring Boot (por defecto 8080, pero en nuestra configuración es 8090)
EXPOSE 8090

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
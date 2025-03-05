# Usa una imagen base de OpenJDK 17
FROM openjdk:17-jdk-slim

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el archivo .jar generado en target/
COPY target/atendi-0.0.1-SNAPSHOT.jar backend.jar

# Exponer el puerto de la aplicación (asegúrate de que coincida con el de tu Spring Boot)
EXPOSE 8080

# Ejecutar la aplicación
CMD ["java", "-jar", "backend.jar"]

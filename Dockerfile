# 🔥 Usa la imagen base de OpenJDK 17
FROM openjdk:17-jdk-slim

# 🔥 Instalar CUPS y bibliotecas necesarias para impresión
RUN apt-get update && \
    apt-get install -y cups libcups2 cups-client && \
    rm -rf /var/lib/apt/lists/*

# 🔥 Configurar CUPS para permitir accesos
RUN echo "ServerName localhost" > /etc/cups/client.conf && \
    cupsctl --remote-admin --remote-any --share-printers

# 🔥 Copiar el archivo JAR generado en target/
WORKDIR /app
COPY target/atendi-0.0.1-SNAPSHOT.jar backend.jar

# 🔥 Configurar permisos para CUPS (puertos y usuarios)
RUN usermod -aG lpadmin root && chmod -R 777 /etc/cups

# 🔥 Exponer puertos: 
# 8080 para la API de Spring Boot
# 631 para el servicio CUPS
EXPOSE 8080 631

# 🔥 Comando para iniciar CUPS y la aplicación Java
CMD service cups start && java -jar backend.jar

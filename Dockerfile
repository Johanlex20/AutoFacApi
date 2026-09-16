
# ============================
#  Etapa 1: Build del proyecto
# ============================

FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
# Copiamos solo el POM primero (para cachear dependencias)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y compilamos
COPY  src ./src
RUN mvn clean package -DskipTests


# ============================
#  Etapa 2: Imagen final (runtime)
# ============================
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Chromium + su driver, necesarios para la sincronizacion automatica de facturas Siigo (Selenium headless).
# Se usa la variante Alpine de la imagen porque en la variante Ubuntu el paquete "chromium" es solo
# un stub que exige snap, y snap no funciona dentro de contenedores (probado y confirmado).
RUN apk add --no-cache chromium chromium-chromedriver

ENV CHROME_BIN=/usr/bin/chromium
ENV CHROMEDRIVER_BIN=/usr/bin/chromedriver

# Copiamos solo el JAR compilado desde la etapa anterior
COPY --from=build /app/target/AutoFacApi-0.0.1-SNAPSHOT.war app.jar

# Puerto por defecto
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

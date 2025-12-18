# --- ETAPA 1: Compilación (Build) ---
# Usamos el JDK completo para poder compilar el código fuente
FROM eclipse-temurin:21-jdk-jammy AS build

# Definimos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos los archivos de Maven primero para aprovechar la caché de Docker
# Si no cambias el pom.xml, Docker no volverá a descargar las dependencias
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# 2. LE DAS EL PERMISO (La solución)
RUN chmod +x mvnw

# Descargamos las dependencias (esto ahorra tiempo en futuros builds)
RUN ./mvnw dependency:go-offline

# Copiamos el código fuente de tu proyecto
COPY src ./src

# Compilamos y generamos el archivo JAR (saltamos los tests para agilizar)
RUN ./mvnw clean package -DskipTests

# --- ETAPA 2: Ejecución (Runtime) ---
# Usamos el JRE, que es mucho más ligero que el JDK (solo para correr la app)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copiamos solo el JAR generado en la etapa anterior
# El nombre debe coincidir con: artifactId-version.jar de tu pom.xml
COPY --from=build /app/target/circuito-manejo-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto donde corre Spring Boot
EXPOSE 8080

# Comando para arrancar la aplicación cuando el contenedor inicie
ENTRYPOINT ["java", "-jar", "app.jar"]
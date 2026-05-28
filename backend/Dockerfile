# =========================================================================
# FASE 1: Compilación del Backend (JDK 25 nativo + Maven manual)
# =========================================================================
FROM eclipse-temurin:25-jdk-jammy AS builder

# Instalamos Maven manualmente usando el gestor de paquetes de Ubuntu/Debian (Jammy)
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copiamos solo el pom.xml para descargar las dependencias primero (aprovecha la caché)
COPY pom.xml ./
RUN mvn dependency:resolve -B

# Copiamos el código fuente del proyecto
COPY src ./src

# Compilamos el archivo JAR saltándonos los tests
RUN mvn clean package -DskipTests

# =========================================================================
# FASE 2: Entorno de Ejecución Ligero y Seguro (¡Mantenemos tu JRE 25!)
# =========================================================================
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Copiamos el artefacto .jar generado en la fase anterior
COPY --from=builder /app/target/*.jar app.jar

# Exponemos el puerto de escucha nativo de Spring Boot
EXPOSE 8080

# Comando de arranque por defecto de la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
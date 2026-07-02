# Etapa 1: Compilación del microservicio con Gradle
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle bootJar -x test --no-daemon

# Etapa 2: Imagen ligera solo con JRE para ejecutar el JAR
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario sin privilegios por seguridad
RUN addgroup -S bank && adduser -S bank -G bank
USER bank

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# Perfil prod activado por defecto en contenedor (PostgreSQL)
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]

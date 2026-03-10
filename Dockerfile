# --- Etapa 1: Construção (Build) ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia tudo
COPY . .

# Gera o .jar (O nome será 'vidaplus.jar' por causa do pom.xml)
RUN mvn clean package -DskipTests

# --- Etapa 2: Execução (Run) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia apenas o resultado da etapa anterior
COPY --from=build /app/target/vidaplus.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
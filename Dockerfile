# Usa o Java 21 Leve (para rodar)
FROM eclipse-temurin:21-jre-alpine

# Define a pasta de trabalho
WORKDIR /app

# COPIA O ARQUIVO QUE VOCÊ JÁ CRIOU NO TERMINAL
# (Isso garante que o código novo vai para o ar)
COPY target/helpdesk.jar app.jar

# Libera a porta
EXPOSE 8080

# Roda o aplicativo
ENTRYPOINT ["java", "-jar", "app.jar"]
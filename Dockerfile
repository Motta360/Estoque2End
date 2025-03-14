# Estágio de build
FROM ubuntu:latest AS build

# Instala dependências necessárias
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    maven

# Copia o código-fonte para o contêiner
COPY . .

# Compila o projeto com Maven
RUN mvn clean install

# Estágio de execução
FROM eclipse-temurin:17

# Expõe a porta 8080
EXPOSE 8080

# Copia o arquivo JAR gerado no estágio de build
COPY --from=build target/estoqueAPI-0.0.1-SNAPSHOT.jar app.jar

# Define o comando de execução
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "app.jar"]
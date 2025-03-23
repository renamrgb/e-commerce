FROM gradle:7.6.1-jdk17 AS build

WORKDIR /app

# Copiar apenas os arquivos de dependência para aproveitar o cache do Docker
COPY build.gradle.kts settings.gradle.kts ./

# Baixar todas as dependências
RUN gradle dependencies --no-daemon

# Copiar o código-fonte
COPY src ./src

# Compilar o projeto
RUN gradle build --no-daemon -x test

# Imagem final
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Diretório para armazenar dados de tempo de execução
RUN mkdir -p /app/logs

# Variáveis de ambiente
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE="prod"

# Copiar o JAR construído
COPY --from=build /app/build/libs/*.jar app.jar

# Expor porta
EXPOSE 8085

# Comando de execução
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 
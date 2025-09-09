# Stage 1: Build da aplicação
FROM openjdk:21-jdk-slim AS build

# Instalar Maven e dependências de build
RUN apt-get update && apt-get install -y maven git \
    && rm -rf /var/lib/apt/lists/*

# Criar diretório de trabalho
WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Dar permissão de execução ao Maven wrapper
RUN chmod +x mvnw

# Baixar dependências (cache layer otimizado)
RUN ./mvnw dependency:resolve dependency:resolve-sources -B

# Copiar código fonte
COPY src ./src

# Construir a aplicação
RUN ./mvnw package -DskipTests

# Stage 2: Imagem final
FROM openjdk:21-jdk-slim

# Informações do container
LABEL maintainer="OD46S Team"
LABEL description="Backend do Sistema de Coleta de Lixo Urbano com Liquibase"
LABEL version="2.0.0"

# Instalar dependências runtime
RUN apt-get update && apt-get install -y curl \
    && rm -rf /var/lib/apt/lists/*

# Criar usuário não-root para segurança
RUN groupadd --system springboot && useradd --system --gid springboot springboot
USER springboot

# Definir diretório de trabalho
WORKDIR /app

# Copiar JAR construído do stage anterior
COPY --from=build /app/target/backend-*.jar app.jar

# Copiar propriedades específicas do Docker
COPY src/main/resources/application-docker.properties ./config/application.properties

# Expor porta da aplicação
EXPOSE 8080

# Health check otimizado
HEALTHCHECK --interval=30s --timeout=10s --retries=5 CMD curl --fail http://localhost:8080/actuator/health || exit 1

# Executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]

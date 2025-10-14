# DOCKERFILE
FROM openjdk:21-jdk-slim AS build

# Metadados
LABEL stage=builder
LABEL description="Build stage for OD46S Backend"

# Configurar diretório de trabalho
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Dar permissão de execução ao Maven wrapper
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B --no-transfer-progress \
    && ./mvnw dependency:resolve-sources -B --no-transfer-progress

COPY src ./src

RUN ./mvnw clean package -DskipTests -B --no-transfer-progress -q

# ==========================================
# STAGE 2: Imagem final (PRODUÇÃO)
# ==========================================
FROM openjdk:21-jdk-slim

# Metadados da aplicação
LABEL maintainer="OD46S Team"
LABEL description="Sistema de Coleta de Lixo Urbano - Backend Otimizado"
LABEL version="2.1.0"
LABEL app="od46s-backend"

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean \
    && rm -rf /var/cache/apt/*

RUN groupadd --system --gid 1000 springboot \
    && useradd --system --uid 1000 --gid springboot --shell /bin/false springboot

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/logs /app/uploads /app/config \
    && chown -R springboot:springboot /app

COPY src/main/resources/application.properties ./config/application-default.properties

# Script para escolher configuração baseada no ambiente
RUN echo '#!/bin/bash\n\
cp /app/config/application-default.properties /app/application.properties\n\
exec "$@"' > /app/entrypoint.sh \
    && chmod +x /app/entrypoint.sh \
    && chown springboot:springboot /app/entrypoint.sh

# Trocar para usuário não-root
USER springboot

# Expor porta da aplicação
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["/app/entrypoint.sh", "java", \
    "-server", \
    "-XX:+UseContainerSupport", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseStringDeduplication", \
    "-XX:+OptimizeStringConcat", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default}", \
    "-jar", "app.jar"]

# ==========================================
# VARIÁVEIS DE AMBIENTE OPCIONAIS
# ==========================================
ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE="default" \
    SERVER_PORT=8080

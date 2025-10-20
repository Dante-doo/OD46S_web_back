#!/usr/bin/env bash
set -euo pipefail

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔄 Iniciando reset do ambiente...${NC}"

# Carregar configurações se o arquivo existir
if [ -f ".env" ]; then
    echo -e "${GREEN}📁 Carregando configurações de .env...${NC}"
    set -a
    source .env
    set +a
    echo -e "${GREEN}✅ Configurações carregadas!${NC}"
    echo -e "${BLUE}📊 Configurações principais:${NC}"
    echo -e "   🗄️  Banco: ${DB_HOST:-localhost}:${DB_PORT:-5432}/${DB_NAME:-od46s_db_dev}"
    echo -e "   🚀 App: ${APP_NAME:-OD46S Backend} na porta ${APP_PORT:-8080}"
    echo -e "   🔧 Profile: ${APP_PROFILE:-default}"
else
    echo -e "${YELLOW}⚠️ Arquivo .env não encontrado, usando valores padrão${NC}"
fi

echo -e "${YELLOW}🛑 Parando containers...${NC}"
docker-compose down -v || true

echo -e "${YELLOW}🧹 Removendo volumes e imagens antigas...${NC}"
docker volume prune -f || true
docker image prune -f || true

# Remover volumes específicos do projeto
echo -e "${YELLOW}🗑️ Removendo volumes específicos do projeto...${NC}"
docker volume rm -f od46s_web_back_postgres_data od46s_web_back_backend_logs od46s_web_back_backend_uploads 2>/dev/null || true

echo -e "${GREEN}🏗️ Reconstruindo e iniciando stack...${NC}"
docker-compose up -d --build postgres

echo -e "${BLUE}⏳ Aguardando PostgreSQL ficar saudável...${NC}"
timeout=60
counter=0
while ! docker-compose exec postgres pg_isready -U ${DB_USER:-od46s_user} -d ${DB_NAME:-od46s_db_dev} >/dev/null 2>&1; do
    if [ $counter -ge $timeout ]; then
        echo -e "${RED}❌ Timeout aguardando PostgreSQL!${NC}"
        echo -e "${YELLOW}📋 Logs do PostgreSQL:${NC}"
        docker-compose logs postgres --tail=10
        exit 1
    fi
    echo -e "${YELLOW}⏳ Aguardando PostgreSQL... ($counter/$timeout)${NC}"
    sleep 2
    counter=$((counter + 2))
done

echo -e "${GREEN}✅ PostgreSQL está saudável!${NC}"

echo -e "${GREEN}🚀 Iniciando backend...${NC}"
docker-compose up -d --build backend

echo -e "${BLUE}⏳ Aguardando backend ficar saudável...${NC}"
timeout=120
counter=0
while ! curl -sf http://127.0.0.1:${APP_PORT:-8080}/actuator/health >/dev/null 2>&1; do
    if [ $counter -ge $timeout ]; then
        echo -e "${RED}❌ Timeout aguardando backend!${NC}"
        echo -e "${YELLOW}📋 Logs do backend:${NC}"
        docker-compose logs backend --tail=20
        echo -e "${YELLOW}📋 Status dos containers:${NC}"
        docker-compose ps
        exit 1
    fi
    echo -e "${YELLOW}⏳ Aguardando backend... ($counter/$timeout)${NC}"
    sleep 3
    counter=$((counter + 3))
done

echo -e "${GREEN}✅ Backend está saudável!${NC}"

echo -e "${GREEN}🎉 Reset concluído com sucesso!${NC}"
echo -e "${BLUE}📊 Status dos containers:${NC}"
docker-compose ps

echo -e "${BLUE}🌐 URLs disponíveis:${NC}"
echo -e "   🚀 Backend: http://localhost:${APP_PORT:-8080}"
echo -e "   📚 Swagger: http://localhost:${APP_PORT:-8080}/swagger-ui.html"
echo -e "   ❤️ Health: http://localhost:${APP_PORT:-8080}/actuator/health"
echo -e "   🗄️ PostgreSQL: localhost:${DB_PORT:-5432}"

echo -e "${GREEN}✅ Ambiente resetado e funcionando!${NC}"
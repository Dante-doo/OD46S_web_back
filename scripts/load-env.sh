#!/bin/bash

# ===========================================
# SCRIPT PARA CARREGAR CONFIGURAÇÕES CENTRALIZADAS
# ===========================================

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔧 Carregando configurações centralizadas...${NC}"

# Verificar se o arquivo de configuração existe
if [ ! -f ".env" ]; then
    echo -e "${RED}❌ Arquivo .env não encontrado!${NC}"
    echo -e "${YELLOW}💡 Execute este script a partir do diretório raiz do projeto.${NC}"
    exit 1
fi

# Carregar variáveis do arquivo .env
echo -e "${GREEN}📁 Carregando variáveis de .env...${NC}"
set -a
source .env
set +a

# Verificar se as variáveis principais foram carregadas
if [ -z "$DB_HOST" ] || [ -z "$DB_USER" ] || [ -z "$APP_PORT" ]; then
    echo -e "${RED}❌ Erro ao carregar variáveis de ambiente!${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Configurações carregadas com sucesso!${NC}"
echo -e "${BLUE}📊 Configurações principais:${NC}"
echo -e "   🗄️  Banco: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo -e "   🚀 App: ${APP_NAME} na porta ${APP_PORT}"
echo -e "   🔧 Profile: ${APP_PROFILE}"

# Função para executar comandos com as variáveis carregadas
run_with_env() {
    echo -e "${YELLOW}🚀 Executando: $@${NC}"
    "$@"
}

# Função para debug da aplicação
debug_app() {
    echo -e "${GREEN}🐛 Iniciando aplicação em modo debug...${NC}"
    echo -e "${BLUE}💡 Debug será executado na porta 5005${NC}"
    echo -e "${BLUE}💡 Conecte seu debugger em localhost:5005${NC}"
    run_with_env ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
}

# Função para debug com suspensão
debug_suspend() {
    echo -e "${GREEN}🐛 Iniciando aplicação em modo debug (suspenso)...${NC}"
    echo -e "${BLUE}💡 Debug será executado na porta 5005 (suspenso)${NC}"
    echo -e "${BLUE}💡 Conecte seu debugger em localhost:5005 e continue${NC}"
    run_with_env ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
}

# Função para debug do Docker
debug_docker() {
    echo -e "${GREEN}🐳 Iniciando debug do Docker...${NC}"
    echo -e "${BLUE}💡 Backend será executado com debug na porta 5005${NC}"
    run_with_env docker-compose -f docker-compose.yml -f docker-compose.debug.yml up -d
}

# Função para debug dos testes
debug_test() {
    echo -e "${GREEN}🧪 Executando testes em modo debug...${NC}"
    echo -e "${BLUE}💡 Testes serão executados com debug na porta 5005${NC}"
    run_with_env ./mvnw test -Dmaven.surefire.debug
}

# Função para mostrar ajuda
show_help() {
    echo -e "${BLUE}📖 Uso do script:${NC}"
    echo -e "   ./scripts/load-env.sh [comando]"
    echo -e ""
    echo -e "${BLUE}📋 Comandos disponíveis:${NC}"
    echo -e "   ${GREEN}dev${NC}           - Executar aplicação em modo desenvolvimento"
    echo -e "   ${GREEN}docker${NC}        - Executar com Docker Compose"
    echo -e "   ${GREEN}test${NC}          - Executar testes"
    echo -e "   ${GREEN}build${NC}         - Build da aplicação"
    echo -e "   ${GREEN}clean${NC}         - Limpar, rebuild e recriar DB (remove volumes)"
    echo -e "   ${GREEN}reset${NC}         - Reset completo (limpa volumes e rebuild)"
    echo -e "   ${GREEN}logs${NC}          - Ver logs do Docker"
    echo -e "   ${GREEN}stop${NC}          - Parar containers (mantém volumes)"
    echo -e "   ${GREEN}down${NC}          - Parar e remover containers (mantém volumes)"
    echo -e "   ${GREEN}down-volumes${NC}  - Parar e remover containers + volumes"
    echo -e "   ${GREEN}debug${NC}         - Debug da aplicação (porta 5005)"
    echo -e "   ${GREEN}debug-suspend${NC} - Debug suspenso (porta 5005)"
    echo -e "   ${GREEN}docker-debug${NC}  - Debug do Docker"
    echo -e "   ${GREEN}test-debug${NC}    - Debug dos testes"
    echo -e "   ${GREEN}coverage${NC}      - Gerar relatório de cobertura"
    echo -e "   ${GREEN}check${NC}         - Verificar configurações do .env"
    echo -e "   ${GREEN}help${NC}          - Mostrar esta ajuda"
    echo -e ""
    echo -e "${BLUE}💡 Exemplos:${NC}"
    echo -e "   ./scripts/load-env.sh dev          # Rodar localmente"
    echo -e "   ./scripts/load-env.sh docker       # Rodar com Docker"
    echo -e "   ./scripts/load-env.sh clean        # Rebuild com DB limpo"
    echo -e "   ./scripts/load-env.sh reset        # Reset completo (cuidado!)"
    echo -e "   ./scripts/load-env.sh logs         # Ver logs em tempo real"
    echo -e ""
    echo -e "${YELLOW}⚠️  Comandos 'clean', 'reset' e 'down-volumes' removem o banco de dados!${NC}"
}

# Processar argumentos
case "${1:-help}" in
    "dev")
        echo -e "${GREEN}🚀 Iniciando aplicação em modo desenvolvimento...${NC}"
        run_with_env ./mvnw spring-boot:run -Dspring.profiles.active=default
        ;;
    "docker")
        echo -e "${GREEN}🐳 Iniciando com Docker Compose...${NC}"
        run_with_env docker-compose up -d
        echo -e "${GREEN}✅ Containers iniciados!${NC}"
        echo -e "${BLUE}📊 Status:${NC}"
        run_with_env docker-compose ps
        ;;
    "test")
        echo -e "${GREEN}🧪 Executando testes...${NC}"
        run_with_env ./mvnw test
        ;;
    "build")
        echo -e "${GREEN}🔨 Build da aplicação...${NC}"
        run_with_env ./mvnw clean package -DskipTests
        ;;
    "clean")
        echo -e "${GREEN}🧹 Limpando e rebuildando...${NC}"
        echo -e "${YELLOW}⚠️  Isso irá remover todos os volumes (banco de dados será limpo)${NC}"
        run_with_env ./mvnw clean package -DskipTests
        run_with_env docker-compose down -v
        run_with_env docker-compose up --build -d
        echo -e "${GREEN}✅ Aplicação limpa e recriada com sucesso!${NC}"
        ;;
    "logs")
        echo -e "${GREEN}📋 Mostrando logs...${NC}"
        run_with_env docker-compose logs -f
        ;;
    "reset")
        echo -e "${RED}🔄 RESET COMPLETO DO AMBIENTE${NC}"
        echo -e "${YELLOW}⚠️  Isso irá remover TODOS os volumes (dados do banco serão perdidos!)${NC}"
        echo -e "${YELLOW}⚠️  Aguarde 5 segundos para cancelar (Ctrl+C)...${NC}"
        sleep 5
        echo -e "${GREEN}🧹 Limpando projeto Maven...${NC}"
        run_with_env ./mvnw clean package -DskipTests
        echo -e "${GREEN}🐳 Removendo containers e volumes...${NC}"
        run_with_env docker-compose down -v
        echo -e "${GREEN}🔨 Reconstruindo e iniciando...${NC}"
        run_with_env docker-compose up --build -d
        echo -e "${GREEN}✅ Reset completo concluído!${NC}"
        echo -e "${BLUE}📊 Status dos containers:${NC}"
        run_with_env docker-compose ps
        ;;
    "stop")
        echo -e "${GREEN}🛑 Parando containers (volumes mantidos)...${NC}"
        run_with_env docker-compose stop
        ;;
    "down")
        echo -e "${GREEN}🛑 Parando e removendo containers (volumes mantidos)...${NC}"
        run_with_env docker-compose down
        ;;
    "down-volumes")
        echo -e "${RED}🛑 Parando e removendo containers + volumes${NC}"
        echo -e "${YELLOW}⚠️  Dados do banco serão perdidos!${NC}"
        run_with_env docker-compose down -v
        ;;
    "debug")
        debug_app
        ;;
    "debug-suspend")
        debug_suspend
        ;;
    "docker-debug")
        debug_docker
        ;;
    "test-debug")
        debug_test
        ;;
    "coverage")
        echo -e "${GREEN}📊 Gerando relatório de cobertura...${NC}"
        run_with_env ./scripts/coverage-report.sh
        ;;
    "check")
        echo -e "${GREEN}🔍 Verificando configurações do .env...${NC}"
        run_with_env ./scripts/check-env.sh
        ;;
    "help"|*)
        show_help
        ;;
esac

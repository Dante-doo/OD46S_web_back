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
    echo -e "   ${GREEN}dev${NC}          - Executar aplicação em modo desenvolvimento"
    echo -e "   ${GREEN}docker${NC}       - Executar com Docker Compose"
    echo -e "   ${GREEN}test${NC}          - Executar testes"
    echo -e "   ${GREEN}build${NC}        - Build da aplicação"
    echo -e "   ${GREEN}clean${NC}         - Limpar e rebuild"
    echo -e "   ${GREEN}logs${NC}          - Ver logs do Docker"
    echo -e "   ${GREEN}stop${NC}          - Parar containers"
    echo -e "   ${GREEN}debug${NC}        - Debug da aplicação (porta 5005)"
    echo -e "   ${GREEN}debug-suspend${NC} - Debug suspenso (porta 5005)"
    echo -e "   ${GREEN}docker-debug${NC}  - Debug do Docker"
    echo -e "   ${GREEN}test-debug${NC}  - Debug dos testes"
    echo -e "   ${GREEN}coverage${NC}     - Gerar relatório de cobertura"
    echo -e "   ${GREEN}check${NC}         - Verificar configurações do .env"
    echo -e "   ${GREEN}help${NC}          - Mostrar esta ajuda"
    echo -e ""
    echo -e "${BLUE}💡 Exemplos:${NC}"
    echo -e "   ./scripts/load-env.sh dev"
    echo -e "   ./scripts/load-env.sh docker"
    echo -e "   ./scripts/load-env.sh test"
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
        run_with_env ./mvnw clean package -DskipTests
        run_with_env docker-compose down
        run_with_env docker-compose up --build -d
        ;;
    "logs")
        echo -e "${GREEN}📋 Mostrando logs...${NC}"
        run_with_env docker-compose logs -f
        ;;
    "stop")
        echo -e "${GREEN}🛑 Parando containers...${NC}"
        run_with_env docker-compose down
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

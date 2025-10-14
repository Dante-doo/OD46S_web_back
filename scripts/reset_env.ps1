Param()

# Configurações
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

# Cores para output
$Red = "`e[31m"
$Green = "`e[32m"
$Yellow = "`e[33m"
$Blue = "`e[34m"
$Reset = "`e[0m"

Write-Host "${Blue}🔄 Iniciando reset do ambiente...${Reset}"

# Carregar configurações se o arquivo existir
if (Test-Path ".env") {
    Write-Host "${Green}📁 Carregando configurações de .env...${Reset}"
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^([^#][^=]+)=(.*)$") {
            [Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
        }
    }
    Write-Host "${Green}✅ Configurações carregadas!${Reset}"
    Write-Host "${Blue}📊 Configurações principais:${Reset}"
    $dbHost = if ($env:DB_HOST) { $env:DB_HOST } else { 'localhost' }
    $dbPort = if ($env:DB_PORT) { $env:DB_PORT } else { '5432' }
    $dbName = if ($env:DB_NAME) { $env:DB_NAME } else { 'od46s_db_dev' }
    $appName = if ($env:APP_NAME) { $env:APP_NAME } else { 'OD46S Backend' }
    $appPort = if ($env:APP_PORT) { $env:APP_PORT } else { '8080' }
    $appProfile = if ($env:APP_PROFILE) { $env:APP_PROFILE } else { 'default' }
    
    Write-Host "   🗄️  Banco: ${dbHost}:${dbPort}/${dbName}"
    Write-Host "   🚀 App: ${appName} na porta ${appPort}"
    Write-Host "   🔧 Profile: ${appProfile}"
} else {
    Write-Host "${Yellow}⚠️ Arquivo .env não encontrado, usando valores padrão${Reset}"
}

Write-Host "${Yellow}🛑 Parando containers...${Reset}"
try {
    docker-compose down -v | Out-Null
} catch {
    Write-Host "${Yellow}⚠️ Aviso: Erro ao parar containers${Reset}"
}

Write-Host "${Yellow}🧹 Removendo volumes e imagens antigas...${Reset}"
try {
    docker volume prune -f | Out-Null
    docker image prune -f | Out-Null
} catch {
    Write-Host "${Yellow}⚠️ Aviso: Erro ao limpar volumes/imagens${Reset}"
}

# Remover volumes específicos do projeto
Write-Host "${Yellow}🗑️ Removendo volumes específicos do projeto...${Reset}"
$volumes = @("od46s_web_back_postgres_data", "od46s_web_back_backend_logs", "od46s_web_back_backend_uploads")
foreach ($volume in $volumes) {
    try {
        docker volume rm -f $volume | Out-Null
    } catch {
        # Ignorar erros de volumes que não existem
    }
}

Write-Host "${Green}🏗️ Reconstruindo e iniciando stack...${Reset}"
try {
    docker-compose up -d --build postgres | Out-Null
} catch {
    Write-Host "${Red}❌ Erro ao iniciar PostgreSQL${Reset}"
    exit 1
}

Write-Host "${Blue}⏳ Aguardando PostgreSQL ficar saudável...${Reset}"
$timeout = 60
$counter = 0
$dbUser = if ($env:DB_USER) { $env:DB_USER } else { "od46s_user" }
$dbName = if ($env:DB_NAME) { $env:DB_NAME } else { "od46s_db_dev" }

while ($counter -lt $timeout) {
    try {
        $result = docker-compose exec postgres pg_isready -U $dbUser -d $dbName 2>$null
        if ($LASTEXITCODE -eq 0) {
            break
        }
    } catch {}
    
    Write-Host "${Yellow}⏳ Aguardando PostgreSQL... ($counter/$timeout)${Reset}"
    Start-Sleep -Seconds 2
    $counter += 2
}

if ($counter -ge $timeout) {
    Write-Host "${Red}❌ Timeout aguardando PostgreSQL!${Reset}"
    Write-Host "${Yellow}📋 Logs do PostgreSQL:${Reset}"
    docker-compose logs postgres --tail=10
    exit 1
}

Write-Host "${Green}✅ PostgreSQL está saudável!${Reset}"

Write-Host "${Green}🚀 Iniciando backend...${Reset}"
try {
    docker-compose up -d --build backend | Out-Null
} catch {
    Write-Host "${Red}❌ Erro ao iniciar backend${Reset}"
    exit 1
}

Write-Host "${Blue}⏳ Aguardando backend ficar saudável...${Reset}"
$timeout = 120
$counter = 0
$appPort = if ($env:APP_PORT) { $env:APP_PORT } else { "8080" }

while ($counter -lt $timeout) {
    try {
        $resp = Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:$appPort/actuator/health" -TimeoutSec 3 -ErrorAction SilentlyContinue
        if ($resp.StatusCode -eq 200) {
            break
        }
    } catch {}
    
    Write-Host "${Yellow}⏳ Aguardando backend... ($counter/$timeout)${Reset}"
    Start-Sleep -Seconds 3
    $counter += 3
}

if ($counter -ge $timeout) {
    Write-Host "${Red}❌ Timeout aguardando backend!${Reset}"
    Write-Host "${Yellow}📋 Logs do backend:${Reset}"
    docker-compose logs backend --tail=20
    Write-Host "${Yellow}📋 Status dos containers:${Reset}"
    docker-compose ps
    exit 1
}

Write-Host "${Green}✅ Backend está saudável!${Reset}"

Write-Host "${Green}🎉 Reset concluído com sucesso!${Reset}"
Write-Host "${Blue}📊 Status dos containers:${Reset}"
docker-compose ps

Write-Host "${Blue}🌐 URLs disponíveis:${Reset}"
Write-Host "   🚀 Backend: http://localhost:$appPort"
Write-Host "   📚 Swagger: http://localhost:$appPort/swagger-ui.html"
Write-Host "   ❤️ Health: http://localhost:$appPort/actuator/health"
Write-Host "   🗄️ PostgreSQL: localhost:5432"

Write-Host "${Green}✅ Ambiente resetado e funcionando!${Reset}"
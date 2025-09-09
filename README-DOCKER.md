# 🐳 Docker - Sistema OD46S

## 📋 Visão Geral

Configuração Docker completa para o Sistema OD46S de Coleta de Lixo Urbano com:
- **Backend Spring Boot** (Java 21)
- **PostgreSQL 15** (banco de dados)
- **pgAdmin** (opcional - interface web para o banco)

## 🚀 Como Executar

### Pré-requisitos
- Docker 20+
- Docker Compose 2+

### 1. **Executar Ambiente Completo**
```bash
# Clonar o repositório
git clone <url-do-repositorio>
cd OD46S_web_back

# Subir todos os serviços
docker-compose up -d

# Verificar status
docker-compose ps
```

### 2. **Executar com pgAdmin (opcional)**
```bash
# Subir com interface de administração do banco
docker-compose --profile admin up -d
```

### 3. **Logs dos Serviços**
```bash
# Ver logs do backend
docker-compose logs -f backend

# Ver logs do banco
docker-compose logs -f postgres

# Ver logs de todos os serviços
docker-compose logs -f
```

## 🔧 Configuração dos Serviços

### 📊 **Backend Spring Boot**
- **URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Profile**: docker
- **Logs**: Salvos em volume persistente

### 🗄️ **PostgreSQL**
- **Host**: localhost:5432
- **Database**: od46s_db_dev
- **User**: od46s_user
- **Password**: 1234

### 🖥️ **pgAdmin (opcional)**
- **URL**: http://localhost:5050
- **Email**: admin@od46s.com
- **Password**: admin123

## 📱 Teste da API

### 🔐 **Login Padrão**
```bash
# Admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@od46s.com","password":"admin123"}'

# Motorista  
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"motorista@od46s.com","password":"motorista123"}'
```

### 📊 **Listar Usuários**
```bash
# Usar o token recebido no login
curl -X GET http://localhost:8080/api/usuarios \
  -H "Authorization: Bearer <seu_jwt_token>"
```

### 🚛 **Listar Veículos**
```bash
curl -X GET http://localhost:8080/api/veiculos \
  -H "Authorization: Bearer <seu_jwt_token>"
```

## 🎯 Dados Iniciais

O sistema já vem com dados de exemplo:

### 👥 **Usuários**
| Tipo | Email | Senha | CPF |
|------|-------|-------|-----|
| Admin | admin@od46s.com | admin123 | 12345678901 |
| Motorista | motorista@od46s.com | motorista123 | 98765432100 |

### 🚛 **Veículo**
- **Placa**: ABC1234
- **Modelo**: Compactador 15m³
- **Status**: Disponível

### 🗺️ **Rota**
- **Nome**: Rota Centro Manhã
- **Tipo**: Residencial  
- **Periodicidade**: Segunda, Quarta e Sexta às 6:00
- **1 Ponto de Coleta** no centro

## 🛠️ Comandos Úteis

### 🔄 **Gerenciamento**
```bash
# Parar todos os serviços
docker-compose down

# Parar e remover volumes (CUIDADO: apaga dados)
docker-compose down -v

# Rebuild do backend
docker-compose build backend

# Restart de um serviço específico
docker-compose restart backend

# Ver status detalhado
docker-compose ps -a
```

### 🗄️ **Banco de Dados**
```bash
# Acessar console do PostgreSQL
docker-compose exec postgres psql -U od46s_user -d od46s_db_dev

# Backup do banco
docker-compose exec postgres pg_dump -U od46s_user od46s_db_dev > backup.sql

# Restore do banco
docker-compose exec -T postgres psql -U od46s_user od46s_db_dev < backup.sql
```

### 📊 **Monitoramento**
```bash
# Verificar recursos utilizados
docker stats

# Verificar saúde dos containers
docker-compose exec backend curl -f http://localhost:8080/actuator/health

# Inspecionar rede
docker network inspect od46s_web_back_od46s-network
```

## 🐛 Solução de Problemas

### ❌ **Backend não conecta no banco**
```bash
# Verificar se PostgreSQL está rodando
docker-compose ps postgres

# Verificar logs do banco
docker-compose logs postgres

# Verificar conectividade
docker-compose exec backend ping postgres
```

### ❌ **Porta já está em uso**
```yaml
# Alterar portas no docker-compose.yml
services:
  backend:
    ports:
      - "8081:8080"  # Usar porta 8081 no host
  postgres:
    ports:
      - "5433:5432"  # Usar porta 5433 no host
```

### ❌ **Build falha**
```bash
# Limpar cache do Docker
docker system prune -a

# Rebuild forçado
docker-compose build --no-cache backend
```

### ❌ **Dados não persistem**
```bash
# Verificar volumes
docker volume ls | grep od46s

# Verificar se volumes estão montados
docker-compose exec postgres df -h
```

## 📁 Estrutura dos Volumes

```
volumes/
├── postgres_data/     # Dados do PostgreSQL
├── backend_logs/      # Logs da aplicação
├── backend_uploads/   # Fotos das coletas
└── pgadmin_data/      # Configurações do pgAdmin
```

## 🔒 Segurança

### ⚠️ **Ambiente de Desenvolvimento**
- Senhas simples (admin123, 1234)
- Todas as portas expostas
- Logs detalhados habilitados

### 🛡️ **Para Produção**
1. Alterar senhas no docker-compose.yml
2. Usar secrets do Docker
3. Configurar SSL/TLS
4. Restringir exposição de portas
5. Usar imagens oficiais com tags específicas

## 🚀 Deploy em Produção

```bash
# Usar variáveis de ambiente
export POSTGRES_PASSWORD=senha_segura_producao
export JWT_SECRET=jwt_secret_longo_e_seguro

# Deploy com override para produção
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

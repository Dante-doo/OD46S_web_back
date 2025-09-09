# OD46S - Sistema de Coleta de Lixo Urbano

## 📋 Descrição

Sistema para gestão de coleta de lixo urbano com interface web para administradores e aplicativo mobile para motoristas.

[Especificação do Projeto](https://docs.google.com/document/d/13mvG5-8O9F1I0LOoHcwJ8F5z_uUiGNxA/edit?usp=sharing&ouid=101922106087156697360&rtpof=true&sd=true).

## 🎯 Funcionalidades

**Administradores (Web React):**
- Gestão de motoristas e veículos
- Criação de rotas com pontos de coleta
- Visualização de coletas realizadas
- Relatórios básicos

**Motoristas (Mobile Kotlin):**
- Visualização de rotas do dia
- Registro de coletas (peso, fotos, observações)
- Funcionamento offline com sincronização

## 🏗️ Tecnologias

- **Backend**: Java 21 + Spring Boot 3.5.5 + **Liquibase**
- **Frontend**: React + TypeScript
- **Mobile**: Kotlin Android
- **Banco**: PostgreSQL + SQLite (mobile offline)
- **Autenticação**: JWT + BCrypt

## 🔐 Perfis de Usuário

- **ADMIN**: Acesso completo via web e mobile
- **MOTORISTA**: Acesso limitado via mobile

## 🗄️ Banco de Dados

### 📋 **Liquibase v1.0 - Rollback Gratuito**
O sistema utiliza **Liquibase** para gerenciamento completo do banco de dados com rollback automático:

```
src/main/resources/db/changelog/
├── db.changelog-master.xml          # 🎯 Orquestrador principal
└── v1.0/                           # 🚀 Release consolidada
    ├── 001-setup-database.xml      # 🔧 Configurações PostgreSQL
    ├── 002-create-schema.xml       # 📊 Todas as 9 tabelas
    ├── 003-create-indexes.xml      # ⚡ Otimizações
    ├── 004-create-functions.xml    # 🛠️ Funções utilitárias
    └── 005-insert-initial-data.xml # 📝 Dados exemplo
```

### Configuração PostgreSQL
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/od46s_db_dev
spring.datasource.username=od46s_user
spring.datasource.password=1234
```

### Entidades Principais (9 Tabelas)
- **usuarios** (base para herança)
- **administradores** + **motoristas** (herança)
- **veiculos** + **rotas** + **rota_pontos_coleta**
- **execucoes_rota** + **registros_gps** + **registros_coleta_pontos**

## 🚀 Como Executar

### 🐳 **Opção 1: Docker (Recomendado)**
```bash
# Clone o repositório
git clone <url-do-repositorio>
cd OD46S_web_back

# Execute com Docker Compose
docker-compose up -d

# Verificar status
docker-compose ps
```

### 💻 **Opção 2: Desenvolvimento Local**
**Pré-requisitos:**
- Java 21
- PostgreSQL
- Maven

**Configuração do Banco:**
```sql
CREATE DATABASE od46s_db_dev;
CREATE USER od46s_user WITH PASSWORD '1234';
GRANT ALL PRIVILEGES ON DATABASE od46s_db_dev TO od46s_user;
```

**Executar Aplicação:**
```bash
# O Liquibase criará automaticamente tabelas e dados iniciais
./mvnw spring-boot:run
```

**Comandos Liquibase Úteis:**
```bash
# Rollback (gratuito!)
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1

# Status das migrações
./mvnw liquibase:status

# Ver diferenças
./mvnw liquibase:diff
```

### 🔍 **Verificar Aplicação**
- **URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **PostgreSQL**: localhost:5433 (Docker)
- **pgAdmin** (Docker): http://localhost:5050

### 👥 **Usuários Padrão**
| Tipo | Email | Senha |
|------|-------|-------|
| Admin | admin@od46s.com | admin123 |
| Motorista | motorista@od46s.com | motorista123 |

## 📱 APIs Principais

### Autenticação
```
POST /api/auth/login      # Login de usuário
```

### Administração
```
GET    /api/usuarios      # Listar usuários
POST   /api/usuarios      # Criar usuário
GET    /api/veiculos      # Listar veículos
POST   /api/veiculos      # Criar veículo
GET    /api/rotas         # Listar rotas
POST   /api/rotas         # Criar rota
```

### Mobile
```
GET    /api/rotas/motorista/{id}    # Rotas do motorista
POST   /api/coletas/iniciar         # Iniciar coleta
POST   /api/coletas/{id}/pontos     # Registrar ponto
POST   /api/coletas/{id}/finalizar  # Finalizar coleta
POST   /api/sync/dados              # Sincronizar offline
```

---

**Sistema simples e focado nos requisitos de coleta de lixo urbano.**
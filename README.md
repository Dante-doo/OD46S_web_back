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

- **Backend**: Java 21 + Spring Boot 3.5.5
- **Frontend**: React + TypeScript
- **Mobile**: Kotlin Android
- **Banco**: PostgreSQL + SQLite (mobile offline)
- **Autenticação**: JWT + BCrypt

## 🔐 Perfis de Usuário

- **ADMIN**: Acesso completo via web e mobile
- **MOTORISTA**: Acesso limitado via mobile

## 🗄️ Banco de Dados

### Configuração PostgreSQL
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/od46s_db_dev
spring.datasource.username=od46s_user
spring.datasource.password=1234
```

### Entidades Principais
- **usuarios** (base para herança)
- **administradores**
- **motoristas**
- **veiculos**
- **rotas**
- **pontos_coleta**
- **registros_coleta**

## 🚀 Como Executar

### Pré-requisitos
- Java 21
- PostgreSQL
- Maven

### Configuração do Banco
```sql
CREATE DATABASE od46s_db_dev;
CREATE USER od46s_user WITH PASSWORD '1234';
GRANT ALL PRIVILEGES ON DATABASE od46s_db_dev TO od46s_user;
```

### Executar Aplicação
```bash
# Clone o repositório
git clone <url-do-repositorio>
cd OD46S_web_back

# Execute a aplicação
./mvnw spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

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
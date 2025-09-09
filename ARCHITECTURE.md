# 🏗️ Arquitetura do Sistema OD46S

## 📋 Visão Geral

Sistema simples com três componentes: **Mobile App (Kotlin)**, **Backend (Spring Boot)** e **Frontend Web (React)** para gestão de coleta de lixo urbano.

## 🏛️ Arquitetura de Comunicação

```
┌─────────────────────────────────────────────────────────────────────┐
│                          SISTEMA OD46S                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐  │
│  │   FRONTEND WEB  │    │     BACKEND     │    │   MOBILE APP    │  │
│  │    (React)      │◄──►│  (Spring Boot)  │◄──►│    (Kotlin)     │  │
│  │                 │    │                 │    │                 │  │
│  │ • Dashboard     │    │ • REST APIs     │    │ • Coleta Dados  │  │
│  │ • Relatórios    │    │ • Autenticação  │    │ • GPS Tracking  │  │
│  │ • Gestão Users  │    │ • Validações    │    │ • Modo Offline  │  │
│  │ • Config Rotas  │    │ • Banco Dados   │    │ • Sincronização │  │
│  └─────────────────┘    │ • Lógica Negóc. │    └─────────────────┘  │
│                         └─────────────────┘                        │
│                                  │                                  │
│                                  ▼                                  │
│                         ┌─────────────────┐                        │
│                         │   POSTGRESQL    │                        │
│                         │ + Liquibase v1.0│                        │
│                         │ (Rollback Free) │                        │
│                         └─────────────────┘                        │
└─────────────────────────────────────────────────────────────────────┘

COMUNICAÇÃO:
├── Frontend ↔ Backend: HTTP/REST + JWT Auth
├── Mobile ↔ Backend: HTTP/REST + JWT Auth  
└── Mobile: SQLite Local + Sincronização
```

## 📊 Entidades Principais (9 Tabelas - Liquibase v1.0)

### 👥 **Usuários (Herança)**
- **usuarios** (base): id, nome, email, senha, ativo, data_criacao
- **administradores** (herda): nivel_acesso, setor, telefone_corporativo
- **motoristas** (herda): cnh, categoria_cnh, validade_cnh, habilitado

### 🚛 **Operações**
- **veiculos**: placa, modelo, marca, capacidade_kg, status, tipo_combustivel
- **rotas**: nome, tipo_coleta, periodicidade (cron), prioridade, distancia_km
- **rota_pontos_coleta**: endereco, latitude, longitude, tipo_residuo, ordem_sequencia

### 📊 **Execuções e Registros**
- **execucoes_rota**: data_inicio, data_fim, status, km_inicial, peso_coletado_kg
- **registros_gps**: timestamp_gps, latitude, longitude, velocidade_kmh, status_veiculo
- **registros_coleta_pontos**: timestamp_coleta, peso_coletado_kg, status_coleta, fotos

## 🔗 Componentes do Sistema

### 📱 **Mobile App (Kotlin)**
**Responsabilidade:** Interface para motoristas em campo
- **Autenticação** - Login do motorista
- **Rotas do Dia** - Visualizar rotas atribuídas  
- **Coleta de Dados** - Registrar peso, fotos, observações
- **GPS Tracking** - Rastrear localização durante coleta
- **Modo Offline** - Trabalhar sem internet (SQLite local)
- **Sincronização** - Enviar dados quando online

### 🖥️ **Frontend Web (React)**
**Responsabilidade:** Interface administrativa
- **Gestão de Usuários** - CRUD motoristas e administradores
- **Gestão de Veículos** - Controle da frota
- **Planejamento de Rotas** - Criar e configurar rotas
- **Pontos de Coleta** - Gerenciar locais de coleta
- **Relatórios** - Visualizar dados e estatísticas

### 🖥️ **Backend (Spring Boot + Liquibase)**
**Responsabilidade:** Lógica de negócio e persistência
- **APIs REST** - Endpoints para mobile e web
- **Autenticação JWT** - Controle de acesso
- **Validações de Negócio** - Regras da aplicação
- **Liquibase v1.0** - Migrations com rollback gratuito
- **Persistência** - Gerenciar dados no PostgreSQL
- **Sincronização** - Processar dados do mobile

## 🔄 Fluxo de Comunicação

### 📱 **Mobile → Backend**
```
1. Login: POST /api/auth/login
2. Buscar Rotas: GET /api/rotas/motorista/{id}
3. Iniciar Coleta: POST /api/coletas/iniciar
4. Registrar Ponto: POST /api/coletas/{id}/pontos
5. Finalizar Coleta: POST /api/coletas/{id}/finalizar
6. Sincronizar Offline: POST /api/sync/dados
```

### 🖥️ **Frontend → Backend**
```
1. Login Admin: POST /api/auth/login
2. Dashboard: GET /api/dashboard/resumo
3. Gestão Users: GET/POST/PUT/DELETE /api/usuarios
4. Gestão Veículos: GET/POST/PUT /api/veiculos
5. Gestão Rotas: GET/POST/PUT /api/rotas
6. Relatórios: GET /api/relatorios/{tipo}
```

### 🛡️ **Segurança**
- **JWT Token** para autenticação
- **Roles**: ADMIN, MOTORISTA
- **BCrypt** para senhas
- **HTTPS** obrigatório em produção

## 🔄 **Liquibase v1.0 - Database Migration**

### 📋 **Estrutura Consolidada**
```
src/main/resources/db/changelog/
├── db.changelog-master.xml          # 🎯 Orquestrador principal
└── v1.0/                           # 🚀 Release consolidada
    ├── 001-setup-database.xml      # 🔧 Extensões PostgreSQL
    ├── 002-create-schema.xml       # 📊 Todas as 9 tabelas
    ├── 003-create-indexes.xml      # ⚡ Otimizações de performance
    ├── 004-create-functions.xml    # 🛠️ Funções utilitárias
    └── 005-insert-initial-data.xml # 📝 Dados iniciais
```

### ✅ **Benefícios**
- **Rollback Gratuito** - Desfazer migrações automaticamente
- **Versionamento** - Controle completo de mudanças no banco
- **Execução Automática** - Migrations na inicialização da aplicação
- **Validação** - Verificação de integridade dos changesets

## 📱 Modo Offline (Mobile)

- **SQLite Local** no mobile para trabalhar sem internet
- **Fila de Sincronização** para operações pendentes
- **Auto-Sync** quando reconecta à internet
- **Dados Offline**: rotas do dia, registros de coleta, fotos

## 📋 Requisitos Funcionais Essenciais

### 👨‍💼 **Administradores**
- **Login/Logout** no sistema web
- **Gestão de Motoristas** (criar, editar, ativar/desativar)
- **Gestão de Veículos** (cadastrar, editar, status)
- **Criação de Rotas** com pontos de coleta
- **Visualização de Coletas** realizadas
- **Relatórios Simples** (coletas por período, motorista)

### 🚚 **Motoristas**  
- **Login no App Mobile** (Kotlin)
- **Visualizar Rotas** do dia
- **Iniciar/Finalizar Coleta** de rota
- **Registrar Coleta** em cada ponto (peso, fotos, observações)
- **Trabalhar Offline** quando sem internet
- **Sincronizar Dados** quando voltar online

### 🗄️ **Sistema**
- **Autenticação JWT** para ambos os clientes
- **Banco PostgreSQL** para persistência
- **APIs REST** para comunicação
- **Upload de Fotos** das coletas
- **Logs de Auditoria** básicos

## 🚀 Stack Tecnológico Simplificado

- **Backend**: Java 21 + Spring Boot 3.5.5 + **Liquibase**
- **Frontend**: React + TypeScript  
- **Mobile**: Kotlin Android
- **Banco**: PostgreSQL + SQLite (mobile)
- **Autenticação**: JWT + BCrypt

## 📋 Próximos Passos

1. ✅ ~~Implementar entidades~~ - **Concluído com Liquibase v1.0**
2. Desenvolver APIs REST para coleta e sincronização  
3. Criar interfaces React para administração
4. Desenvolver app mobile Kotlin com modo offline
5. Implementar upload de fotos e relatórios

---

**Arquitetura simples e focada nos requisitos de coleta de lixo urbano.**

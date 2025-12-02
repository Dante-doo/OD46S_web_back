# 🗑️ Sistema OD46S - Coleta de Lixo Urbano

Sistema digital integrado para gestão inteligente de coleta de lixo urbano, conectando administradores municipais, motoristas e operações de campo através de aplicações web e mobile.

## 🎯 Intuito do Sistema

O Sistema OD46S foi projetado para modernizar e otimizar a gestão de coleta de lixo urbano através de:

- **Gestão Centralizada**: Dashboard administrativo para controle total das operações
- **Mobilidade em Campo**: App mobile para motoristas com funcionalidades offline
- **Tracking em Tempo Real**: Acompanhamento GPS de rotas e coletas
- **Relatórios Inteligentes**: Analytics e KPIs para tomada de decisão
- **Escalabilidade Municipal**: Suporte a múltiplos tipos de coleta e frotas

## 🛠️ Tecnologias Utilizadas

### Backend
- **Spring Boot 3.5.5** - Framework principal
- **Java 21** - Linguagem de programação
- **PostgreSQL** - Banco de dados relacional
- **Liquibase** - Controle de versão do banco
- **JWT + BCrypt** - Autenticação e segurança
- **Swagger/OpenAPI** - Documentação interativa da API
- **MinIO** - Armazenamento S3-compatible para fotos (eventos GPS)
- **Docker + Docker Compose** - Containerização e orquestração

### Frontend (Planejado)
- **React 18** - Interface web
- **TypeScript** - Tipagem estática
- **Material-UI** - Componentes visuais
- **React Query** - Gerenciamento de estado

### Mobile (Planejado)
- **Kotlin** - App Android nativo
- **SQLite** - Banco local para modo offline
- **Retrofit** - Cliente HTTP
- **Google Maps API** - Mapas e navegação

### DevOps & Hosting
- **Docker** - Containerização
- **GitHub Actions** - CI/CD (2000min/mês gratuito)
- **Nginx** - Proxy reverso
- **Oracle Cloud Always Free** - Hosting gratuito permanente
- **Cloudflare** - CDN gratuito
- **Let's Encrypt** - SSL gratuito

## ⚙️ Configuração Centralizada

O sistema utiliza um arquivo de configuração centralizado (`.env`) para gerenciar todas as variáveis de ambiente.

### 📁 Arquivo de Configuração
- `.env` - Configurações centralizadas (único arquivo)
- `env.example` - Arquivo de exemplo

### 🚀 Script de Automação
```bash
# Carregar configurações e executar comandos
./scripts/load-env.sh [comando]

# Comandos disponíveis:
./scripts/load-env.sh dev           # Desenvolvimento local
./scripts/load-env.sh docker        # Docker Compose
./scripts/load-env.sh test          # Executar testes
./scripts/load-env.sh build         # Build da aplicação
./scripts/load-env.sh clean         # Limpar e rebuild (remove volumes)
./scripts/load-env.sh reset         # Reset completo (limpa tudo)
./scripts/load-env.sh logs          # Ver logs em tempo real
./scripts/load-env.sh stop          # Parar containers (mantém volumes)
./scripts/load-env.sh down          # Parar e remover containers
./scripts/load-env.sh down-volumes  # Parar e remover containers + volumes
```

### 🔧 Configuração Inicial
```bash
# 1. Copiar arquivo de exemplo
cp env.example .env

# 2. Editar configurações conforme necessário
nano .env

# 3. Executar com configurações centralizadas
./scripts/load-env.sh docker
```

### 🔧 Troubleshooting

**Erro de Liquibase (checksum validation failed):**
```bash
# Solução: Limpar o banco de dados e recriar
./scripts/load-env.sh clean

# Ou reset completo (mais seguro)
./scripts/load-env.sh reset
```

**Diferença entre os comandos:**
- `stop` - Para containers, mantém tudo (volumes, networks)
- `down` - Remove containers e networks, mantém volumes (banco preservado)
- `down-volumes` - Remove tudo incluindo volumes (⚠️ apaga banco!)
- `clean` - Build + down-volumes + up (resolve problemas de Liquibase)
- `reset` - Igual ao clean, mas com aviso de 5 segundos

## 🐳 Como Executar com Docker

### Pré-requisitos
- Docker 20.0+
- Docker Compose 2.0+

### Execução
```bash
# 1. Clone o repositório
git clone <repository-url>
cd OD46S_web_back

# 2. Configurar ambiente
cp env.example .env

# 3. Iniciar com configuração centralizada
./scripts/load-env.sh docker

# 4. Verificar se está funcionando
curl http://localhost:8080/actuator/health

# 5. Para parar
./scripts/load-env.sh stop
```

### 🎛️ Comandos Alternativos
```bash
# Execução tradicional (ainda funciona)
docker-compose up -d

# Com configuração específica
docker-compose --env-file .env up -d

# Com perfil de administração (inclui pgAdmin)
docker-compose --profile admin up -d
```

### Portas e URLs
- **Backend**: http://localhost:8080
- **PostgreSQL**: localhost:5432
- **MinIO API**: http://localhost:9000
- **MinIO Console**: http://localhost:9001 (login: minioadmin/minioadmin)
- **Health Check**: http://localhost:8080/actuator/health
- **Documentação da API (Swagger)**: http://localhost:8080/swagger-ui/index.html

## 📱 Endpoints Implementados

### Sistema
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/actuator/health` | Health check da aplicação (Actuator) | ✅ Implementado |
| GET | `/api/v1/health` | Health detalhado da API | ✅ Implementado |
| GET | `/health` | Health simples | ✅ Implementado |

### Autenticação
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| POST | `/api/v1/auth/login` | Login com email/cpf + senha | ✅ Implementado |
| POST | `/api/v1/auth/refresh` | Renovar token JWT | ✅ Implementado |
| GET | `/api/v1/auth/health` | Health do serviço de autenticação | ✅ Implementado |

### Gestão de Usuários
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/users` | Listar usuários (paginado) | ✅ Implementado |
| GET | `/api/v1/users/{id}` | Obter usuário específico | ✅ Implementado |
| POST | `/api/v1/users` | Criar novo usuário | ✅ Implementado |
| PUT | `/api/v1/users/{id}` | Atualizar usuário | ✅ Implementado |
| DELETE | `/api/v1/users/{id}` | Remover usuário | ✅ Implementado |

### Gestão de Veículos
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/vehicles` | Listar veículos | ✅ Implementado |
| POST | `/api/v1/vehicles` | Cadastrar veículo | ✅ Implementado |
| PUT | `/api/v1/vehicles/{id}` | Atualizar veículo | ✅ Implementado |
| PATCH | `/api/v1/vehicles/{id}/status` | Alterar status | ✅ Implementado |

### Gestão de Rotas
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/routes` | Listar rotas | ✅ Implementado |
| GET | `/api/v1/routes/{id}` | Obter rota com pontos | ✅ Implementado |
| POST | `/api/v1/routes` | Criar nova rota | ✅ Implementado |
| POST | `/api/v1/routes/{id}/points` | Adicionar ponto à rota | ✅ Implementado |
| PUT | `/api/v1/routes/{id}/points/reorder` | Reordenar pontos | ✅ Implementado |

### Escalas (Assignments) - Cadastro Interligado
> **💡 Conceito**: Vínculo duradouro entre rota, motorista e caminhão

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/assignments` | Listar escalas | ✅ Implementado |
| GET | `/api/v1/assignments/{id}` | Detalhes da escala | ✅ Implementado |
| POST | `/api/v1/assignments` | Criar escala (Admin) | ✅ Implementado |
| PUT | `/api/v1/assignments/{id}` | Atualizar escala | ✅ Implementado |
| PATCH | `/api/v1/assignments/{id}/deactivate` | Desativar escala | ✅ Implementado |
| GET | `/api/v1/assignments/my-current` | Escala do motorista | ✅ Implementado |

### Execuções (Executions) - Coletas Realizadas
> **💡 Conceito**: Registro de uma coleta específica realizada

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/executions` | Histórico de execuções | ✅ Implementado |
| GET | `/api/v1/executions/{id}` | Detalhes da execução | ✅ Implementado |
| POST | `/api/v1/executions/start` | Iniciar coleta (Driver) | ✅ Implementado |
| PATCH | `/api/v1/executions/{id}/complete` | Finalizar coleta | ✅ Implementado |
| PATCH | `/api/v1/executions/{id}/cancel` | Cancelar execução | ✅ Implementado |
| GET | `/api/v1/executions/my-current` | Coleta em andamento | ✅ Implementado |

### GPS Tracking & Eventos
> **💡 Conceito**: Rastreamento em tempo real durante execuções + registro de eventos/ocorrências com fotos

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| POST | `/api/v1/executions/{id}/gps` | Registrar GPS/evento com foto opcional | ✅ Implementado |
| GET | `/api/v1/executions/{id}/gps` | Obter rastro GPS completo | ✅ Implementado |
| GET | `/api/v1/files/gps-photos/{executionId}/{filename}` | Baixar foto de evento | ✅ Implementado |

**Tipos de Eventos Suportados:**
- `START` - Início da coleta
- `NORMAL` - Percurso normal (GPS periódico)
- `STOP` - Parada qualquer
- `BREAK` - Intervalo/Descanso
- `FUEL` - Abastecimento
- `LUNCH` - Almoço
- `PROBLEM` - Problema encontrado
- `OBSERVATION` - Observação
- `PHOTO` - Registro fotográfico
- `END` - Fim da coleta

**Dados Capturados:**
- Latitude/Longitude (obrigatório)
- Velocidade, direção, precisão (opcional)
- Tipo de evento (default: NORMAL)
- Descrição textual (opcional)
- Foto (opcional, max 10MB, JPG/PNG/WebP)
- Timestamp

**Exemplo de Uso:**
```bash
# Registrar parada com problema e foto
POST /api/v1/executions/123/gps
Content-Type: multipart/form-data

latitude=-25.4284
longitude=-49.2733
event_type=PROBLEM
description=Lixeira transbordando, lixo na calçada
photo=@foto_problema.jpg
```

### Registros de Coleta (Planejado)
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| POST | `/api/v1/executions/{id}/collections` | Registrar coleta em ponto | ⏳ Planejado |
| GET | `/api/v1/executions/{id}/collections` | Listar coletas da execução | ⏳ Planejado |

### Relatórios e Analytics (Planejados)
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/analytics/dashboard` | Dashboard com KPIs | ⏳ Planejado |
| GET | `/api/v1/analytics/routes/efficiency` | Eficiência de rotas | ⏳ Planejado |
| GET | `/api/v1/analytics/drivers/performance` | Performance motoristas | ⏳ Planejado |
| GET | `/api/v1/analytics/fleet/utilization` | Utilização da frota | ⏳ Planejado |

### Sincronização Mobile (Planejada)
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/mobile/sync/download` | Download dados offline | ⏳ Planejado |
| POST | `/api/v1/mobile/sync/upload` | Upload dados coletados | ⏳ Planejado |

## 📋 Estrutura do Projeto

```
OD46S_web_back/
├── src/main/java/utfpr/OD46S/backend/
│   ├── BackendApplication.java          # Aplicação principal
│   ├── controllers/                     # Controllers REST
│   │   ├── AuthController.java         # Autenticação
│   │   ├── UsuarioController.java      # Gestão de usuários
│   │   ├── VeiculoController.java      # Gestão de veículos
│   │   ├── RouteController.java        # Gestão de rotas
│   │   └── AssignmentController.java   # Gestão de escalas
│   ├── services/                        # Lógica de negócio
│   │   ├── login/AuthService.java      # Autenticação e JWT
│   │   ├── UsuarioService.java         # Usuários e motoristas
│   │   ├── VeiculoService.java         # Veículos
│   │   ├── RouteService.java           # Rotas e pontos de coleta
│   │   └── AssignmentService.java      # Escalas (rota+motorista+veículo)
│   ├── repositories/                    # Acesso aos dados (JPA)
│   ├── entitys/                         # Entidades JPA
│   │   ├── Usuario.java                # Usuário base
│   │   ├── Administrator.java          # Admin (herda Usuario)
│   │   ├── Motorista.java              # Motorista (herda Usuario)
│   │   ├── Veiculo.java                # Veículos da frota
│   │   ├── Route.java                  # Rotas de coleta
│   │   ├── RouteCollectionPoint.java   # Pontos de coleta
│   │   └── RouteAssignment.java        # Escalas (vínculo rota+driver+vehicle)
│   ├── dtos/                            # Data Transfer Objects
│   ├── enums/                           # Enumerações
│   │   ├── StatusVeiculo.java          # Status dos veículos
│   │   ├── StatusMotorista.java        # Status dos motoristas
│   │   ├── CategoriaCNH.java           # Categorias de CNH
│   │   ├── CollectionType.java         # Tipos de coleta
│   │   ├── Priority.java               # Prioridades
│   │   ├── WasteType.java              # Tipos de lixo
│   │   └── AssignmentStatus.java       # Status de escalas (ACTIVE/INACTIVE)
│   ├── config/                          # Configurações
│   │   ├── SecurityConfig.java         # Spring Security
│   │   ├── JwtAuthFilter.java          # Filtro JWT
│   │   ├── OpenApiConfig.java          # Swagger/OpenAPI
│   │   ├── MinioConfig.java            # Configuração MinIO
│   │   └── DotenvInitializer.java      # Carregamento .env
│   └── utils/                           # Utilitários
│       └── JwtUtils.java               # Operações JWT
├── src/main/resources/
│   ├── application.properties           # Configurações locais
│   ├── application-docker.properties    # Configurações Docker
│   └── db/changelog/                    # Migrations Liquibase
│       ├── db.changelog-master.yml
│       └── v1.0/
│           ├── 001-create-tables.yml   # Tabelas principais
│           ├── 002-create-indexes.yml  # Índices de performance
│           └── 003-insert-initial-data.yml  # Dados iniciais
├── docs/                                # Documentação
│   ├── API_CONTRACT.md                 # Contrato da API
│   ├── ARCHITECTURE.md                 # Arquitetura
│   ├── DATABASE_DESIGN.md              # Design do banco
│   ├── CONFIGURATION.md                # Configuração
│   └── OD46S_API_Collection.postman_collection.json
├── docker-compose.yml                   # Orquestração Docker
├── Dockerfile                           # Imagem do backend
├── pom.xml                              # Dependências Maven
└── .env                                 # Variáveis de ambiente (criar a partir do env.example)
```

## 🗄️ Banco de Dados

### Tabelas Principais

**Módulo de Usuários**
- `users` - Usuários do sistema (base)
- `administrators` - Administradores (herança)
- `drivers` - Motoristas (herança)

**Módulo de Veículos**
- `vehicles` - Caminhões da frota

**Módulo de Rotas**
- `routes` - Rotas de coleta (com periodicidade)
- `route_collection_points` - Pontos de coleta em cada rota

**Módulo de Escalas**
- `route_assignments` - Vínculo rota + motorista + caminhão (duradouro) ✅

**Módulo de Execuções**
- `route_executions` - Registro de coletas realizadas ✅
- `gps_records` - Rastreamento GPS + eventos + fotos (description, photo_url) ✅
- `collection_point_records` - Registro de coleta em cada ponto (planejado)

**Armazenamento de Arquivos**
- MinIO (S3-compatible) - Fotos de eventos GPS (max 10MB, JPG/PNG/WebP) ✅
- Bucket: `od46s-files`
- Path: `gps-photos/execution_{id}/photo_{timestamp}_{uuid}.{ext}`

### Relacionamentos Principais

```
users (base)
  ├─→ administrators (herança)
  └─→ drivers (herança)

routes
  └─→ route_collection_points (1:N)

route_assignments (escala permanente) ✅ IMPLEMENTADO
  ├─→ routes (N:1)
  ├─→ drivers (N:1)
  ├─→ vehicles (N:1)
  ├─→ administrators (N:1) - created_by
  └─→ route_executions (1:N) ← Uma execução por dia (planejado)

route_executions (coleta realizada)
  ├─→ route_assignments (N:1)
  ├─→ gps_records (1:N)
  └─→ collection_point_records (1:N)
```

## 📚 Documentação Adicional

- **[API Contract](docs/API_CONTRACT.md)** - Contrato completo das APIs REST
- **[Architecture](docs/ARCHITECTURE.md)** - Arquitetura do sistema e padrões
- **[Database Design](docs/DATABASE_DESIGN.md)** - Design do banco de dados e relacionamentos
- **[Configuration](docs/CONFIGURATION.md)** - Guia de configuração e variáveis de ambiente
- **[Postman Collection](docs/OD46S_API_Collection.postman_collection.json)** - Coleção completa para testes da API

## 🚀 Desenvolvimento

### Compilar e Executar Localmente
```bash
# Compilar
./mvnw clean package

# Executar
./mvnw spring-boot:run

# Ou com perfil específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Logs e Debug
```bash
# Ver logs do container
docker logs od46s-backend -f

# Acessar container
docker exec -it od46s-backend /bin/sh

# Ver status dos containers
docker-compose ps
```

### 🧪 Testando a API

#### Usando Postman
1. Importe a coleção: `docs/OD46S_API_Collection.postman_collection.json`
2. Configure a variável `baseUrl` para `http://localhost:8080`
3. Execute primeiro um login para obter o token JWT
4. Teste os endpoints de usuários com autenticação

#### Exemplo de Teste com cURL
```bash
# 1. Login para obter token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@od46s.com", "password": "admin123"}'

# 2. Listar usuários (substitua TOKEN pelo token obtido)
curl -X GET "http://localhost:8080/api/v1/users?page=1&limit=10" \
  -H "Authorization: Bearer TOKEN"

# 3. Criar novo usuário
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "name": "Novo Usuário",
    "email": "novo@od46s.com",
    "cpf": "12345678901",
    "password": "senha123",
    "type": "DRIVER",
    "licenseNumber": "12345678901",
    "licenseCategory": "B",
    "licenseExpiry": "2030-12-31"
  }'
```

## 📊 Cobertura de Código

O projeto utiliza **JaCoCo** para análise de cobertura de código com relatórios detalhados e métricas automáticas.

### 🔍 Comandos de Cobertura

#### Verificar Cobertura Atual
```bash
# Script automatizado com análise completa
./scripts/coverage-report.sh
```

#### Gerar Relatórios
```bash
# Executar testes com cobertura e gerar relatórios
./mvnw clean test jacoco:report

# Apenas gerar relatório (após testes)
./mvnw jacoco:report
```

#### Verificar Meta de Cobertura
```bash
# Verificar se atinge a meta de 80%
./mvnw jacoco:check
```

#### Executar Apenas Testes
```bash
# Executar todos os testes
./mvnw test

# Executar testes específicos
./mvnw test -Dtest=UsuarioControllerTest
```

### 📁 Relatórios Gerados

#### Relatório HTML (Recomendado)
```bash
# Abrir relatório no navegador
open target/site/jacoco/index.html
```

## 🧰 Scripts de Reset do Ambiente

Use os scripts em `scripts/` para resetar o ambiente Docker e subir tudo novamente do zero.

### macOS / Linux
```bash
bash scripts/reset_env.sh
```

Pré-requisitos: `docker` e `docker-compose` instalados. O script irá:
- Derrubar o stack (`docker-compose down -v`)
- Prunar volumes e imagens dangling
- Remover volumes do projeto se existirem
- Subir `postgres` e depois `backend` com `--build`
- Aguardar o health em `http://127.0.0.1:8080/actuator/health`

### Windows (PowerShell)
```powershell
powershell -ExecutionPolicy Bypass -File scripts/reset_env.ps1
```

Se necessário, execute o PowerShell como Administrador. O script realiza as mesmas etapas descritas acima.

---

**Sistema OD46S - Modernizando a coleta de lixo urbano com tecnologia** 🌍 

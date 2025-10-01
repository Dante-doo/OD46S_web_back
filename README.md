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
- **Docker + Docker Compose** - Containerização

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

## 🐳 Como Executar com Docker

### Pré-requisitos
- Docker 20.0+
- Docker Compose 2.0+

### Execução
```bash
# 1. Clone o repositório
git clone <repository-url>
cd OD46S_web_back

# 2. Inicie os containers
docker-compose up -d

# 3. Verifique se está funcionando
curl http://localhost:8080/actuator/health

# 4. Para parar
docker-compose down
```

### Portas e URLs
- **Backend**: http://localhost:8080
- **PostgreSQL**: localhost:5432
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
| POST | `/api/v1/auth/register` | Registro de novo usuário | ✅ Implementado |
| POST | `/api/v1/auth/refresh` | Renovar token JWT | ✅ Implementado |
| GET | `/api/v1/auth/health` | Health do serviço de autenticação | ✅ Implementado |

### Gestão de Usuários (Planejadas)
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/users` | Listar usuários (paginado) | ❌ Não implementado |
| GET | `/api/v1/users/{id}` | Obter usuário específico | ❌ Não implementado |
| POST | `/api/v1/users` | Criar novo usuário | ❌ Não implementado |
| PUT | `/api/v1/users/{id}` | Atualizar usuário | ❌ Não implementado |
| DELETE | `/api/v1/users/{id}` | Remover usuário | ❌ Não implementado |

### Gestão de Veículos
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/vehicles` | Listar veículos | ✅ Implementado |
| POST | `/api/v1/vehicles` | Cadastrar veículo | ✅ Implementado |
| PUT | `/api/v1/vehicles/{id}` | Atualizar veículo | ✅ Implementado |
| PATCH | `/api/v1/vehicles/{id}/status` | Alterar status | ✅ Implementado |

### Gestão de Rotas (Planejadas)
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/routes` | Listar rotas | ❌ Não implementado |
| GET | `/api/v1/routes/{id}` | Obter rota com pontos | ❌ Não implementado |
| POST | `/api/v1/routes` | Criar nova rota | ❌ Não implementado |
| POST | `/api/v1/routes/{id}/points` | Adicionar ponto à rota | ❌ Não implementado |
| PUT | `/api/v1/routes/{id}/points/reorder` | Reordenar pontos | ❌ Não implementado |

### APIs Mobile (Planejadas)
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/mobile/routes` | Rotas do motorista | ❌ Não implementado |
| POST | `/api/v1/mobile/executions` | Iniciar execução | ❌ Não implementado |
| POST | `/api/v1/mobile/executions/{id}/gps` | Registrar GPS | ❌ Não implementado |
| POST | `/api/v1/mobile/executions/{id}/collections` | Registrar coleta | ❌ Não implementado |
| PUT | `/api/v1/mobile/executions/{id}/finish` | Finalizar execução | ❌ Não implementado |

### Relatórios (Planejados)
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/dashboard` | Dashboard com KPIs | ❌ Não implementado |
| GET | `/api/v1/executions` | Histórico de execuções | ❌ Não implementado |
| GET | `/api/v1/executions/{id}/tracking` | Tracking GPS | ❌ Não implementado |

### Sincronização (Planejada)
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| POST | `/api/v1/mobile/sync` | Sincronizar dados offline | ❌ Não implementado |

## 📋 Estrutura do Projeto

```
OD46S_web_back/
├── src/main/java/utfpr/OD46S/backend/
│   ├── BackendApplication.java          # Aplicação principal
│   ├── controllers/                     # Controllers REST
│   ├── services/                        # Lógica de negócio
│   ├── repositories/                    # Acesso aos dados
│   ├── entities/                        # Entidades JPA
│   ├── dtos/                           # Data Transfer Objects
│   ├── enums/                          # Enumerações
│   ├── config/                         # Configurações
│   └── utils/                          # Utilitários
├── src/main/resources/
│   ├── application.properties          # Configurações locais
│   ├── application-docker.properties   # Configurações Docker
│   └── db/changelog/                   # Migrations Liquibase
├── docker-compose.yml                  # Orquestração Docker
├── Dockerfile                          # Imagem do backend
└── pom.xml                             # Dependências Maven
```

## 📚 Documentação Adicional

- **[API Contract](API_CONTRACT.md)** - Contrato completo das APIs
- **[Architecture](ARCHITECTURE.md)** - Arquitetura do sistema
- **[Database Design](DATABASE_DESIGN.md)** - Design do banco de dados

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

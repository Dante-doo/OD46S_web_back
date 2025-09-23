# 🏗️ Arquitetura - Sistema OD46S

## 📋 Visão Geral

O Sistema OD46S é uma plataforma completa para gestão de coleta de lixo urbano, projetada com arquitetura moderna e escalável, integrando aplicações web, mobile e APIs robustas.

## 🎯 Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────────────────┐
│                            🌐 CAMADA DE APRESENTAÇÃO                 │
├─────────────────────────────┬───────────────────────────────────────┤
│     📱 Mobile App           │        🖥️ Web Dashboard               │
│     (Kotlin Android)        │        (React + TypeScript)          │
│                            │                                       │
│  • Rotas do Motorista      │  • Gestão de Usuários                │
│  • Coleta em Campo         │  • Controle de Veículos               │
│  • GPS Tracking           │  • Planejamento de Rotas              │
│  • Modo Offline           │  • Relatórios e KPIs                  │
│  • Sync de Dados          │  • Dashboard Executivo                │
└─────────────────────────────┴───────────────────────────────────────┘
                              │
                              │ HTTPS/REST + JWT
                              │
┌─────────────────────────────────────────────────────────────────────┐
│                        🔗 CAMADA DE INTEGRAÇÃO                      │
├─────────────────────────────────────────────────────────────────────┤
│                        🌐 API Gateway / Load Balancer               │
│                               (Nginx)                               │
│                                                                     │
│  • Roteamento de Requisições  • Rate Limiting                      │
│  • SSL Termination           • Compressão GZIP                     │
│  • Cache de Respostas        • Health Checks                       │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP Interno
                              │
┌─────────────────────────────────────────────────────────────────────┐
│                       ⚙️ CAMADA DE APLICAÇÃO                        │
├─────────────────────────────────────────────────────────────────────┤
│                    🚀 Backend API (Spring Boot)                     │
│                                                                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │
│  │  🔐 Auth Module │  │ 👥 Users Module │  │ 🚛 Fleet Module │    │
│  │                 │  │                 │  │                 │    │
│  │ • JWT Security  │  │ • User CRUD     │  │ • Vehicle CRUD  │    │
│  │ • Role Control  │  │ • Profiles      │  │ • Status Track  │    │
│  │ • Password Hash │  │ • Permissions   │  │ • Maintenance   │    │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘    │
│                                                                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │
│  │ 🗺️ Routes Module│  │ 📋 Exec Module  │  │ 📊 Report Module│    │
│  │                 │  │                 │  │                 │    │
│  │ • Route CRUD    │  │ • Execution     │  │ • Dashboard     │    │
│  │ • Point Mgmt    │  │ • GPS Tracking  │  │ • Analytics     │    │
│  │ • Scheduling    │  │ • Collection    │  │ • KPIs          │    │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘    │
│                                                                     │
│  ┌─────────────────┐  ┌─────────────────┐                         │
│  │ 🔄 Sync Module  │  │ 📁 File Module  │                         │
│  │                 │  │                 │                         │
│  │ • Offline Data  │  │ • Photo Upload  │                         │
│  │ • Batch Proc    │  │ • File Storage  │                         │
│  │ • Conflict Res  │  │ • CDN Integ     │                         │
│  └─────────────────┘  └─────────────────┘                         │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              │ JPA/Hibernate
                              │
┌─────────────────────────────────────────────────────────────────────┐
│                        💾 CAMADA DE DADOS                           │
├─────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐                         │
│  │ 🗄️ PostgreSQL   │  │ 📁 File Storage │                         │
│  │   (Primary)     │  │   (MinIO Open)  │                         │
│  │                 │  │                 │                         │
│  │ • Users Data    │  │ • Photos        │                         │
│  │ • Routes        │  │ • Documents     │                         │
│  │ • Executions    │  │ • Reports       │                         │
│  │ • Collections   │  │ • Exports       │                         │
│  │ • GPS Logs      │  │ • Backups       │                         │
│  │ • Analytics     │  │ • Media Files   │                         │
│  └─────────────────┘  └─────────────────┘                         │
└─────────────────────────────────────────────────────────────────────┘
```

## 🛠️ Stack Tecnológico

### 🖥️ Backend
| Componente | Tecnologia | Versão | Propósito |
|------------|------------|--------|-----------|
| **Framework** | Spring Boot | 3.5.5 | Framework principal |
| **Linguagem** | Java | 21 LTS | Linguagem de programação |
| **ORM** | Hibernate/JPA | 6.x | Mapeamento objeto-relacional |
| **Segurança** | Spring Security | 6.x | Autenticação e autorização |
| **JWT** | JJWT | 0.12.x | Tokens de autenticação |
| **Validação** | Bean Validation | 3.x | Validação de dados |
| **Banco Principal** | PostgreSQL | 15 | Banco de dados principal |
| **Migrations** | Liquibase | 4.x | Versionamento do banco |
| **Documentação** | OpenAPI 3 | 3.x | Documentação da API |
| **Monitoramento** | Spring Actuator | 3.x | Health checks e métricas |
| **Build** | Maven | 3.9.x | Gerenciador de dependências |

### 🌐 Frontend Web
| Componente | Tecnologia | Versão | Propósito |
|------------|------------|--------|-----------|
| **Framework** | React | 18.x | Interface de usuário |
| **Linguagem** | TypeScript | 5.x | Tipagem estática |
| **UI Library** | Material-UI | 5.x | Componentes visuais |
| **Estado Global** | Redux Toolkit | 2.x | Gerenciamento de estado |
| **Roteamento** | React Router | 6.x | Navegação SPA |
| **HTTP Client** | Axios | 1.x | Cliente HTTP |
| **Formulários** | React Hook Form | 7.x | Gerenciamento de forms |
| **Charts** | Chart.js | 4.x | Gráficos e visualizações |
| **Maps** | Leaflet | 1.9.x | Mapas interativos |
| **Build** | Vite | 5.x | Build tool moderna |

### 📱 Mobile Android
| Componente | Tecnologia | Versão | Propósito |
|------------|------------|--------|-----------|
| **Linguagem** | Kotlin | 1.9.x | Linguagem nativa Android |
| **UI Framework** | Jetpack Compose | 1.5.x | Interface declarativa |
| **Arquitetura** | MVVM + Clean | - | Padrão arquitetural |
| **DI** | Hilt | 2.x | Injeção de dependência |
| **Networking** | Retrofit + OkHttp | 2.9.x | Cliente HTTP |
| **Banco Local** | Room | 2.5.x | Banco SQLite local |
| **GPS/Maps** | Google Maps API | Latest | Mapas e localização |
| **Camera** | CameraX | 1.3.x | Captura de fotos |
| **Sincronização** | WorkManager | 2.8.x | Tarefas em background |
| **Serialização** | Kotlinx.serialization | 1.6.x | JSON parsing |

### 🚀 DevOps e Infraestrutura
| Componente | Tecnologia | Versão | Propósito |
|------------|------------|--------|-----------|
| **Containerização** | Docker | 24.x | Containers |
| **Orquestração** | Docker Compose | 2.x | Multi-container |
| **Proxy Reverso** | Nginx | 1.25.x | Load balancer |
| **CI/CD** | GitHub Actions | - | Integração contínua |
| **Monitoramento** | Prometheus | 2.x | Métricas |
| **Logs** | ELK Stack | 8.x | Centralização de logs |
| **Storage** | MinIO (Open Source) | Latest | Armazenamento de arquivos S3-compatible |
| **Deployment** | Self-Hosted VPS | - | Servidor próprio ou VPS barato |

---

**Arquitetura moderna, escalável e segura para gestão municipal de coleta de lixo** 🏗️

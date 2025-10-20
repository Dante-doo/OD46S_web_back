# ⚙️ Configuração Centralizada - Sistema OD46S

## 📋 Visão Geral

O Sistema OD46S utiliza um arquivo de configuração centralizado (`config.env`) para gerenciar todas as variáveis de ambiente, facilitando o deployment e a manutenção em diferentes ambientes.

## 📁 Estrutura de Arquivos

```
├── config.env              # Configurações principais (desenvolvimento)
├── config.env.example      # Arquivo de exemplo
├── config.prod.env         # Configurações de produção
├── scripts/load-env.sh     # Script de automação
└── src/main/resources/
    ├── application.properties        # Configurações Spring (desenvolvimento)
    └── application-docker.properties # Configurações Spring (Docker)
```

## 🔧 Configuração Inicial

### 1. Copiar Arquivo de Exemplo
```bash
cp config.env.example config.env
```

### 2. Editar Configurações
```bash
# Editar com seu editor preferido
nano config.env
# ou
vim config.env
# ou
code config.env
```

### 3. Executar com Configurações
```bash
# Usar script de automação
./scripts/load-env.sh docker

# Ou executar diretamente
docker-compose up -d
```

## 📊 Variáveis de Configuração

### 🗄️ Banco de Dados
```bash
# Desenvolvimento
DB_HOST=localhost
DB_PORT=5432
DB_NAME=od46s_db_dev
DB_USER=od46s_user
DB_PASSWORD=1234

# Docker
DOCKER_DB_HOST=postgres
DOCKER_DB_PORT=5432
DOCKER_DB_NAME=od46s_db
DOCKER_DB_USER=od46s_user
DOCKER_DB_PASSWORD=password123
```

### 🚀 Aplicação
```bash
APP_NAME=backend
APP_PORT=8080
APP_ADDRESS=0.0.0.0
APP_PROFILE=default
```

### 🔐 Segurança
```bash
JWT_SECRET=od46s_super_secret_key_2025_very_long_and_secure
JWT_EXPIRATION=86400000
BCRYPT_STRENGTH=10
```

### 🌐 CORS
```bash
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=*
```

### 📝 Logging
```bash
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=DEBUG
LOG_LEVEL_SPRING=INFO
LOG_LEVEL_HIBERNATE=DEBUG
LOG_PATTERN=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
LOG_FILE_PATH=/app/logs/od46s-backend.log
```

## 🚀 Script de Automação

O script `scripts/load-env.sh` facilita o gerenciamento do ambiente:

### Comandos Disponíveis
```bash
./scripts/load-env.sh dev      # Desenvolvimento local
./scripts/load-env.sh docker   # Docker Compose
./scripts/load-env.sh test     # Executar testes
./scripts/load-env.sh build    # Build da aplicação
./scripts/load-env.sh clean    # Limpar e rebuild
./scripts/load-env.sh logs     # Ver logs do Docker
./scripts/load-env.sh stop     # Parar containers
./scripts/load-env.sh help     # Mostrar ajuda
```

### Funcionalidades do Script
- ✅ Carrega automaticamente as variáveis de `config.env`
- ✅ Valida se as configurações foram carregadas
- ✅ Exibe informações sobre as configurações ativas
- ✅ Executa comandos com as variáveis carregadas
- ✅ Interface colorida e amigável

## 🌍 Ambientes

### 🛠️ Desenvolvimento
```bash
# Usar config.env (padrão)
./scripts/load-env.sh docker
```

### 🏭 Produção
```bash
# Usar config.prod.env
docker-compose --env-file config.prod.env up -d
```

### 🧪 Testes
```bash
# Executar testes com configurações
./scripts/load-env.sh test
```

## 🔄 Migração de Configurações

### De Configurações Antigas
Se você estava usando configurações hardcoded, siga estes passos:

1. **Identificar variáveis**: Encontre todas as configurações nos arquivos `.properties`
2. **Mapear para config.env**: Crie entradas correspondentes no `config.env`
3. **Testar**: Execute `./scripts/load-env.sh docker` para verificar
4. **Remover hardcoded**: Substitua valores fixos por variáveis `${VAR_NAME:default}`

### Exemplo de Migração
```properties
# ANTES (hardcoded)
spring.datasource.url=jdbc:postgresql://localhost:5432/od46s_db_dev
spring.datasource.username=od46s_user
spring.datasource.password=1234

# DEPOIS (centralizado)
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:od46s_db_dev}
spring.datasource.username=${DB_USER:od46s_user}
spring.datasource.password=${DB_PASSWORD:1234}
```

## 🛡️ Segurança

### 🔒 Variáveis Sensíveis
```bash
# NUNCA commitar no Git
JWT_SECRET=your_super_secret_key_here
DB_PASSWORD=your_database_password_here
PGADMIN_PASSWORD=your_pgadmin_password_here
```

### 📁 Arquivos a Ignorar
```gitignore
# Adicionar ao .gitignore
config.env
config.prod.env
*.env
```

### 🔐 Para Produção
1. **Use secrets management** (AWS Secrets Manager, Azure Key Vault, etc.)
2. **Rotacione chaves** regularmente
3. **Use senhas fortes** e únicas
4. **Monitore acessos** às configurações

## 🐛 Troubleshooting

### ❌ Erro: "Arquivo config.env não encontrado"
```bash
# Solução: Copiar arquivo de exemplo
cp config.env.example config.env
```

### ❌ Erro: "Variáveis não carregadas"
```bash
# Verificar se o arquivo tem formato correto
cat config.env | grep -v '^#' | head -5

# Verificar se não há espaços em torno do =
# CORRETO: VAR=value
# INCORRETO: VAR = value
```

### ❌ Erro: "Docker não encontra variáveis"
```bash
# Verificar se o docker-compose.yml está usando env_file
grep -A 5 "env_file" docker-compose.yml

# Verificar se as variáveis estão definidas
docker-compose config
```

### ❌ Erro: "Aplicação não conecta no banco"
```bash
# Verificar configurações do banco
echo "DB_HOST: $DB_HOST"
echo "DB_PORT: $DB_PORT"
echo "DB_NAME: $DB_NAME"

# Testar conexão
docker-compose exec postgres psql -U $DB_USER -d $DB_NAME -c "SELECT 1;"
```

## 📚 Referências

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)
- [Docker Compose Environment Variables](https://docs.docker.com/compose/environment-variables/)
- [Environment Variables Best Practices](https://12factor.net/config)

---

**Última atualização**: 14 de Outubro de 2025  
**Versão**: 1.0  
**Responsável**: Equipe de Desenvolvimento OD46S

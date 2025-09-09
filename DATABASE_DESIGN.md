# 🗄️ Design de Banco de Dados - Sistema OD46S

## 📊 Entidades Principais

### 👥 Usuários (Herança)
- **usuarios** - tabela base
- **administradores** - herda de usuarios  
- **motoristas** - herda de usuarios

### 🚛 Operacionais
- **veiculos** - frota de caminhões
- **rotas** - planejamento de coletas
- **pontos_coleta** - locais de coleta
- **registros_coleta** - histórico das coletas
- **enderecos** - localização dos pontos

## 🏗️ Scripts das Tabelas

### 📋 Estrutura de Usuários (Herança)

#### **Abordagem: Table Inheritance (Ideal)**
Tabela base + tabelas específicas para cada tipo de usuário.

```sql
-- 1. TABELA BASE - Dados comuns a todos os usuários
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    tipo_usuario VARCHAR(15) NOT NULL CHECK (tipo_usuario IN ('ADMIN', 'MOTORISTA')),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE
);

-- 2. TABELA ESPECÍFICA - Administradores
CREATE TABLE administradores (
    id BIGINT PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
    nivel_acesso VARCHAR(20) DEFAULT 'ADMIN',
    ultimo_login TIMESTAMP,
    
    -- Constraint para garantir que só admins sejam inseridos
    CONSTRAINT chk_admin_tipo CHECK (
        (SELECT tipo_usuario FROM usuarios WHERE id = administradores.id) = 'ADMIN'
    )
);

-- 3. TABELA ESPECÍFICA - Motoristas  
CREATE TABLE motoristas (
    id BIGINT PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
    cnh VARCHAR(11) UNIQUE NOT NULL,
    categoria_cnh VARCHAR(2) NOT NULL,
    validade_cnh DATE NOT NULL,
    habilitado BOOLEAN DEFAULT TRUE,
    
    -- Constraints
    CONSTRAINT chk_motorista_tipo CHECK (
        (SELECT tipo_usuario FROM usuarios WHERE id = motoristas.id) = 'MOTORISTA'
    ),
    CONSTRAINT chk_categoria_cnh CHECK (categoria_cnh IN ('A', 'B', 'C', 'D', 'E')),
    CONSTRAINT chk_validade_cnh CHECK (validade_cnh > CURRENT_DATE)
);
```

#### **🔧 Triggers para Consistência**
```sql
-- Trigger para atualizar timestamp
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_usuarios_timestamp 
    BEFORE UPDATE ON usuarios 
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();
```

#### **🎯 Vantagens desta Estrutura:**

1. **✅ Normalização**: Sem campos nulos desnecessários
2. **✅ Integridade**: Constraints garantem consistência
3. **✅ Extensibilidade**: Fácil adicionar novos tipos de usuário
4. **✅ Performance**: Consultas específicas são mais eficientes
5. **✅ Manutenção**: Alterações isoladas por tipo

#### **📊 Consultas Úteis:**

```sql
-- Buscar todos os usuários com seus tipos
SELECT u.id, u.nome, u.email, u.tipo_usuario, u.ativo
FROM usuarios u;

-- Buscar motoristas com dados específicos
SELECT u.nome, u.email, m.cnh, m.categoria_cnh, m.habilitado
FROM usuarios u
JOIN motoristas m ON u.id = m.id
WHERE u.ativo = true;

-- Buscar administradores ativos
SELECT u.nome, u.email, a.nivel_acesso, a.ultimo_login
FROM usuarios u
JOIN administradores a ON u.id = a.id
WHERE u.ativo = true;

-- Contar usuários por tipo
SELECT tipo_usuario, COUNT(*) as total
FROM usuarios
WHERE ativo = true
GROUP BY tipo_usuario;
```

### 🚛 Veículos
```sql
CREATE TABLE veiculos (
    id BIGSERIAL PRIMARY KEY,
    placa VARCHAR(8) UNIQUE NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    marca VARCHAR(30) NOT NULL,
    ano INTEGER NOT NULL,
    capacidade_kg DECIMAL(8,2) NOT NULL,
    tipo_veiculo VARCHAR(30) NOT NULL,
    status_veiculo VARCHAR(20) DEFAULT 'DISPONIVEL',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 🗺️ Endereços e Pontos
```sql
CREATE TABLE enderecos (
    id BIGSERIAL PRIMARY KEY,
    logradouro VARCHAR(200) NOT NULL,
    numero VARCHAR(20),
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    estado VARCHAR(2) NOT NULL,
    cep VARCHAR(8) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8)
);

CREATE TABLE pontos_coleta (
    id BIGSERIAL PRIMARY KEY,
    endereco_id BIGINT NOT NULL REFERENCES enderecos(id),
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    tipos_lixo VARCHAR(20)[] NOT NULL,
    peso_estimado_kg DECIMAL(8,2) DEFAULT 0,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 🛣️ Sistema de Rotas Completo

#### **1. Rotas Cadastradas (Planejamento)**
```sql
-- Rotas planejadas pelos administradores
CREATE TABLE rotas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    tipo_coleta VARCHAR(20) NOT NULL, -- RESIDENCIAL, COMERCIAL, etc
    
    -- Periodicidade usando cron expression (muito flexível!)
    periodicidade_cron VARCHAR(50) NOT NULL, -- Ex: "0 6 * * 1,3,5" = seg,qua,sex às 6h
    
    -- Campo auxiliar para facilitar consultas
    descricao_periodicidade VARCHAR(100), -- Ex: "Segunda, Quarta e Sexta às 6:00"
    
    duracao_estimada_min INTEGER,
    distancia_estimada_km DECIMAL(8,2),
    status_rota VARCHAR(20) DEFAULT 'ATIVA',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Validação básica do formato cron (5 campos)
    CONSTRAINT chk_cron_format CHECK (
        periodicidade_cron ~ '^[0-9\*,\-/]+ [0-9\*,\-/]+ [0-9\*,\-/]+ [0-9\*,\-/]+ [0-9\*,\-/]+$'
    )
);

-- Exemplos de periodicidades comuns para coleta de lixo:
/*
CRON FORMAT: minuto hora dia mês dia_da_semana

EXEMPLOS PRÁTICOS:
"0 6 * * 1,3,5"    = Segunda, Quarta e Sexta às 6:00
"0 6 * * 1-5"      = Segunda a Sexta às 6:00  
"0 6 * * 1"        = Toda Segunda às 6:00
"0 6 */2 * *"      = A cada 2 dias às 6:00
"0 6 1,15 * *"     = Dia 1 e 15 de cada mês às 6:00
"0 6 1 * *"        = Todo dia 1º do mês às 6:00
"0 6 * * 6"        = Todo Sábado às 6:00
"30 14 * * 2,4"    = Terça e Quinta às 14:30
"0 8 */3 * *"      = A cada 3 dias às 8:00
"0 7 1-7 * 1"      = Primeira Segunda do mês às 7:00
*/

-- Pontos que fazem parte de cada rota (sequência planejada)
CREATE TABLE rota_pontos_coleta (
    id BIGSERIAL PRIMARY KEY,
    rota_id BIGINT NOT NULL REFERENCES rotas(id) ON DELETE CASCADE,
    ponto_coleta_id BIGINT NOT NULL REFERENCES pontos_coleta(id) ON DELETE CASCADE,
    ordem_visita INTEGER NOT NULL,
    horario_estimado TIME,
    tempo_estimado_min INTEGER,
    obrigatorio BOOLEAN DEFAULT TRUE,
    
    UNIQUE(rota_id, ordem_visita),
    UNIQUE(rota_id, ponto_coleta_id)
);
```

#### **2. Execuções de Rotas (Realizadas)**
```sql
-- Quando um motorista executa uma rota (cada saída = 1 execução)
CREATE TABLE execucoes_rota (
    id BIGSERIAL PRIMARY KEY,
    rota_id BIGINT NOT NULL REFERENCES rotas(id),
    motorista_id BIGINT NOT NULL REFERENCES motoristas(id),
    veiculo_id BIGINT NOT NULL REFERENCES veiculos(id),
    
    -- Controle de tempo
    data_execucao DATE NOT NULL DEFAULT CURRENT_DATE,
    hora_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    hora_fim TIMESTAMP,
    
    -- Status da execução
    status_execucao VARCHAR(20) DEFAULT 'EM_ANDAMENTO', -- EM_ANDAMENTO, CONCLUIDA, CANCELADA
    
    -- Dados coletados durante execução
    total_peso_coletado_kg DECIMAL(10,2),
    total_pontos_visitados INTEGER DEFAULT 0,
    total_pontos_planejados INTEGER,
    distancia_percorrida_km DECIMAL(8,2),
    
    -- Observações gerais da execução
    observacoes_gerais TEXT,
    problemas_encontrados TEXT,
    
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### **3. Registros GPS (Tracking)**
```sql
-- Tracking GPS durante a execução da rota
CREATE TABLE registros_gps (
    id BIGSERIAL PRIMARY KEY,
    execucao_rota_id BIGINT NOT NULL REFERENCES execucoes_rota(id) ON DELETE CASCADE,
    
    -- Coordenadas
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    altitude DECIMAL(8, 2), -- opcional
    
    -- Dados do movimento
    velocidade_kmh DECIMAL(5, 2),
    direcao_graus INTEGER, -- 0-360 graus
    precisao_metros DECIMAL(6, 2),
    
    -- Timestamp do GPS
    timestamp_gps TIMESTAMP NOT NULL,
    timestamp_servidor TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Contexto (opcional)
    tipo_evento VARCHAR(20), -- INICIO, PARADA, PONTO_COLETA, FIM, NORMAL
    observacao VARCHAR(255)
);
```

#### **4. Coletas nos Pontos (Detalhamento)**
```sql
-- Registro específico de cada ponto coletado durante a execução
CREATE TABLE registros_coleta_pontos (
    id BIGSERIAL PRIMARY KEY,
    execucao_rota_id BIGINT NOT NULL REFERENCES execucoes_rota(id) ON DELETE CASCADE,
    ponto_coleta_id BIGINT NOT NULL REFERENCES pontos_coleta(id),
    
    -- Sequência real (pode diferir do planejado)
    ordem_real_visita INTEGER,
    
    -- Controle de tempo no ponto
    hora_chegada TIMESTAMP,
    hora_saida TIMESTAMP,
    tempo_coleta_min INTEGER GENERATED ALWAYS AS (
        EXTRACT(EPOCH FROM (hora_saida - hora_chegada)) / 60
    ) STORED,
    
    -- Dados coletados
    peso_coletado_kg DECIMAL(8,2),
    tipos_lixo_coletados VARCHAR(20)[],
    volume_estimado_m3 DECIMAL(6,2),
    
    -- Status da coleta neste ponto
    status_coleta VARCHAR(20) DEFAULT 'PENDENTE', -- PENDENTE, COLETADO, PULADO, PROBLEMA
    motivo_nao_coleta VARCHAR(100), -- se status != COLETADO
    
    -- Localização real da coleta
    latitude_real DECIMAL(10, 8),
    longitude_real DECIMAL(11, 8),
    
    -- Evidências
    fotos_antes JSONB DEFAULT '[]',
    fotos_depois JSONB DEFAULT '[]',
    observacoes TEXT,
    
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(execucao_rota_id, ponto_coleta_id)
);
```

#### **📊 Índices para Performance**
```sql
-- Rotas
CREATE INDEX idx_rotas_status ON rotas(status_rota);
CREATE INDEX idx_rotas_tipo ON rotas(tipo_coleta);
CREATE INDEX idx_rotas_cron ON rotas(periodicidade_cron);
CREATE INDEX idx_rotas_tipo_status ON rotas(tipo_coleta, status_rota);

-- Execuções de Rota  
CREATE INDEX idx_execucoes_data ON execucoes_rota(data_execucao);
CREATE INDEX idx_execucoes_motorista ON execucoes_rota(motorista_id, data_execucao);
CREATE INDEX idx_execucoes_status ON execucoes_rota(status_execucao);
CREATE INDEX idx_execucoes_rota_data ON execucoes_rota(rota_id, data_execucao);

-- GPS (crítico para performance)
CREATE INDEX idx_gps_execucao_timestamp ON registros_gps(execucao_rota_id, timestamp_gps);
CREATE INDEX idx_gps_timestamp ON registros_gps(timestamp_gps);
CREATE INDEX idx_gps_coordenadas ON registros_gps(latitude, longitude);

-- Coletas nos Pontos
CREATE INDEX idx_coleta_pontos_execucao ON registros_coleta_pontos(execucao_rota_id);
CREATE INDEX idx_coleta_pontos_status ON registros_coleta_pontos(status_coleta);
CREATE INDEX idx_coleta_pontos_ordem ON registros_coleta_pontos(execucao_rota_id, ordem_real_visita);
```

#### **🕐 Funções para Trabalhar com Cron**

```sql
-- Função para extrair horário do cron
CREATE OR REPLACE FUNCTION extrair_horario_cron(cron_expr VARCHAR)
RETURNS TIME AS $$
BEGIN
    RETURN (split_part(cron_expr, ' ', 2) || ':' || split_part(cron_expr, ' ', 1))::TIME;
END;
$$ LANGUAGE plpgsql;

-- Função para verificar se rota deve executar hoje
CREATE OR REPLACE FUNCTION rota_executa_hoje(cron_expr VARCHAR, data_ref DATE DEFAULT CURRENT_DATE)
RETURNS BOOLEAN AS $$
DECLARE
    dia_semana INT := EXTRACT(DOW FROM data_ref); -- 0=domingo, 1=segunda, etc
    dia_mes INT := EXTRACT(DAY FROM data_ref);
    mes INT := EXTRACT(MONTH FROM data_ref);
    cron_parts TEXT[];
BEGIN
    cron_parts := string_to_array(cron_expr, ' ');
    
    -- Verificar dia da semana (campo 5)
    IF cron_parts[5] != '*' THEN
        IF NOT (dia_semana = ANY(string_to_array(replace(cron_parts[5], '-', ','), ','::TEXT)::INT[])) THEN
            RETURN FALSE;
        END IF;
    END IF;
    
    -- Verificar dia do mês (campo 3) 
    IF cron_parts[3] != '*' THEN
        IF NOT (dia_mes = ANY(string_to_array(replace(cron_parts[3], '-', ','), ','::TEXT)::INT[])) THEN
            RETURN FALSE;
        END IF;
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
```

#### **📋 Consultas Úteis do Sistema**

```sql
-- 1. Buscar execuções de hoje por motorista
SELECT e.*, r.nome as rota_nome, r.descricao_periodicidade
FROM execucoes_rota e
JOIN rotas r ON e.rota_id = r.id
WHERE e.motorista_id = 123 
  AND e.data_execucao = CURRENT_DATE;

-- 2. Rotas que devem executar hoje
SELECT r.id, r.nome, r.periodicidade_cron, r.descricao_periodicidade,
       extrair_horario_cron(r.periodicidade_cron) as horario
FROM rotas r
WHERE r.status_rota = 'ATIVA'
  AND rota_executa_hoje(r.periodicidade_cron)
ORDER BY extrair_horario_cron(r.periodicidade_cron);

-- 2. Tracking GPS de uma execução
SELECT latitude, longitude, velocidade_kmh, timestamp_gps
FROM registros_gps
WHERE execucao_rota_id = 456
ORDER BY timestamp_gps;

-- 3. Relatório de coleta por execução
SELECT 
    rcp.ponto_coleta_id,
    pc.nome as ponto_nome,
    rcp.peso_coletado_kg,
    rcp.status_coleta,
    rcp.hora_chegada,
    rcp.hora_saida
FROM registros_coleta_pontos rcp
JOIN pontos_coleta pc ON rcp.ponto_coleta_id = pc.id
WHERE rcp.execucao_rota_id = 456
ORDER BY rcp.ordem_real_visita;

-- 4. Performance de motorista (últimos 30 dias)
SELECT 
    m.id,
    u.nome,
    COUNT(e.id) as total_execucoes,
    AVG(e.total_peso_coletado_kg) as peso_medio,
    AVG(EXTRACT(EPOCH FROM (e.hora_fim - e.hora_inicio))/60) as tempo_medio_min
FROM motoristas m
JOIN usuarios u ON m.id = u.id
JOIN execucoes_rota e ON m.id = e.motorista_id
WHERE e.data_execucao >= CURRENT_DATE - INTERVAL '30 days'
  AND e.status_execucao = 'CONCLUIDA'
GROUP BY m.id, u.nome;

-- 5. Rotas mais executadas
SELECT 
    r.nome,
    COUNT(e.id) as total_execucoes,
    AVG(e.total_peso_coletado_kg) as peso_medio
FROM rotas r
LEFT JOIN execucoes_rota e ON r.id = e.rota_id
WHERE e.data_execucao >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY r.id, r.nome
ORDER BY total_execucoes DESC;
```

## 📊 Enumerações e Formatos

### **Cron Expression Format**
```
minuto hora dia mês dia_da_semana

Campos:
- minuto: 0-59
- hora: 0-23  
- dia: 1-31
- mês: 1-12
- dia_da_semana: 0-6 (0=domingo, 1=segunda, ..., 6=sábado)

Operadores:
- * = qualquer valor
- , = lista de valores (1,3,5)
- - = intervalo (1-5)
- / = incremento (*/2 = a cada 2)
```

### **Status do Sistema**
```sql
-- Status de Rota
status_rota: ATIVA, INATIVA, TEMPORARIA

-- Status de Execução
status_execucao: EM_ANDAMENTO, CONCLUIDA, CANCELADA

-- Status de Coleta em Ponto
status_coleta: PENDENTE, COLETADO, PULADO, PROBLEMA

-- Tipos de Coleta
tipo_coleta: RESIDENCIAL, COMERCIAL, HOSPITALAR, RECICLAVEL, ORGANICO

-- Tipos de Veículo
tipo_veiculo: CAMINHAO_COMPACTADOR, CAMINHAO_BASCULANTE, CAMINHAO_CARROCERIA, VEICULO_APOIO

-- Status de Veículo
status_veiculo: DISPONIVEL, EM_USO, MANUTENCAO, INATIVO

-- Eventos GPS
tipo_evento: INICIO, PARADA, PONTO_COLETA, FIM, NORMAL
```

## 🚀 Exemplos de Inserção

#### **Criar Administrador:**
```sql
-- 1. Inserir na tabela base
INSERT INTO usuarios (nome, email, cpf, senha, tipo_usuario) VALUES 
('João Admin', 'admin@od46s.com', '12345678901', '$2a$12$hash_da_senha', 'ADMIN');

-- 2. Inserir dados específicos do admin
INSERT INTO administradores (id, nivel_acesso) VALUES 
((SELECT id FROM usuarios WHERE email = 'admin@od46s.com'), 'ADMIN');
```

#### **Criar Motorista:**
```sql
-- 1. Inserir na tabela base  
INSERT INTO usuarios (nome, email, cpf, senha, tipo_usuario) VALUES 
('Carlos Motorista', 'carlos@od46s.com', '98765432100', '$2a$12$hash_da_senha', 'MOTORISTA');

-- 2. Inserir dados específicos do motorista
INSERT INTO motoristas (id, cnh, categoria_cnh, validade_cnh, habilitado) VALUES 
((SELECT id FROM usuarios WHERE email = 'carlos@od46s.com'), '12345678901', 'D', '2026-12-31', true);
```

#### **Criar Rota Completa:**
```sql
-- 1. Criar rota com periodicidade cron
INSERT INTO rotas (nome, tipo_coleta, periodicidade_cron, descricao_periodicidade, duracao_estimada_min) 
VALUES ('Rota Centro Manhã', 'RESIDENCIAL', '0 6 * * 1,3,5', 'Segunda, Quarta e Sexta às 6:00', 180);

-- Outros exemplos de rotas:
INSERT INTO rotas (nome, tipo_coleta, periodicidade_cron, descricao_periodicidade, duracao_estimada_min) VALUES
('Coleta Comercial Semanal', 'COMERCIAL', '0 14 * * 6', 'Todo Sábado às 14:00', 240),
('Coleta Hospitalar Diária', 'HOSPITALAR', '0 8 * * 1-5', 'Segunda a Sexta às 8:00', 120),
('Coleta Quinzenal Residencial', 'RESIDENCIAL', '0 6 1,15 * *', 'Dia 1 e 15 de cada mês às 6:00', 300),
('Coleta a cada 3 dias', 'RECICLAVEL', '0 7 */3 * *', 'A cada 3 dias às 7:00', 180);

-- 2. Adicionar pontos à rota (em ordem)
INSERT INTO rota_pontos_coleta (rota_id, ponto_coleta_id, ordem_visita, horario_estimado, tempo_estimado_min) 
VALUES 
    (1, 1, 1, '06:15:00', 15),
    (1, 2, 2, '06:35:00', 20),
    (1, 3, 3, '07:00:00', 15);
```

#### **Executar uma Rota:**
```sql
-- 1. Iniciar execução
INSERT INTO execucoes_rota (rota_id, motorista_id, veiculo_id, total_pontos_planejados)
VALUES (1, 5, 3, 3);

-- 2. Registrar ponto GPS (automático pelo mobile)
INSERT INTO registros_gps (execucao_rota_id, latitude, longitude, velocidade_kmh, timestamp_gps, tipo_evento)
VALUES (1, -25.4284, -49.2733, 35.5, NOW(), 'INICIO');

-- 3. Coletar em um ponto
INSERT INTO registros_coleta_pontos (execucao_rota_id, ponto_coleta_id, ordem_real_visita, 
    hora_chegada, hora_saida, peso_coletado_kg, status_coleta, latitude_real, longitude_real)
VALUES (1, 1, 1, NOW(), NOW() + INTERVAL '15 minutes', 150.5, 'COLETADO', -25.4284, -49.2733);

-- 4. Finalizar execução
UPDATE execucoes_rota 
SET hora_fim = NOW(), 
    status_execucao = 'CONCLUIDA',
    total_peso_coletado_kg = 450.0,
    total_pontos_visitados = 3,
    distancia_percorrida_km = 25.3
WHERE id = 1;
```

## 🔄 Fluxo Completo do Sistema

### **📋 1. Planejamento (Admin via Web)**
1. Admin cria **rota** com dados básicos
2. Admin adiciona **pontos de coleta** à rota em ordem
3. Sistema calcula estimativas de tempo e distância

### **🚛 2. Execução (Motorista via Mobile)**
1. Motorista inicia **execução da rota** (escolhe rota + veículo)
2. App mobile envia **GPS tracking** automaticamente
3. Em cada ponto: motorista registra **coleta** (peso, fotos, observações)
4. Motorista finaliza execução com dados totais

### **📊 3. Monitoramento (Admin via Web)**
1. Admin vê execuções em **tempo real** via GPS
2. Admin consulta **relatórios** de performance
3. Sistema gera **estatísticas** de eficiência

### **🔍 4. Estrutura das Tabelas**

```
PLANEJAMENTO:
├── rotas (o que fazer)
└── rota_pontos_coleta (sequência planejada)

EXECUÇÃO:
├── execucoes_rota (cada saída do caminhão)
├── registros_gps (tracking durante execução)
└── registros_coleta_pontos (o que foi coletado)

SUPORTE:
├── usuarios/motoristas/administradores (quem)
├── veiculos (com o que)
├── pontos_coleta (onde)
└── enderecos (localização)
```

## 🎯 **Vantagens das Cron Expressions**

### **✅ Flexibilidade Total:**
- **Intervalos customizados**: A cada 2 dias, 3 dias, semana, quinzena
- **Datas específicas**: Dia 1 e 15 do mês, primeira segunda
- **Múltiplos horários**: Combinações complexas de dias e horários
- **Padrões sazonais**: Diferentes no verão/inverno

### **✅ Exemplos Práticos para Coleta:**

| Tipo | Cron Expression | Descrição |
|------|----------------|-----------|
| **Residencial Padrão** | `0 6 * * 1,3,5` | Seg, Qua, Sex às 6h |
| **Comercial Semanal** | `0 14 * * 6` | Todo Sábado às 14h |
| **Hospitalar Diário** | `0 8 * * 1-5` | Segunda a Sexta às 8h |
| **Quinzenal** | `0 6 1,15 * *` | Dia 1 e 15 do mês às 6h |
| **A cada 3 dias** | `0 7 */3 * *` | A cada 3 dias às 7h |
| **Mensal** | `0 6 1 * *` | Todo dia 1º do mês às 6h |
| **Urgente** | `0 */4 * * *` | A cada 4 horas |

### **✅ Comparação:**

**❌ Antes (limitado):**
```sql
dias_semana INTEGER[] -- Só dias da semana: [1,2,3,4,5]
```

**✅ Agora (flexível):**
```sql
periodicidade_cron VARCHAR(50) -- Qualquer padrão: "0 6 */2 * *"
```

### **🔧 Como Usar no Spring Boot:**
```java
// Service para verificar rotas do dia
@Service
public class RotaScheduleService {
    
    public List<Rota> getRotasParaHoje() {
        return rotaRepository.findRotasQueExecutamHoje();
    }
    
    // Usar biblioteca cron4j ou quartz para parsing
    public boolean rotaDeveExecutarHoje(String cronExpression) {
        CronExpression cron = new CronExpression(cronExpression);
        return cron.isSatisfiedBy(new Date());
    }
}
```

### **🚀 Benefícios:**
- **Mais realista**: Coleta não é sempre "seg-sex"
- **Configurável**: Admin pode criar qualquer padrão
- **Escalável**: Suporta crescimento complexo da cidade
- **Padrão universal**: Formato conhecido pelos devs
- **Flexível**: Mudanças sazonais fáceis de implementar

---

**Sistema completo com periodicidade flexível usando cron expressions universais.**
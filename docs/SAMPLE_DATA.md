# 📊 Dados de Exemplo - OD46S System

## 🔐 Credenciais de Login

### Usuários Permanentes (NÃO DELETAR)

| ID | Nome | Email | Senha | Tipo | CPF | Descrição |
|----|------|-------|-------|------|-----|-----------|
| 1 | System Administrator | admin@od46s.com | `od46s123` | ADMIN | 11111111111 | ✅ Super Admin - Permanente |
| 2 | Maria Silva | maria.silva@od46s.com | `od46s123` | ADMIN | 22222222222 | ✅ Admin Operacional - Permanente |
| 3 | João Motorista | joao.driver@od46s.com | `od46s123` | DRIVER | 33333333333 | ✅ Motorista Exemplo - Permanente |

### Usuários Temporários (SAFE TO DELETE/UPDATE)

| ID | Nome | Email | Senha | Tipo | CPF | Descrição |
|----|------|-------|-------|------|-----|-----------|
| 4 | TEMP Admin Test | temp.admin@od46s.com | `od46s123` | ADMIN | 44444444444 | 🧪 Dados de teste - DELETE/UPDATE |
| 5 | TEMP Driver Test | temp.driver@od46s.com | `od46s123` | DRIVER | 55555555555 | 🧪 Dados de teste - DELETE/UPDATE |

---

## 🚗 Veículos

### Veículos Temporários (SAFE TO DELETE/UPDATE)

| ID | Placa | Modelo | Marca | Ano | Descrição |
|----|-------|--------|-------|-----|-----------|
| 1 | TMP1234 | TEMP Test Vehicle | Test Brand | 2020 | 🧪 Dados de teste - DELETE/UPDATE |

---

## 🗺️ Rotas e Pontos de Coleta

### Rota Exemplo (Permanente)

| ID | Nome | Tipo | Prioridade | Distância | Criador |
|----|------|------|------------|-----------|---------|
| 1 | Downtown Route A1 | COMMERCIAL | HIGH | 15.5 km | Admin (ID 1) |

#### Pontos de Coleta da Rota 1

| ID | Ordem | Endereço | Lat/Long | Tipo |
|----|-------|----------|----------|------|
| 1 | 1 | 123 Main Street, Downtown | -25.428400, -49.273300 | COMMERCIAL |
| 2 | 2 | 456 Commerce Avenue, Downtown | -25.429500, -49.274200 | COMMERCIAL |
| 3 | 3 | 789 Business Street, Downtown | -25.430200, -49.275100 | COMMERCIAL |

---

## 📝 Guia de Uso no Postman

### ✅ REGRAS IMPORTANTES:

1. **NUNCA DELETE** usuários com IDs **1, 2 ou 3** (permanentes)
2. **USE IDs 4 e 5** para testes de DELETE/UPDATE de usuários
3. **USE ID 1** para testes de DELETE/UPDATE de veículos

### 🧪 Endpoints de Teste Configurados:

#### UPDATE de Usuários:
- `PUT /api/v1/users/4` - Atualizar TEMP Admin Test
- `PUT /api/v1/users/5` - Atualizar TEMP Driver Test

#### DELETE de Usuários:
- `DELETE /api/v1/users/4` - Deletar TEMP Admin Test (Safe)
- `DELETE /api/v1/users/5` - Deletar TEMP Driver Test (Safe)

#### UPDATE de Veículos:
- `PUT /api/v1/vehicles/1` - Atualizar TEMP Test Vehicle

---

## 🔄 Como Restaurar Dados Temporários

Se você deletou os dados temporários e quer recriá-los:

### Opção 1: Recriar o banco completo
```bash
docker-compose down -v
docker-compose up postgres -d
# Aguarde o Liquibase aplicar os changesets
```

### Opção 2: Criar manualmente via API

**Criar Usuário Admin Temporário:**
```json
POST /api/v1/users
{
  "name": "TEMP Admin Test",
  "email": "temp.admin.new@od46s.com",
  "cpf": "66666666666",
  "password": "od46s123",
  "type": "ADMIN",
  "active": true,
  "accessLevel": "ADMIN",
  "department": "Testing",
  "corporatePhone": "47444444444"
}
```

**Criar Usuário Driver Temporário:**
```json
POST /api/v1/users
{
  "name": "TEMP Driver Test",
  "email": "temp.driver.new@od46s.com",
  "cpf": "77777777777",
  "password": "od46s123",
  "type": "DRIVER",
  "active": true,
  "license_number": "88888888888",
  "license_category": "D",
  "license_expiry": "2025-12-31",
  "phone": "47555555555"
}
```

**Criar Veículo Temporário:**
```json
POST /api/v1/vehicles
{
  "licensePlate": "TMP9999",
  "model": "TEMP Test Vehicle",
  "brand": "Test Brand",
  "year": 2020,
  "capacityKg": 5000,
  "fuelType": "DIESEL",
  "averageConsumption": 5.0,
  "status": "AVAILABLE",
  "currentKm": 50000,
  "acquisitionDate": "2020-01-01",
  "notes": "TEMPORARY vehicle for tests",
  "active": true
}
```

---

## 🎯 Fluxo de Teste Completo

### 1. **Login**
```
POST /api/v1/auth/login
{ "email": "admin@od46s.com", "password": "od46s123" }
```

### 2. **Criar Assignment** (Admin)
```
POST /api/v1/assignments
{
  "route_id": 1,
  "driver_id": 3,
  "vehicle_id": 1,
  "start_date": "2025-01-01",
  "notes": "Test assignment"
}
```

### 3. **Iniciar Execução** (Driver)
```
POST /api/v1/executions/start
{
  "assignment_id": 1,
  "initial_km": 50000,
  "initial_notes": "Starting collection"
}
```

### 4. **Registrar GPS** (Driver)

**GPS Normal:**
```
POST /api/v1/executions/1/gps
Content-Type: multipart/form-data

latitude=-25.4284
longitude=-49.2733
speed_kmh=35.5
event_type=NORMAL
```

**Parada para Almoço:**
```
POST /api/v1/executions/1/gps
Content-Type: multipart/form-data

latitude=-25.4284
longitude=-49.2733
event_type=LUNCH
description=Parada para almoço - 30min
```

**Problema COM FOTO:**
```
POST /api/v1/executions/1/gps
Content-Type: multipart/form-data

latitude=-25.4284
longitude=-49.2733
event_type=PROBLEM
description=Lixeira transbordando, lixo espalhado na calçada
photo=@foto_problema.jpg
```

**Coleta em Ponto (Sucesso):**
```
POST /api/v1/executions/1/gps
Content-Type: multipart/form-data

latitude=-25.4284
longitude=-49.2733
event_type=POINT_COLLECTED
point_id=1
collected_weight_kg=45.5
point_condition=NORMAL
description=Lixeira em bom estado, coleta ok
photo=@foto_lixeira_coletada.jpg
```

**Ponto Não Coletado (Pulado):**
```
POST /api/v1/executions/1/gps
Content-Type: multipart/form-data

latitude=-25.4290
longitude=-49.2740
event_type=POINT_SKIPPED
point_id=2
point_condition=INACCESSIBLE
description=Portão trancado, sem acesso ao local
photo=@foto_portao_trancado.jpg
```

**Problema no Ponto (Lixeira Saturada):**
```
POST /api/v1/executions/1/gps
Content-Type: multipart/form-data

latitude=-25.4302
longitude=-49.2751
event_type=POINT_PROBLEM
point_id=3
collected_weight_kg=80.0
point_condition=SATURATED
description=Lixeira transbordando mas consegui coletar tudo
photo=@foto_lixeira_saturada.jpg
```

### 5. **Finalizar Execução** (Driver)
```
PATCH /api/v1/executions/1/complete
{
  "final_km": 50080,
  "total_collected_weight_kg": 1500,
  "points_visited": 3,
  "points_collected": 3,
  "final_notes": "Collection completed successfully"
}
```

---

## 🆘 Troubleshooting

### Erro: "User already exists"
- Use email diferente ou delete o usuário temporário primeiro

### Erro: "Placa já cadastrada"
- Use placa diferente ou delete o veículo temporário primeiro

### Erro: "Driver has history"
- Se tentar deletar driver com assignments/executions, ele será marcado como INATIVO ao invés de deletado

### Erro: "Execution not in progress"
- Só pode registrar GPS em execuções com status `IN_PROGRESS`

### Erro: "File size exceeds maximum limit"
- Fotos devem ter no máximo 10MB
- Formatos aceitos: JPG, PNG, WebP

### Erro: "Only JPEG, PNG and WebP images are allowed"
- Envie apenas arquivos de imagem válidos

---

## 📌 Notas Importantes

1. **Senhas criptografadas**: Todas as senhas são armazenadas com BCrypt (cost 10)
2. **Soft Delete**: Usuários com histórico são marcados como inativos ao invés de deletados
3. **Validações**: Coordenadas GPS, CNH, placas e CPFs são validados
4. **Sequências**: IDs começam após os dados iniciais (users: 6+, vehicles: 2+)
5. **Fotos GPS**: Armazenadas no MinIO (S3-compatible), max 10MB, formatos: JPG/PNG/WebP
6. **Tipos de Eventos GPS**: START, NORMAL, STOP, BREAK, FUEL, LUNCH, PROBLEM, OBSERVATION, PHOTO, END
7. **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin) para gerenciar arquivos

---


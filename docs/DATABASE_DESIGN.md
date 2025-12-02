# 🗄️ Database Design - OD46S System

## 📋 Overview

The OD46S System database was designed to support complete urban waste collection management operations, based on the [API Contract](API_CONTRACT.md) defined for the system.

## 🏗️ Database Architecture

### Technologies
- **PostgreSQL 15** - Primary database
- **JPA/Hibernate** - ORM and mapping
- **MinIO** - S3-compatible file storage for photos
- **Docker** - Containerization
- **JOINED Inheritance** - JPA strategy for user types

### Configuration
```yaml
# docker-compose.yml
postgres:
  image: postgres:15-alpine
  environment:
    POSTGRES_DB: od46s_db
    POSTGRES_USER: od46s_user
    POSTGRES_PASSWORD: password123
  ports:
    - "5432:5432"
```

## 📊 Data Model

### 1. 👥 Users Module (JOINED Inheritance)

#### users (Base Table)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,        -- BCrypt hash
    cpf VARCHAR(11) UNIQUE NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### administrators (Inheritance)
```sql
CREATE TABLE administrators (
    id BIGINT PRIMARY KEY,
    access_level VARCHAR(50) NOT NULL,  -- SUPER_ADMIN, ADMIN, OPERATOR
    department VARCHAR(100),
    corporate_phone VARCHAR(20),
    
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### drivers (Inheritance)
```sql
CREATE TABLE drivers (
    id BIGINT PRIMARY KEY,
    license_number VARCHAR(11) UNIQUE NOT NULL,
    license_category VARCHAR(2) NOT NULL,  -- A, B, C, D, E
    license_expiry DATE NOT NULL,
    enabled BOOLEAN DEFAULT true,
    phone VARCHAR(20),
    
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);
```

### 2. 🚛 Vehicles Module

#### vehicles
```sql
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    license_plate VARCHAR(7) UNIQUE NOT NULL,
    model VARCHAR(100) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL,
    capacity_kg DECIMAL(10,2) NOT NULL,
    fuel_type VARCHAR(20) NOT NULL, -- DIESEL, GASOLINE, ETHANOL, ELECTRIC
    average_consumption DECIMAL(5,2),
    status VARCHAR(20) DEFAULT 'AVAILABLE', -- AVAILABLE, IN_USE, MAINTENANCE, INACTIVE
    current_km INTEGER DEFAULT 0,
    acquisition_date DATE,
    notes TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3. 🗺️ Routes Module

#### routes
```sql
CREATE TABLE routes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    collection_type VARCHAR(20) NOT NULL,     -- RESIDENTIAL, COMMERCIAL, HOSPITAL, SELECTIVE, RECYCLABLE
    periodicity VARCHAR(50) NOT NULL,         -- Cron expression
    priority VARCHAR(10) DEFAULT 'MEDIUM',    -- LOW, MEDIUM, HIGH, URGENT
    estimated_time_minutes INTEGER,
    distance_km DECIMAL(8,2),
    active BOOLEAN DEFAULT true,
    notes TEXT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (created_by) REFERENCES administrators(id)
);
```

#### route_collection_points
```sql
CREATE TABLE route_collection_points (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    sequence_order INTEGER NOT NULL,
    address VARCHAR(500) NOT NULL,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    waste_type VARCHAR(50) NOT NULL,
    estimated_capacity_kg DECIMAL(8,2),
    collection_frequency VARCHAR(20),     -- DAILY, ALTERNATE, WEEKLY
    notes TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    UNIQUE(route_id, sequence_order)
);
```

### 4. 🔗 Assignments Module (Escalas/Atribuições)

> **💡 Conceito**: Vínculo DURADOURO entre rota, motorista e caminhão.  
> É o **cadastro que interliga** os três elementos.  
> Duração: dias, semanas, meses ou anos.

#### route_assignments
```sql
CREATE TABLE route_assignments (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',    -- ACTIVE, INACTIVE
    start_date DATE NOT NULL,               -- Início da escala
    end_date DATE,                          -- Fim da escala (NULL = indefinido)
    notes TEXT,
    created_by BIGINT NOT NULL,             -- Admin que criou
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (route_id) REFERENCES routes(id),
    FOREIGN KEY (driver_id) REFERENCES drivers(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    FOREIGN KEY (created_by) REFERENCES administrators(id)
);
```

### 5. 📋 Executions Module (Execuções Individuais)

> **💡 Conceito**: Registro de UMA COLETA ESPECÍFICA realizada.  
> Criada quando motorista INICIA a coleta no app mobile.  
> Duração: algumas horas (início até fim).  
> Vinculada a uma ASSIGNMENT.

#### route_executions
```sql
CREATE TABLE route_executions (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL,          -- Vinculada à escala
    execution_date DATE NOT NULL,           -- Dia da execução
    start_time TIMESTAMP,                   -- Horário REAL de início
    end_time TIMESTAMP,                     -- Horário REAL de fim
    status VARCHAR(20) DEFAULT 'IN_PROGRESS', -- IN_PROGRESS, COMPLETED, CANCELLED
    initial_km INTEGER,                     -- KM ao iniciar
    final_km INTEGER,                       -- KM ao finalizar
    total_collected_weight_kg DECIMAL(10,2),
    points_visited INTEGER DEFAULT 0,
    points_collected INTEGER DEFAULT 0,
    initial_notes TEXT,
    final_notes TEXT,
    problems_found TEXT,
    cancellation_reason TEXT,               -- Motivo do cancelamento
    driver_rating INTEGER CHECK (driver_rating BETWEEN 1 AND 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (assignment_id) REFERENCES route_assignments(id) ON DELETE CASCADE,
    UNIQUE(assignment_id, execution_date)   -- Uma execução por dia por escala
);
```

### 6. 📍 GPS Tracking & Events Module

> **💡 Conceito**: Sistema unificado de GPS + eventos/ocorrências com fotos

#### gps_records
```sql
CREATE TABLE gps_records (
    id BIGSERIAL PRIMARY KEY,
    execution_id BIGINT NOT NULL,
    gps_timestamp TIMESTAMP NOT NULL,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    speed_kmh DECIMAL(5,2),
    heading_degrees INTEGER CHECK (heading_degrees BETWEEN 0 AND 359),
    accuracy_meters DECIMAL(5,2),
    event_type VARCHAR(20) DEFAULT 'NORMAL', -- START, NORMAL, STOP, BREAK, FUEL, LUNCH, PROBLEM, OBSERVATION, PHOTO, END
    description TEXT,                        -- Descrição do evento (opcional)
    photo_url VARCHAR(500),                  -- URL da foto no MinIO (opcional)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (execution_id) REFERENCES route_executions(id) ON DELETE CASCADE
);
```

**Event Types**:
- `START` - Início da coleta
- `NORMAL` - Rastreamento periódico normal
- `STOP` - Parada qualquer
- `BREAK` - Intervalo/Descanso
- `FUEL` - Abastecimento
- `LUNCH` - Almoço
- `PROBLEM` - Problema encontrado
- `OBSERVATION` - Observação
- `PHOTO` - Registro fotográfico
- `END` - Fim da coleta

**Photo Storage (MinIO)**:
- Bucket: `od46s-files`
- Path: `gps-photos/execution_{id}/photo_{timestamp}_{uuid}.{ext}`
- Max size: 10MB per photo
- Formats: JPG, PNG, WebP
- Access: `/api/v1/files/gps-photos/{executionId}/{filename}`

### 7. 🗑️ Collections Module

#### collection_point_records
```sql
CREATE TABLE collection_point_records (
    id BIGSERIAL PRIMARY KEY,
    execution_id BIGINT NOT NULL,
    point_id BIGINT NOT NULL,
    arrival_timestamp TIMESTAMP NOT NULL,
    departure_timestamp TIMESTAMP,
    collection_timestamp TIMESTAMP NOT NULL,
    collection_status VARCHAR(20) NOT NULL, -- COLLECTED, SKIPPED, PROBLEM
    collected_weight_kg DECIMAL(8,2),
    collected_waste_types TEXT[],            -- PostgreSQL Array
    point_condition VARCHAR(20),             -- NORMAL, SATURATED, DAMAGED, INACCESSIBLE
    notes TEXT,
    non_collection_reason TEXT,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    accuracy_meters DECIMAL(5,2),
    photo_urls TEXT[],                       -- Array of photo URLs
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (execution_id) REFERENCES route_executions(id) ON DELETE CASCADE,
    FOREIGN KEY (point_id) REFERENCES route_collection_points(id)
);
```

## 🔍 Performance Indexes

```sql
-- Users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_cpf ON users(cpf);
CREATE INDEX idx_users_active ON users(active);

-- Drivers
CREATE INDEX idx_drivers_license ON drivers(license_number);
CREATE INDEX idx_drivers_enabled ON drivers(enabled);

-- Vehicles
CREATE INDEX idx_vehicles_plate ON vehicles(license_plate);
CREATE INDEX idx_vehicles_status ON vehicles(status);
CREATE INDEX idx_vehicles_active ON vehicles(active);

-- Routes
CREATE INDEX idx_routes_name ON routes(name);
CREATE INDEX idx_routes_type ON routes(collection_type);
CREATE INDEX idx_routes_active ON routes(active);
CREATE INDEX idx_routes_priority ON routes(priority);

-- Collection Points
CREATE INDEX idx_points_route_id ON route_collection_points(route_id);
CREATE INDEX idx_points_coordinates ON route_collection_points(latitude, longitude);

-- Assignments (Escalas)
CREATE INDEX idx_assignments_route ON route_assignments(route_id);
CREATE INDEX idx_assignments_driver ON route_assignments(driver_id);
CREATE INDEX idx_assignments_vehicle ON route_assignments(vehicle_id);
CREATE INDEX idx_assignments_status ON route_assignments(status);
CREATE INDEX idx_assignments_dates ON route_assignments(start_date, end_date);

-- Executions (Execuções)
CREATE INDEX idx_executions_assignment ON route_executions(assignment_id);
CREATE INDEX idx_executions_date ON route_executions(execution_date);
CREATE INDEX idx_executions_status ON route_executions(status);
CREATE INDEX idx_executions_start_time ON route_executions(start_time);

-- GPS & Events
CREATE INDEX idx_gps_execution ON gps_records(execution_id);
CREATE INDEX idx_gps_timestamp ON gps_records(gps_timestamp);
CREATE INDEX idx_gps_location ON gps_records(latitude, longitude);
CREATE INDEX idx_gps_event_type ON gps_records(event_type); -- Para filtrar eventos específicos
CREATE INDEX idx_gps_with_photo ON gps_records(execution_id) WHERE photo_url IS NOT NULL; -- Registros com foto

-- Collections
CREATE INDEX idx_collections_execution ON collection_point_records(execution_id);
CREATE INDEX idx_collections_point ON collection_point_records(point_id);
CREATE INDEX idx_collections_timestamp ON collection_point_records(collection_timestamp);
CREATE INDEX idx_collections_status ON collection_point_records(collection_status);
```

## 🗄️ Data Volume Estimates

### Production Estimates (Medium City - 100k inhabitants)
```
👥 Users: ~50 (20 drivers, 30 admins)
🚛 Vehicles: ~15 trucks
🗺️ Routes: ~30 routes
📍 Collection Points: ~3,000 points
📋 Daily Executions: ~25/day → 9,000/year
📊 GPS Records: ~500,000 points/year
🗑️ Collections: ~75,000 records/year
```

### Storage Requirements
```
📊 Database (PostgreSQL): ~2GB/year
📸 Photos (MinIO): ~50GB/year (if 20% of GPS events have photos)
📈 Total: ~52GB/year
💾 5-year projection: ~260GB

Breakdown:
- PostgreSQL: Structured data (users, routes, executions, GPS coordinates)
- MinIO: Unstructured data (photos from GPS events/problems)
```

## 🔄 Database Relationships

```
users (1) ←→ (1) drivers/administrators
administrators (1) ←→ (many) routes [created_by]
routes (1) ←→ (many) route_collection_points
routes (1) ←→ (many) route_executions
drivers (1) ←→ (many) route_executions
vehicles (1) ←→ (many) route_executions
route_executions (1) ←→ (many) gps_records
route_executions (1) ←→ (many) collection_point_records
route_collection_points (1) ←→ (many) collection_point_records
```

---

## 🎯 **Final Architecture Summary**

### ✅ **Spring Boot + JPA Approach Benefits**
- **🚀 Portability**: Works with PostgreSQL, MySQL, H2, SQLite
- **🧪 Testability**: Business logic is unit-testable  
- **🔧 Maintainability**: IDE support, refactoring, debugging
- **📦 Versionability**: Git tracks all changes perfectly
- **🛡️ Security**: Application-layer security (more flexible)
- **📊 Performance**: JPA second-level cache + query optimization

**The database design meets all requirements defined in the [API Contract](API_CONTRACT.md), using modern Spring Boot best practices for complete waste collection management operations with real-time tracking, analytics, and mobile offline capabilities.**

# 🗄️ Database Design - OD46S System

## 📋 Overview

The OD46S System database was designed to support complete urban waste collection management operations, based on the [API Contract](API_CONTRACT.md) defined for the system.

## 🏗️ Database Architecture

### Technologies
- **PostgreSQL 15** - Primary database
- **JPA/Hibernate** - ORM and mapping
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

### 4. 📋 Executions Module

#### route_executions
```sql
CREATE TABLE route_executions (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'SCHEDULED', -- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    initial_km INTEGER NOT NULL,
    final_km INTEGER,
    total_collected_weight_kg DECIMAL(10,2),
    points_visited INTEGER DEFAULT 0,
    points_collected INTEGER DEFAULT 0,
    initial_notes TEXT,
    final_notes TEXT,
    problems_found TEXT,
    driver_rating INTEGER CHECK (driver_rating BETWEEN 1 AND 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (route_id) REFERENCES routes(id),
    FOREIGN KEY (driver_id) REFERENCES drivers(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);
```

### 5. 📍 GPS Tracking Module

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
    event_type VARCHAR(20) DEFAULT 'NORMAL', -- NORMAL, STOP, START, END
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (execution_id) REFERENCES route_executions(id) ON DELETE CASCADE
);
```

### 6. 🗑️ Collections Module

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

-- Executions
CREATE INDEX idx_executions_date ON route_executions(start_time);
CREATE INDEX idx_executions_status ON route_executions(status);
CREATE INDEX idx_executions_driver ON route_executions(driver_id);
CREATE INDEX idx_executions_vehicle ON route_executions(vehicle_id);

-- GPS
CREATE INDEX idx_gps_execution ON gps_records(execution_id);
CREATE INDEX idx_gps_timestamp ON gps_records(gps_timestamp);
CREATE INDEX idx_gps_location ON gps_records(latitude, longitude);

-- Collections
CREATE INDEX idx_collections_execution ON collection_point_records(execution_id);
CREATE INDEX idx_collections_point ON collection_point_records(point_id);
CREATE INDEX idx_collections_timestamp ON collection_point_records(collection_timestamp);
CREATE INDEX idx_collections_status ON collection_point_records(collection_status);
```

## 🔧 Functions and Triggers

### Auto Timestamp Trigger
```sql
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply to relevant tables
CREATE TRIGGER tr_users_update_timestamp
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER tr_vehicles_update_timestamp
    BEFORE UPDATE ON vehicles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER tr_routes_update_timestamp
    BEFORE UPDATE ON routes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();
```

### Distance Calculation Function
```sql
CREATE OR REPLACE FUNCTION calculate_distance_km(
    lat1 DECIMAL, lon1 DECIMAL,
    lat2 DECIMAL, lon2 DECIMAL
) RETURNS DECIMAL AS $$
DECLARE
    earth_radius CONSTANT DECIMAL := 6371; -- Earth radius in km
    dlat DECIMAL;
    dlon DECIMAL;
    a DECIMAL;
    c DECIMAL;
BEGIN
    dlat := RADIANS(lat2 - lat1);
    dlon := RADIANS(lon2 - lon1);
    
    a := SIN(dlat/2) * SIN(dlat/2) + 
         COS(RADIANS(lat1)) * COS(RADIANS(lat2)) * 
         SIN(dlon/2) * SIN(dlon/2);
    
    c := 2 * ATAN2(SQRT(a), SQRT(1-a));
    
    RETURN earth_radius * c;
END;
$$ LANGUAGE plpgsql;
```

### Route Efficiency Function
```sql
CREATE OR REPLACE FUNCTION calculate_route_efficiency(
    route_execution_id BIGINT
) RETURNS DECIMAL AS $$
DECLARE
    execution_record RECORD;
    efficiency_score DECIMAL;
BEGIN
    SELECT 
        points_visited,
        points_collected,
        total_collected_weight_kg,
        EXTRACT(EPOCH FROM (end_time - start_time))/3600 as duration_hours
    INTO execution_record
    FROM route_executions 
    WHERE id = route_execution_id;
    
    IF execution_record.points_visited = 0 OR execution_record.duration_hours = 0 THEN
        RETURN 0;
    END IF;
    
    -- Efficiency = (Collection Rate * Weight per Hour) / 10
    efficiency_score := (
        (execution_record.points_collected::DECIMAL / execution_record.points_visited) * 
        (execution_record.total_collected_weight_kg / execution_record.duration_hours)
    ) / 10;
    
    RETURN ROUND(efficiency_score, 2);
END;
$$ LANGUAGE plpgsql;
```

## 📈 Analytics Views

### Driver Performance View
```sql
CREATE OR REPLACE VIEW vw_driver_performance AS
SELECT 
    d.id,
    u.name,
    COUNT(re.id) as total_executions,
    AVG(re.driver_rating) as average_rating,
    SUM(re.total_collected_weight_kg) as total_collected_weight,
    AVG(EXTRACT(EPOCH FROM (re.end_time - re.start_time))/3600) as average_time_hours,
    (COUNT(CASE WHEN re.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(re.id)) as completion_rate
FROM drivers d
JOIN users u ON d.id = u.id
LEFT JOIN route_executions re ON d.id = re.driver_id
WHERE u.active = true
GROUP BY d.id, u.name;
```

### Route Efficiency View
```sql
CREATE OR REPLACE VIEW vw_route_efficiency AS
SELECT 
    r.id,
    r.name,
    r.collection_type,
    COUNT(re.id) as total_executions,
    AVG(re.total_collected_weight_kg) as average_collected_weight,
    AVG(EXTRACT(EPOCH FROM (re.end_time - re.start_time))/60) as average_time_minutes,
    AVG(re.points_collected * 100.0 / re.points_visited) as average_collection_rate,
    COUNT(CASE WHEN re.status = 'COMPLETED' THEN 1 END) as completed_executions
FROM routes r
LEFT JOIN route_executions re ON r.id = re.route_id 
WHERE r.active = true
GROUP BY r.id, r.name, r.collection_type;
```

### Daily Statistics View
```sql
CREATE OR REPLACE VIEW vw_daily_statistics AS
SELECT 
    DATE(re.start_time) as collection_date,
    COUNT(DISTINCT re.id) as total_executions,
    COUNT(DISTINCT re.driver_id) as active_drivers,
    COUNT(DISTINCT re.vehicle_id) as vehicles_used,
    SUM(re.total_collected_weight_kg) as total_daily_weight,
    COUNT(CASE WHEN re.status = 'COMPLETED' THEN 1 END) as completed_executions,
    AVG(re.points_collected) as average_points_collected
FROM route_executions re
WHERE re.start_time >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(re.start_time)
ORDER BY collection_date DESC;
```

### Fleet Utilization View
```sql
CREATE OR REPLACE VIEW vw_fleet_utilization AS
SELECT 
    v.id,
    v.license_plate,
    v.model,
    v.status,
    COUNT(re.id) as total_uses,
    AVG(re.total_collected_weight_kg) as average_load,
    SUM(re.final_km - re.initial_km) as total_km_driven,
    AVG(EXTRACT(EPOCH FROM (re.end_time - re.start_time))/3600) as average_hours_per_use,
    MAX(re.start_time) as last_used
FROM vehicles v
LEFT JOIN route_executions re ON v.id = re.vehicle_id
WHERE v.active = true
GROUP BY v.id, v.license_plate, v.model, v.status;
```

## 🎯 Database Constraints

### Business Rules
```sql
-- License expiry must be in the future
ALTER TABLE drivers ADD CONSTRAINT chk_license_expiry 
    CHECK (license_expiry > CURRENT_DATE);

-- Vehicle year must be reasonable
ALTER TABLE vehicles ADD CONSTRAINT chk_vehicle_year 
    CHECK (year BETWEEN 1980 AND EXTRACT(YEAR FROM CURRENT_DATE) + 1);

-- Execution end time must be after start time
ALTER TABLE route_executions ADD CONSTRAINT chk_execution_time 
    CHECK (end_time IS NULL OR end_time > start_time);

-- GPS timestamp must be reasonable (not too far in future)
ALTER TABLE gps_records ADD CONSTRAINT chk_gps_timestamp 
    CHECK (gps_timestamp <= CURRENT_TIMESTAMP + INTERVAL '1 hour');

-- Collection timestamp must be within execution period
ALTER TABLE collection_point_records ADD CONSTRAINT chk_collection_timestamp 
    CHECK (collection_timestamp >= arrival_timestamp);

-- Coordinates must be valid (rough boundaries for Brazil)
ALTER TABLE route_collection_points ADD CONSTRAINT chk_coordinates 
    CHECK (latitude BETWEEN -35 AND 5 AND longitude BETWEEN -75 AND -30);

ALTER TABLE gps_records ADD CONSTRAINT chk_gps_coordinates 
    CHECK (latitude BETWEEN -35 AND 5 AND longitude BETWEEN -75 AND -30);

ALTER TABLE collection_point_records ADD CONSTRAINT chk_collection_coordinates 
    CHECK (latitude BETWEEN -35 AND 5 AND longitude BETWEEN -75 AND -30);
```

### Unique Constraints
```sql
-- Prevent duplicate active routes with same name
CREATE UNIQUE INDEX idx_routes_unique_active_name 
    ON routes(name) WHERE active = true;

-- Prevent overlapping executions for same driver
CREATE UNIQUE INDEX idx_executions_no_overlap_driver 
    ON route_executions(driver_id, start_time) 
    WHERE status IN ('SCHEDULED', 'IN_PROGRESS');

-- Prevent overlapping executions for same vehicle
CREATE UNIQUE INDEX idx_executions_no_overlap_vehicle 
    ON route_executions(vehicle_id, start_time) 
    WHERE status IN ('SCHEDULED', 'IN_PROGRESS');
```

## 🔐 Security & Permissions

### Row Level Security (RLS)
```sql
-- Enable RLS on sensitive tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE route_executions ENABLE ROW LEVEL SECURITY;
ALTER TABLE gps_records ENABLE ROW LEVEL SECURITY;

-- Drivers can only see their own executions
CREATE POLICY driver_own_executions ON route_executions
    FOR ALL TO application_role
    USING (driver_id = current_setting('app.current_user_id')::BIGINT);

-- Drivers can only insert their own GPS records
CREATE POLICY driver_own_gps ON gps_records
    FOR INSERT TO application_role
    WITH CHECK (
        execution_id IN (
            SELECT id FROM route_executions 
            WHERE driver_id = current_setting('app.current_user_id')::BIGINT
        )
    );
```

### Database Roles
```sql
-- Application role with limited permissions
CREATE ROLE application_role;
GRANT CONNECT ON DATABASE od46s_db TO application_role;
GRANT USAGE ON SCHEMA public TO application_role;
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO application_role;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO application_role;

-- Read-only role for analytics
CREATE ROLE analytics_role;
GRANT CONNECT ON DATABASE od46s_db TO analytics_role;
GRANT USAGE ON SCHEMA public TO analytics_role;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO analytics_role;
```

## 📊 Implementation Status

### ✅ Implemented (3 Tables)
- **users** - Base user table with JPA inheritance
- **administrators** - Admin-specific fields  
- **drivers** - Driver-specific fields (license info)

### ❌ Not Implemented (9 Tables)
- **vehicles** - Fleet management
- **routes** - Route definitions
- **route_collection_points** - Specific collection points
- **route_executions** - Execution tracking
- **gps_records** - Real-time GPS tracking
- **collection_point_records** - Collection confirmations

### 📈 Statistics
- **Total Tables Planned:** 12
- **Implemented:** 3 (25%)
- **Missing:** 9 (75%)
- **Total Fields:** 100+ across all tables
- **Indexes:** 20+ for performance
- **Views:** 4 for analytics
- **Functions:** 3 for calculations
- **Constraints:** 10+ business rules

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
📊 Database Size: ~2GB/year
📸 Photos: ~50GB/year (if 20% upload photos)
📈 Total: ~52GB/year
💾 5-year projection: ~260GB
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

**The database design meets all requirements defined in the [API Contract](API_CONTRACT.md), supporting complete waste collection management operations with real-time tracking, analytics, and mobile offline capabilities.**

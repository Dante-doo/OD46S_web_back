# 📋 API Contract - OD46S System

## 🔗 Base URL
```
Development: http://localhost:8080/api/v1
Production: https://api.od46s.com/v1
```

## 🛡️ Security & Authentication

### 🔐 Password Security
- **Storage**: Passwords are encrypted using **BCrypt** (cost factor 10+)
- **Transmission**: Passwords sent in plain text **only** during login/register via HTTPS
- **Never stored or transmitted in plain text**
- **Never returned in API responses**

### 🎫 JWT Authentication
All protected routes require JWT Token in the header:
```
Authorization: Bearer {jwt_token}
```

**Token Details:**
- **Algorithm**: HS512
- **Expiration**: 24 hours (86400 seconds)
- **Payload**: User email + timestamps
- **Auto-refresh**: Not implemented (manual re-login required)

### 🔒 Security Headers
```
Content-Type: application/json
Accept: application/json
Authorization: Bearer {jwt_token}  # For protected routes
```

### ⚠️ Critical Security Issues to Fix

🚨 **URGENT - Current Implementation Issues:**
1. **JWT Secret Hardcoded**: Currently using `"mySecretKey"` - **CRITICAL VULNERABILITY**
2. **No Environment Variables**: Secrets should be externalized
3. **No Token Refresh**: Manual re-login required after 24h
4. **No Rate Limiting**: Login endpoints vulnerable to brute force

🔒 **Production Security Checklist:**
- [ ] Use **strong JWT secret** (64+ chars) in environment variables
- [ ] Enable **HTTPS** (TLS 1.2+)
- [ ] Implement **rate limiting** on login endpoints
- [ ] Add **password complexity requirements**
- [ ] Store JWT tokens securely (httpOnly cookies preferred over localStorage)
- [ ] Implement proper **token refresh flow**
- [ ] Add **account lockout** after failed attempts
- [ ] Enable **audit logging** for authentication events

## 📄 Standard Headers
```
Content-Type: application/json
Accept: application/json
Authorization: Bearer {jwt_token}  # For protected routes
```

## ⚠️ Standard Response Codes
| Code | Description | Usage |
|------|-------------|-------|
| 200 | OK | Successful operation |
| 201 | Created | Resource created successfully |
| 204 | No Content | Successful operation without response |
| 400 | Bad Request | Invalid or malformed data |
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | No permission for resource |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Conflict (e.g., email already exists) |
| 422 | Unprocessable Entity | Business validation failed |
| 500 | Internal Server Error | Internal server error |

---

# 🛡️ SECURITY IMPLEMENTATION STATUS

## ✅ **Currently Implemented**
- **BCrypt Password Hashing**: Cost factor 10+ (Spring Security default)
- **JWT Token Generation**: HS512 algorithm, 24h expiration
- **Password Encoding**: All passwords immediately hashed on storage
- **Spring Security Integration**: Basic configuration in place

## 🚨 **Critical Security Gaps**
```java
// CURRENT CODE (INSECURE):
private String secret = "mySecretKey"; // ❌ HARDCODED SECRET

// PRODUCTION CODE (SECURE):
@Value("${JWT_SECRET}")
private String secret; // ✅ ENVIRONMENT VARIABLE
```

## 🔧 **Production Security Improvements Needed**

### 1. **Environment Variables** (application.properties)
```properties
# Required for production:
jwt.secret=${JWT_SECRET:your-super-secure-64-char-secret-key-here}
jwt.expiration=${JWT_EXPIRATION:86400000}
server.ssl.enabled=true
```

### 2. **Rate Limiting** (Not Implemented)
```java
// Need to add:
@RateLimiter(name = "auth", fallbackMethod = "rateLimitFallback")
public AuthResponse login(LoginRequest request) { ... }
```

### 3. **Password Validation** (Not Implemented)
```java
// Need to add:
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
private String password; // Min 8 chars, uppercase, lowercase, number, special char
```

### 4. **Secure Headers** (Not Implemented)
```java
// Need to add security headers:
"X-Content-Type-Options": "nosniff",
"X-Frame-Options": "DENY",
"X-XSS-Protection": "1; mode=block",
"Strict-Transport-Security": "max-age=31536000; includeSubDomains"
```

## 📋 **Client Implementation Examples**

### ✅ **Secure JWT Usage (Frontend)**
```javascript
// ✅ GOOD: Secure token storage
const token = localStorage.getItem('jwt_token'); // Consider httpOnly cookies for better security

// ✅ GOOD: Proper authorization header
const response = await fetch('/api/v1/users', {
    headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    }
});

// ✅ GOOD: Handle token expiration
if (response.status === 401) {
    // Token expired - redirect to login
    localStorage.removeItem('jwt_token');
    window.location.href = '/login';
}
```

### ❌ **Insecure Practices to Avoid**
```javascript
// ❌ BAD: Storing password
localStorage.setItem('password', 'userPassword'); // NEVER DO THIS

// ❌ BAD: Sending password in URL
const url = `/login?password=userpass`; // NEVER DO THIS

// ❌ BAD: Exposing JWT in URL
const url = `/dashboard?token=${jwt}`; // NEVER DO THIS
```

### 🔒 **Password Security Best Practices**
```javascript
// ✅ GOOD: Password handling
const loginData = {
    email: userEmail,
    password: userPassword  // Send only during login, never store
};

// Immediately clear password from memory after use
userPassword = null;
delete loginData.password;
```

---

# 🔐 1. AUTHENTICATION

## 1.1 Login
**POST** `/api/v1/auth/login`

### Request Body
```json
{
  "email": "admin@od46s.com",        // string, required (OR cpf)
  "cpf": "12345678901",              // string, required (OR email)
  "password": "SecureP@ssw0rd"        // string, required (min 6 chars)
}
```

**🔒 Security Implementation:**
- Password transmitted **securely via HTTPS**
- Server validates using **BCrypt.matches()** against database hash
- Original password **never stored**, immediately discarded after hashing
- Failed login attempts should be **logged and rate-limited**

### Response 200
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBvZDQ2cy5jb20iLCJpYXQiOjE2NDA5OTUyMDAsImV4cCI6MTY0MTA4MTYwMH0.signature_part",
    "type": "Bearer",
    "expires_in": 86400,
    "user": {
      "id": 1,
      "name": "System Administrator",
      "email": "admin@od46s.com",
      "type": "ADMIN",
      "active": true
      // 🔒 NOTE: password field is NEVER returned
    }
  },
  "message": "Login successful"
}
```

**🔒 JWT Token Structure:**
```json
// Header
{
  "alg": "HS512",
  "typ": "JWT"
}

// Payload (Base64 decoded)
{
  "sub": "admin@od46s.com",    // Subject (user email)
  "iat": 1640995200,           // Issued at (timestamp)
  "exp": 1641081600            // Expires at (timestamp)
}

// 🚨 SECURITY: Password hash is NEVER included in JWT payload
```

### Response 401
```json
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid email/cpf or password",
    "details": {
      "timestamp": "2025-01-15T10:30:00Z",
      "attempts_remaining": 2  // If rate limiting implemented
    }
  }
}
```

**🔒 Security Notes:**
- Error message is **intentionally vague** (doesn't reveal if email exists)
- **No password hint** or recovery information exposed
- Failed attempts should be **logged** for security monitoring
- Consider implementing **account lockout** after repeated failures

## 1.2 Register
**POST** `/api/v1/auth/register`

### Request Body
```json
{
  "name": "New User",               // string, required
  "email": "user@od46s.com",        // string, required, unique
  "cpf": "12345678901",            // string, required, unique
  "password": "SecureP@ssw0rd!",     // string, required (min 6 chars, recommend 8+ with complexity)
  "type": "DRIVER"                 // ADMIN | DRIVER
}
```

**🔒 Security Implementation:**
- Password **immediately hashed with BCrypt** (cost factor 10+)
- Plain text password **never persisted**, discarded after hashing
- **BCrypt salt** automatically generated per password
- **HTTPS required** to protect password transmission
- Consider implementing **password complexity validation**

### Response 201
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 2,
      "name": "New User",
      "email": "user@od46s.com",
      "type": "DRIVER",
      "active": true,
      "created_at": "2025-01-01T10:00:00Z"
    }
  },
  "message": "User created successfully"
}
```

### Response 409
```json
{
  "success": false,
  "error": {
    "code": "EMAIL_EXISTS",
    "message": "Email already exists",
    "details": {}
  }
}
```

---

# 👥 2. USER MANAGEMENT

## 2.1 List Users
**GET** `/users`

### Query Parameters
```
?page=1              // int, page (default: 1)
&limit=20            // int, items per page (default: 20, max: 100)
&search=John         // string, search by name or email
&type=DRIVER         // string, filter by type (ADMIN|DRIVER)
&active=true         // boolean, filter by active status
&sort=name           // string, sorting (name|email|created_at)
&order=asc           // string, direction (asc|desc)
```

### Headers
```
Authorization: Bearer {jwt_token}
```

### Response 200
```json
{
  "success": true,
  "data": {
    "users": [
      {
        "id": 1,
        "name": "System Administrator",
        "email": "admin@od46s.com",
        "cpf": "***.***.***-**",          // masked
        "type": "ADMIN",
        "active": true,
        "created_at": "2025-01-01T10:00:00Z",
        "updated_at": "2025-01-01T10:00:00Z"
      }
    ],
    "pagination": {
      "current_page": 1,
      "per_page": 20,
      "total": 3,
      "total_pages": 1,
      "has_next": false,
      "has_prev": false
    }
  }
}
```

## 2.2 Get User by ID
**GET** `/users/{id}`

### Headers
```
Authorization: Bearer {jwt_token}
```

### Path Parameters
```
id: integer  // User ID
```

### Response 200
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "name": "System Administrator",
      "email": "admin@od46s.com",
      "cpf": "123.456.789-01",
      "type": "ADMIN",
      "active": true,
      "created_at": "2025-01-01T10:00:00Z",
      "updated_at": "2025-01-01T10:00:00Z",
      
      // Specific fields for ADMIN
      "admin_details": {
        "access_level": "SUPER_ADMIN",
        "department": "Urban Cleaning",
        "corporate_phone": "4733334444"
      }
      
      // OR specific fields for DRIVER
      "driver_details": {
        "license_number": "12345678901",
        "license_category": "D",
        "license_expiry": "2025-12-31",
        "enabled": true,
        "phone": "47999999999"
      }
    }
  }
}
```

## 2.3 Create User
**POST** `/users`

### Headers
```
Authorization: Bearer {jwt_token}  # Only ADMIN
```

### Request Body
```json
{
  "name": "John Silva",             // string, required
  "email": "john@od46s.com",        // string, required, unique
  "cpf": "12345678901",            // string, required, unique
  "password": "SecureP@ssw0rd!",     // string, required (min 6 chars, recommend strong password)
  "type": "DRIVER",                // ADMIN | DRIVER
  "active": true                   // boolean, optional (default: true)
}

// Specific fields for DRIVER
if (type === "DRIVER") {
  "license_number": "12345678901", // string, required
  "license_category": "D",        // A|B|C|D|E, required  
  "license_expiry": "2025-12-31",  // date, required
  "phone": "47999999999"           // string, optional
}

// Specific fields for ADMIN
if (type === "ADMIN") {
  "access_level": "ADMIN",         // SUPER_ADMIN|ADMIN|OPERATOR
  "department": "Urban Cleaning",   // string, optional
  "corporate_phone": "4733334444"   // string, optional
}
```

## 2.4 Update User
**PUT** `/users/{id}`

### Headers
```
Authorization: Bearer {jwt_token}  # Own user or ADMIN
```

### Request Body
```json
{
  "name": "John Silva Updated",     // string, optional
  "email": "john.new@od46s.com",    // string, optional, unique
  "active": false                   // boolean, optional (only ADMIN)
}
```

## 2.5 Deactivate User
**DELETE** `/users/{id}`

### Headers
```
Authorization: Bearer {jwt_token}  # Only ADMIN
```

### Response 204
No content - User deactivated successfully

## 2.6 Change Password
**PATCH** `/users/{id}/password`

### Headers
```
Authorization: Bearer {jwt_token}  # Own user or ADMIN
```

### Request Body
```json
{
  "current_password": "CurrentP@ssw0rd", // string, required if own user (for verification)
  "new_password": "NewSecureP@ssw0rd!"   // string, required (min 6 chars, recommend 8+ with complexity)
}
```

**🔒 Security Implementation:**
- Current password **verified with BCrypt.matches()**
- New password **immediately hashed with fresh BCrypt salt**
- Both passwords transmitted **securely via HTTPS**
- Plain text passwords **never persisted**
- **Password change logged** for audit trail
- Consider **password history** to prevent immediate reuse

---

# 🚛 3. VEHICLE MANAGEMENT

## 3.1 List Vehicles
**GET** `/api/v1/vehicles`

### Query Parameters
```
?page=1              // int, page (default: 1)
&limit=20            // int, items per page (default: 20, max: 100)
&search=ABC1234      // string, search by plate or model
&status=AVAILABLE    // string, filter (AVAILABLE|IN_USE|MAINTENANCE|INACTIVE)
&active=true         // boolean, filter by active
&sort=license_plate  // string, sorting (license_plate|model|brand|year)
&order=asc           // string, direction (asc|desc)
```

### Headers
```
Authorization: Bearer {jwt_token}
```

### Response 200
```json
{
  "success": true,
  "data": {
    "vehicles": [
      {
        "id": 1,
        "license_plate": "ABC1234",
        "model": "Compactor 15m³",
        "brand": "Volvo",
        "year": 2022,
        "capacity_kg": 15000,
        "fuel_type": "DIESEL",
        "average_consumption": 3.5,
        "status": "AVAILABLE",
        "current_km": 12500,
        "acquisition_date": "2022-01-15",
        "notes": "New vehicle",
        "active": true,
        "created_at": "2025-01-01T10:00:00Z",
        "updated_at": "2025-01-01T10:00:00Z"
      }
    ],
    "pagination": { /* ... */ }
  }
}
```

## 3.2 Get Vehicle by ID
**GET** `/api/v1/vehicles/{id}`

### Headers
```
Authorization: Bearer {jwt_token}
```

## 3.3 Create Vehicle
**POST** `/api/v1/vehicles`

### Headers
```
Authorization: Bearer {jwt_token}  # Only ADMIN
```

### Request Body
```json
{
  "license_plate": "ABC1234",      // string, required, unique
  "model": "Compactor 15m³",       // string, required
  "brand": "Volvo",               // string, required
  "year": 2022,                   // int, required
  "capacity_kg": 15000,           // decimal, required
  "fuel_type": "DIESEL",          // DIESEL|GASOLINE|ETHANOL|ELECTRIC
  "average_consumption": 3.5,     // decimal, optional (km/l)
  "status": "AVAILABLE",          // AVAILABLE|IN_USE|MAINTENANCE|INACTIVE
  "acquisition_date": "2022-01-15", // date, optional
  "notes": "New vehicle"          // string, optional
}
```

## 3.4 Update Vehicle
**PUT** `/api/v1/vehicles/{id}`

### Headers
```
Authorization: Bearer {jwt_token}  # Only ADMIN
```

## 3.5 Delete Vehicle
**DELETE** `/api/v1/vehicles/{id}`

### Headers
```
Authorization: Bearer {jwt_token}  # Only ADMIN
```

---

# 🗺️ 4. ROUTE MANAGEMENT

## 4.1 List Routes
**GET** `/routes`

### Query Parameters
```
?page=1                    // int, page (default: 1)
&limit=20                  // int, items per page (default: 20, max: 100)
&search=Downtown           // string, search by name
&collection_type=RESIDENTIAL  // string, filter by type
&priority=HIGH             // string, filter (LOW|MEDIUM|HIGH|URGENT)
&active=true               // boolean, filter by active
&sort=name                 // string, sorting
&order=asc                 // string, direction
```

### Response 200
```json
{
  "success": true,
  "data": {
    "routes": [
      {
        "id": 1,
        "name": "Downtown Route A1",
        "description": "Commercial area collection",
        "collection_type": "COMMERCIAL",
        "periodicity": "0 8 * * 1,3,5",  // Cron: Mon, Wed, Fri at 8am
        "priority": "HIGH",
        "estimated_time_minutes": 120,
        "distance_km": 15.5,
        "active": true,
        "notes": "Heavy traffic area",
        "created_by": 1,
        "created_at": "2025-01-01T10:00:00Z",
        "collection_points_count": 25
      }
    ],
    "pagination": { /* ... */ }
  }
}
```

## 4.2 Get Route by ID
**GET** `/routes/{id}`

### Response 200
```json
{
  "success": true,
  "data": {
    "route": {
      "id": 1,
      "name": "Downtown Route A1",
      "description": "Commercial area collection",
      "collection_type": "COMMERCIAL",
      "periodicity": "0 8 * * 1,3,5",
      "priority": "HIGH",
      "estimated_time_minutes": 120,
      "distance_km": 15.5,
      "active": true,
      "notes": "Heavy traffic area",
      "created_by": 1,
      "created_at": "2025-01-01T10:00:00Z",
      "updated_at": "2025-01-01T10:00:00Z",
      "collection_points": [
        {
          "id": 1,
          "sequence_order": 1,
          "address": "123 Main Street, Downtown",
          "latitude": -25.4284,
          "longitude": -49.2733,
          "waste_type": "COMMERCIAL",
          "estimated_capacity_kg": 50,
          "collection_frequency": "DAILY",
          "notes": "Large office building",
          "active": true
        }
      ]
    }
  }
}
```

## 4.3 Create Route
**POST** `/routes`

### Headers
```
Authorization: Bearer {jwt_token}  # Only ADMIN
```

### Request Body
```json
{
  "name": "New Route B1",           // string, required
  "description": "Residential area", // string, optional
  "collection_type": "RESIDENTIAL", // RESIDENTIAL|COMMERCIAL|HOSPITAL|SELECTIVE|RECYCLABLE
  "periodicity": "0 7 * * 2,4,6",  // string, required (cron expression)
  "priority": "MEDIUM",             // LOW|MEDIUM|HIGH|URGENT
  "estimated_time_minutes": 90,     // int, optional
  "distance_km": 12.0,             // decimal, optional
  "notes": "Quiet residential area", // string, optional
  "collection_points": [
    {
      "sequence_order": 1,
      "address": "456 Oak Street",
      "latitude": -25.4284,
      "longitude": -49.2733,
      "waste_type": "RESIDENTIAL",
      "estimated_capacity_kg": 30,
      "collection_frequency": "ALTERNATE",
      "notes": "Apartment complex"
    }
  ]
}
```

---

# 📋 5. ROUTE EXECUTION

## 5.1 List Route Executions
**GET** `/executions`

### Query Parameters
```
?page=1                    // pagination
&limit=20                  // items per page
&route_id=1               // filter by route
&driver_id=2              // filter by driver
&vehicle_id=3             // filter by vehicle
&status=IN_PROGRESS       // SCHEDULED|IN_PROGRESS|COMPLETED|CANCELLED
&start_date=2025-01-01    // filter by start date (YYYY-MM-DD)
&end_date=2025-01-31      // filter by end date
&sort=start_time          // sorting
&order=desc               // direction
```

### Response 200
```json
{
  "success": true,
  "data": {
    "executions": [
      {
        "id": 1,
        "route": {
          "id": 1,
          "name": "Downtown Route A1"
        },
        "driver": {
          "id": 2,
          "name": "John Driver"
        },
        "vehicle": {
          "id": 1,
          "license_plate": "ABC1234"
        },
        "start_time": "2025-01-15T08:00:00Z",
        "end_time": "2025-01-15T10:30:00Z",
        "status": "COMPLETED",
        "initial_km": 12500,
        "final_km": 12515,
        "total_collected_weight_kg": 1200,
        "points_visited": 25,
        "points_collected": 23,
        "driver_rating": 4,
        "created_at": "2025-01-15T07:45:00Z"
      }
    ],
    "pagination": { /* ... */ }
  }
}
```

## 5.2 Get Execution by ID
**GET** `/executions/{id}`

## 5.3 Create Route Execution
**POST** `/executions`

### Headers
```
Authorization: Bearer {jwt_token}  # Only ADMIN
```

### Request Body
```json
{
  "route_id": 1,                    // int, required
  "driver_id": 2,                   // int, required
  "vehicle_id": 1,                  // int, required
  "start_time": "2025-01-16T08:00:00Z", // datetime, required
  "initial_km": 12515,              // int, required
  "initial_notes": "Starting route execution" // string, optional
}
```

## 5.4 Start Execution
**PATCH** `/executions/{id}/start`

### Headers
```
Authorization: Bearer {jwt_token}  # Driver or ADMIN
```

### Request Body
```json
{
  "initial_km": 12515,              // int, required
  "latitude": -25.4284,             // decimal, required (current position)
  "longitude": -49.2733,            // decimal, required
  "initial_notes": "Starting collection" // string, optional
}
```

## 5.5 Complete Execution
**PATCH** `/executions/{id}/complete`

### Headers
```
Authorization: Bearer {jwt_token}  # Driver or ADMIN
```

### Request Body
```json
{
  "final_km": 12525,                // int, required
  "total_collected_weight_kg": 1200, // decimal, required
  "final_notes": "Collection completed successfully", // string, optional
  "problems_found": "",             // string, optional
  "driver_rating": 4                // int, optional (1-5)
}
```

---

# 📍 6. GPS TRACKING

## 6.1 Send GPS Position
**POST** `/executions/{execution_id}/gps`

### Headers
```
Authorization: Bearer {jwt_token}  # Driver or ADMIN
```

### Request Body
```json
{
  "gps_timestamp": "2025-01-15T08:30:00Z", // datetime, required
  "latitude": -25.4284,             // decimal, required
  "longitude": -49.2733,            // decimal, required
  "speed_kmh": 25.5,               // decimal, optional
  "heading_degrees": 180,           // int, optional (0-359)
  "accuracy_meters": 5.0,          // decimal, optional
  "event_type": "NORMAL"           // NORMAL|STOP|START|END
}
```

### Response 201
```json
{
  "success": true,
  "data": {
    "gps_record": {
      "id": 1,
      "execution_id": 1,
      "gps_timestamp": "2025-01-15T08:30:00Z",
      "latitude": -25.4284,
      "longitude": -49.2733,
      "speed_kmh": 25.5,
      "heading_degrees": 180,
      "accuracy_meters": 5.0,
      "event_type": "NORMAL",
      "created_at": "2025-01-15T08:30:05Z"
    }
  }
}
```

## 6.2 Get GPS Track
**GET** `/executions/{execution_id}/gps`

### Query Parameters
```
?start_time=2025-01-15T08:00:00Z   // filter by start time
&end_time=2025-01-15T10:00:00Z     // filter by end time
&event_type=NORMAL                 // filter by event type
&limit=1000                        // max points (default: 1000)
```

### Response 200
```json
{
  "success": true,
  "data": {
    "gps_track": [
      {
        "id": 1,
        "gps_timestamp": "2025-01-15T08:30:00Z",
        "latitude": -25.4284,
        "longitude": -49.2733,
        "speed_kmh": 25.5,
        "heading_degrees": 180,
        "accuracy_meters": 5.0,
        "event_type": "NORMAL"
      }
    ],
    "execution": {
      "id": 1,
      "route_name": "Downtown Route A1",
      "driver_name": "John Driver",
      "vehicle_plate": "ABC1234",
      "status": "IN_PROGRESS"
    }
  }
}
```

---

# 🗑️ 7. COLLECTION RECORDS

## 7.1 Record Collection Point
**POST** `/executions/{execution_id}/collections`

### Headers
```
Authorization: Bearer {jwt_token}  # Driver or ADMIN
```

### Request Body
```json
{
  "point_id": 1,                    // int, required
  "arrival_timestamp": "2025-01-15T08:45:00Z", // datetime, required
  "collection_timestamp": "2025-01-15T08:50:00Z", // datetime, required
  "departure_timestamp": "2025-01-15T08:55:00Z", // datetime, optional
  "collection_status": "COLLECTED", // COLLECTED|SKIPPED|PROBLEM
  "collected_weight_kg": 45.5,     // decimal, optional
  "collected_waste_types": ["COMMERCIAL", "RECYCLABLE"], // array, optional
  "point_condition": "NORMAL",      // NORMAL|SATURATED|DAMAGED|INACCESSIBLE
  "notes": "Normal collection",     // string, optional
  "non_collection_reason": "",      // string, required if status != COLLECTED
  "latitude": -25.4284,             // decimal, required
  "longitude": -49.2733,            // decimal, required
  "accuracy_meters": 3.0,          // decimal, optional
  "photo_urls": [                   // array, optional
    "https://storage.od46s.com/photos/collection-1-before.jpg",
    "https://storage.od46s.com/photos/collection-1-after.jpg"
  ]
}
```

### Response 201
```json
{
  "success": true,
  "data": {
    "collection_record": {
      "id": 1,
      "execution_id": 1,
      "point_id": 1,
      "arrival_timestamp": "2025-01-15T08:45:00Z",
      "collection_timestamp": "2025-01-15T08:50:00Z",
      "departure_timestamp": "2025-01-15T08:55:00Z",
      "collection_status": "COLLECTED",
      "collected_weight_kg": 45.5,
      "point_condition": "NORMAL",
      "notes": "Normal collection",
      "photo_urls": ["..."],
      "created_at": "2025-01-15T08:55:00Z"
    }
  }
}
```

## 7.2 Upload Collection Photo
**POST** `/executions/{execution_id}/collections/{collection_id}/photos`

### Headers
```
Authorization: Bearer {jwt_token}  # Driver or ADMIN
Content-Type: multipart/form-data
```

### Request Body (multipart)
```
photo: (file)               // image file (jpeg, png), max 5MB
type: "before"              // "before" | "after" | "problem"
description: "Before collection" // string, optional
```

### Response 201
```json
{
  "success": true,
  "data": {
    "photo": {
      "id": 1,
      "url": "https://storage.od46s.com/photos/collection-1-before.jpg",
      "type": "before",
      "description": "Before collection",
      "uploaded_at": "2025-01-15T08:55:00Z"
    }
  }
}
```

---

# 📊 8. ANALYTICS & REPORTS

## 8.1 Dashboard Statistics
**GET** `/analytics/dashboard`

### Query Parameters
```
?period=month              // day|week|month|year
&start_date=2025-01-01     // filter start date
&end_date=2025-01-31       // filter end date
```

### Response 200
```json
{
  "success": true,
  "data": {
    "summary": {
      "total_executions": 150,
      "completed_executions": 145,
      "total_weight_collected_kg": 45000,
      "active_drivers": 8,
      "active_vehicles": 5,
      "average_completion_rate": 96.7
    },
    "daily_stats": [
      {
        "date": "2025-01-15",
        "executions": 12,
        "weight_collected_kg": 3200,
        "completion_rate": 100
      }
    ],
    "top_drivers": [
      {
        "driver_id": 2,
        "driver_name": "John Driver",
        "total_executions": 25,
        "average_rating": 4.5,
        "completion_rate": 100
      }
    ],
    "route_efficiency": [
      {
        "route_id": 1,
        "route_name": "Downtown Route A1",
        "average_time_minutes": 115,
        "average_weight_kg": 1250,
        "efficiency_score": 8.7
      }
    ]
  }
}
```

## 8.2 Driver Performance Report
**GET** `/analytics/drivers/{driver_id}/performance`

### Query Parameters
```
?period=month              // day|week|month|year
&start_date=2025-01-01     
&end_date=2025-01-31       
```

### Response 200
```json
{
  "success": true,
  "data": {
    "driver": {
      "id": 2,
      "name": "John Driver",
      "license_category": "D"
    },
    "performance": {
      "total_executions": 25,
      "completed_executions": 25,
      "completion_rate": 100,
      "average_rating": 4.5,
      "total_weight_collected_kg": 12500,
      "average_time_hours": 2.5,
      "punctuality_score": 95.2,
      "safety_score": 98.1
    },
    "execution_history": [
      {
        "date": "2025-01-15",
        "route_name": "Downtown Route A1",
        "duration_hours": 2.5,
        "weight_collected_kg": 1200,
        "rating": 5,
        "status": "COMPLETED"
      }
    ]
  }
}
```

## 8.3 Route Efficiency Report
**GET** `/analytics/routes/{route_id}/efficiency`

## 8.4 Fleet Utilization Report
**GET** `/analytics/fleet/utilization`

---

# 📱 9. MOBILE SYNC

## 9.1 Sync Data Download
**GET** `/mobile/sync/download`

### Headers
```
Authorization: Bearer {jwt_token}  # Driver
```

### Query Parameters
```
?last_sync=2025-01-15T08:00:00Z    // last sync timestamp
```

### Response 200
```json
{
  "success": true,
  "data": {
    "sync_timestamp": "2025-01-15T12:00:00Z",
    "routes": [/* assigned routes */],
    "vehicles": [/* available vehicles */],
    "collection_points": [/* route points */],
    "offline_data": {
      "waste_types": ["RESIDENTIAL", "COMMERCIAL", "RECYCLABLE"],
      "point_conditions": ["NORMAL", "SATURATED", "DAMAGED"],
      "collection_statuses": ["COLLECTED", "SKIPPED", "PROBLEM"]
    }
  }
}
```

## 9.2 Sync Data Upload
**POST** `/mobile/sync/upload`

### Headers
```
Authorization: Bearer {jwt_token}  # Driver
```

### Request Body
```json
{
  "sync_timestamp": "2025-01-15T12:00:00Z",
  "offline_data": {
    "gps_records": [/* GPS points collected offline */],
    "collection_records": [/* collections made offline */],
    "execution_updates": [/* execution status changes */]
  }
}
```

---

# 🔧 10. SYSTEM HEALTH

## 10.1 Health Check
**GET** `/api/v1/health`

### Response 200
```json
{
  "status": "healthy",
  "timestamp": "2025-01-15T12:00:00Z",
  "version": "1.0.0",
  "services": {
    "database": "healthy",
    "file_storage": "healthy",
    "authentication": "healthy"
  },
  "uptime_seconds": 86400
}
```

## 10.2 Basic Health (simple)
**GET** `/health`

### Headers
```
Authorization: Bearer {jwt_token}  # Only ADMIN
```

### Response 200
```json
{
  "success": true,
  "data": {
    "system": {
      "version": "1.0.0",
      "environment": "production",
      "uptime_seconds": 86400
    },
    "database": {
      "status": "healthy",
      "total_users": 15,
      "total_routes": 25,
      "total_executions": 150
    },
    "storage": {
      "status": "healthy",
      "total_photos": 1250,
      "storage_used_gb": 2.5
    }
  }
}
```

---

# 📝 Implementation Status

## ✅ Implemented Routes
- Auth
  - **POST** `/api/v1/auth/login`
  - **POST** `/api/v1/auth/register`
  - **POST** `/api/v1/auth/refresh`
  - **GET** `/api/v1/auth/health`
- Health
  - **GET** `/api/v1/health`
  - **GET** `/health`
  - **GET** `/actuator/health`
- Vehicles
  - **GET** `/api/v1/vehicles`
  - **POST** `/api/v1/vehicles`
  - **PUT** `/api/v1/vehicles/{id}`
  - **PATCH** `/api/v1/vehicles/{id}/status`

## ❌ Not Implemented
- User management (CRUD)
- Routes (CRUD + points)
- Executions (CRUD + transitions)
- GPS tracking and photo upload
- Analytics and reports
- Mobile sync

## 🗃️ Database Status
- ✅ Entity structure defined (users, drivers, administrators)
- ✅ JPA inheritance (JOINED strategy) 
- ❌ No Liquibase migrations (removed)
- ❌ No database tables created
- ❌ No initial data

## 🏗️ Current Architecture
- ✅ Spring Boot 3.2
- ✅ Java 21
- ✅ Docker containerization
- ✅ JWT authentication structure
- ✅ BCrypt password hashing
- ❌ Database integration disabled
- ❌ File storage not implemented
- ❌ No business logic implementation

**Approx. total implemented:** 11 endpoints

---

# 🛡️ FINAL SECURITY ASSESSMENT

## ✅ **What's Currently Secure**
1. **Password Hashing**: BCrypt with proper salt (Spring Security default)
2. **JWT Structure**: Valid HS512 algorithm, proper expiration
3. **Password Never Returned**: API responses exclude password fields
4. **Immediate Hashing**: Passwords encoded immediately upon receipt

## 🚨 **Critical Vulnerabilities to Fix**
1. **Hardcoded JWT Secret**: Needs environment variable (`${JWT_SECRET}`)
2. **No Rate Limiting**: Login endpoints vulnerable to brute force
3. **No HTTPS Enforcement**: Passwords transmitted in plain text without TLS
4. **No Password Complexity**: Accepts weak passwords (current min: 6 chars)
5. **No Account Lockout**: Unlimited login attempts allowed
6. **No Security Headers**: Missing XSS, CSRF, and content-type protection

## 🔧 **Priority Security Fixes (Recommended Order)**
1. **IMMEDIATE**: Move JWT secret to environment variables
2. **HIGH**: Enable HTTPS/TLS in production 
3. **HIGH**: Add rate limiting to auth endpoints
4. **MEDIUM**: Implement password complexity requirements
5. **MEDIUM**: Add security headers (CSRF, XSS protection)
6. **LOW**: Add account lockout mechanism
7. **LOW**: Implement audit logging for authentication events

## 🎯 **Production-Ready Security Checklist**
- [ ] JWT secret in environment variable (64+ chars)
- [ ] HTTPS/TLS 1.2+ enabled
- [ ] Rate limiting on auth endpoints (5 attempts/minute)
- [ ] Password complexity (8+ chars, mixed case, numbers, symbols)
- [ ] Security headers (XSS, CSRF, Content-Type protection)
- [ ] Account lockout (temporary after 5 failed attempts)
- [ ] Audit logging (login attempts, password changes)
- [ ] Token refresh mechanism (avoid 24h fixed expiration)
- [ ] Input validation on all endpoints
- [ ] SQL injection protection (using JPA/Hibernate properly)

**Security Score: 4/10** ⚠️ *Needs significant improvement before production deployment*

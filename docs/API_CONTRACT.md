# 📋 API Contract - OD46S System

## 🔗 Base URL
```
Development: http://localhost:8080/api/v1
Production: https://api.od46s.com/v1
```

## 🛡️ Security & Authentication

### 🔐 Password Security
- **Storage**: Passwords are encrypted using **BCrypt** (cost factor 10+)
- **Transmission**: Passwords sent in plain text **only** during login via HTTPS
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

## 1.2 User Management (Admin Only)
**POST** `/api/v1/users`

> **🔒 ADMIN ONLY**: This endpoint requires administrator authentication.

### Request Body
```json
{
  "name": "New User",               // string, required
  "email": "user@od46s.com",        // string, required, unique
  "cpf": "12345678901",            // string, required, unique
  "password": "SecureP@ssw0rd!",     // string, required (min 6 chars)
  "type": "DRIVER",                // ADMIN | DRIVER
  "licenseNumber": "12345678901",   // string, required for DRIVER
  "licenseCategory": "B",          // string, required for DRIVER
  "licenseExpiry": "2030-12-31",   // string, required for DRIVER
  "phone": "47999999999"           // string, optional for DRIVER
}
```

**🔒 Security Implementation:**
- **Admin authentication required** - only administrators can create users
- Password **immediately hashed with BCrypt** (cost factor 10+)
- Plain text password **never persisted**, discarded after hashing
- **BCrypt salt** automatically generated per password
- **HTTPS required** to protect password transmission

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

**🔒 Security Notes:**
- **No public registration** - only administrators can create users
- **Email/CPF uniqueness** enforced at database level
- **Admin authentication required** for all user creation
- **Centralized user management** by administrators

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

# 🔗 5. ROUTE ASSIGNMENTS (Escalas/Atribuições)

> **💡 Conceito**: Uma ATRIBUIÇÃO é o vínculo DURADOURO entre uma rota, um motorista e um caminhão.  
> É o **cadastro que interliga** rota + motorista + caminhão.  
> Duração: dias, semanas, meses ou anos (escala permanente).

## 5.1 List Assignments
**GET** `/api/v1/assignments`

### Query Parameters
```
?page=1                    // pagination
&limit=20                  // items per page
&route_id=1               // filter by route
&driver_id=2              // filter by driver
&vehicle_id=3             // filter by vehicle
&status=ACTIVE            // ACTIVE|INACTIVE
&start_date=2025-01-01    // filter by start date
&sort=created_at          // sorting
&order=desc               // direction
```

### Response 200
```json
{
  "success": true,
  "data": {
    "assignments": [
      {
        "id": 1,
        "route": {
          "id": 1,
          "name": "Downtown Route A1",
          "periodicity": "0 8 * * 1,3,5"
        },
        "driver": {
          "id": 2,
          "name": "John Driver",
          "license_number": "12345678901"
        },
        "vehicle": {
          "id": 1,
          "license_plate": "ABC1234",
          "model": "Mercedes-Benz Atego 1719"
        },
        "status": "ACTIVE",
        "start_date": "2025-01-01",
        "end_date": null,
        "notes": "Permanent assignment for downtown area",
        "created_by": 1,
        "created_at": "2025-01-01T10:00:00Z",
        "executions_count": 45
      }
    ],
    "pagination": {
      "current_page": 1,
      "total_pages": 5,
      "total_items": 89,
      "items_per_page": 20
    }
  }
}
```

## 5.2 Get Assignment by ID
**GET** `/api/v1/assignments/{id}`

### Response 200
```json
{
  "success": true,
  "data": {
    "assignment": {
      "id": 1,
      "route": {
        "id": 1,
        "name": "Downtown Route A1",
        "description": "Commercial area collection",
        "collection_type": "COMMERCIAL",
        "periodicity": "0 8 * * 1,3,5",
        "estimated_time_minutes": 120,
        "distance_km": 15.5,
        "collection_points_count": 25
      },
      "driver": {
        "id": 2,
        "name": "John Driver",
        "email": "john@od46s.com",
        "cpf": "12345678901",
        "license_number": "12345678901",
        "license_category": "D",
        "license_expiry": "2028-12-31",
        "phone": "47999999999"
      },
      "vehicle": {
        "id": 1,
        "license_plate": "ABC1234",
        "model": "Mercedes-Benz Atego 1719",
        "year": 2022,
        "capacity_kg": 8000,
        "status": "AVAILABLE"
      },
      "status": "ACTIVE",
      "start_date": "2025-01-01",
      "end_date": null,
      "notes": "Permanent assignment for downtown area",
      "created_by": 1,
      "created_at": "2025-01-01T10:00:00Z",
      "updated_at": "2025-01-01T10:00:00Z",
      "recent_executions": [
        {
          "id": 15,
          "execution_date": "2025-01-15",
          "start_time": "2025-01-15T08:15:00Z",
          "end_time": "2025-01-15T10:30:00Z",
          "status": "COMPLETED"
        }
      ],
      "executions_count": 45
    }
  }
}
```

## 5.3 Create Assignment
**POST** `/api/v1/assignments`

> **🔒 ADMIN ONLY**: Este endpoint requer autenticação de administrador.

### Headers
```
Authorization: Bearer {jwt_token}
```

### Request Body
```json
{
  "route_id": 1,                    // int, required
  "driver_id": 2,                   // int, required
  "vehicle_id": 1,                  // int, required
  "start_date": "2025-01-01",      // date (YYYY-MM-DD), required
  "end_date": null,                 // date (YYYY-MM-DD), optional (null = indefinido)
  "notes": "Permanent assignment"   // string, optional
}
```

### Response 201
```json
{
  "success": true,
  "data": {
    "assignment": {
      "id": 1,
      "route_id": 1,
      "driver_id": 2,
      "vehicle_id": 1,
      "status": "ACTIVE",
      "start_date": "2025-01-01",
      "end_date": null,
      "notes": "Permanent assignment",
      "created_by": 1,
      "created_at": "2025-01-01T10:00:00Z"
    }
  },
  "message": "Assignment created successfully"
}
```

### Response 400 (Validation Errors)
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {
      "driver_id": "Driver is not active or enabled",
      "vehicle_id": "Vehicle is not available",
      "route_id": "Route is not active"
    }
  }
}
```

### Response 409 (Conflict)
```json
{
  "success": false,
  "error": {
    "code": "ASSIGNMENT_CONFLICT",
    "message": "Driver already has an active assignment for this period",
    "details": {
      "existing_assignment_id": 5,
      "conflicting_route": "Downtown Route A1"
    }
  }
}
```

## 5.4 Update Assignment
**PUT** `/api/v1/assignments/{id}`

> **🔒 ADMIN ONLY**

### Headers
```
Authorization: Bearer {jwt_token}
```

### Request Body
```json
{
  "route_id": 1,                    // int, optional
  "driver_id": 2,                   // int, optional
  "vehicle_id": 1,                  // int, optional
  "start_date": "2025-01-01",      // date, optional
  "end_date": "2025-12-31",        // date, optional
  "notes": "Updated notes"          // string, optional
}
```

## 5.5 Deactivate Assignment
**PATCH** `/api/v1/assignments/{id}/deactivate`

> **🔒 ADMIN ONLY**

### Headers
```
Authorization: Bearer {jwt_token}
```

### Request Body
```json
{
  "reason": "Driver transferred to another route", // string, optional
  "end_date": "2025-01-31"                        // date, optional
}
```

### Response 200
```json
{
  "success": true,
  "data": {
    "assignment": {
      "id": 1,
      "status": "INACTIVE",
      "end_date": "2025-01-31"
    }
  },
  "message": "Assignment deactivated successfully"
}
```

## 5.6 Get Driver's Current Assignment
**GET** `/api/v1/assignments/my-current`

> **🔒 DRIVER**: O motorista autenticado vê sua própria atribuição ativa.

### Headers
```
Authorization: Bearer {jwt_token}  # Driver
```

### Response 200
```json
{
  "success": true,
  "data": {
    "assignment": {
      "id": 1,
      "route": {
        "id": 1,
        "name": "Downtown Route A1",
        "periodicity": "0 8 * * 1,3,5",
        "description": "Commercial area - Mon, Wed, Fri at 8am"
      },
      "vehicle": {
        "id": 1,
        "license_plate": "ABC1234",
        "model": "Mercedes-Benz Atego 1719"
      },
      "start_date": "2025-01-01",
      "next_scheduled_dates": [
        "2025-01-20",
        "2025-01-22",
        "2025-01-24"
      ]
    }
  }
}
```

---

# 📋 6. ROUTE EXECUTIONS (Execuções Individuais)

> **💡 Conceito**: Uma EXECUÇÃO é o registro de UMA COLETA ESPECÍFICA realizada.  
> Criada automaticamente quando o motorista INICIA uma coleta no app mobile.  
> Duração: algumas horas (do início ao fim da coleta).  
> Vinculada a uma ASSIGNMENT (escala).

## 6.1 List Executions
**GET** `/api/v1/executions`

### Query Parameters
```
?page=1                    // pagination
&limit=20                  // items per page
&assignment_id=1          // filter by assignment
&route_id=1               // filter by route
&driver_id=2              // filter by driver
&vehicle_id=3             // filter by vehicle
&status=COMPLETED         // IN_PROGRESS|COMPLETED|CANCELLED
&execution_date=2025-01-15 // filter by date (YYYY-MM-DD)
&start_date=2025-01-01    // filter by date range
&end_date=2025-01-31      // filter by date range
&sort=execution_date      // sorting
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
        "assignment": {
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
          }
        },
        "execution_date": "2025-01-15",
        "start_time": "2025-01-15T08:15:00Z",
        "end_time": "2025-01-15T10:30:00Z",
        "status": "COMPLETED",
        "initial_km": 12500,
        "final_km": 12515,
        "distance_km": 15,
        "duration_minutes": 135,
        "total_collected_weight_kg": 1200,
        "points_visited": 25,
        "points_collected": 23,
        "collection_rate": 0.92,
        "driver_rating": 4,
        "problems_found": "2 damaged bins at collection points",
        "created_at": "2025-01-15T08:15:00Z"
      }
    ],
    "pagination": { /* ... */ },
    "summary": {
      "total_executions": 150,
      "completed": 145,
      "in_progress": 2,
      "cancelled": 3,
      "total_distance_km": 2250,
      "total_weight_collected_kg": 180000
    }
  }
}
```

## 6.2 Get Execution by ID
**GET** `/api/v1/executions/{id}`

### Response 200
```json
{
  "success": true,
  "data": {
    "execution": {
      "id": 1,
      "assignment": {
        "id": 1,
        "route": {
          "id": 1,
          "name": "Downtown Route A1",
          "collection_points_count": 25
        },
        "driver": {
          "id": 2,
          "name": "John Driver"
        },
        "vehicle": {
          "id": 1,
          "license_plate": "ABC1234"
        }
      },
      "execution_date": "2025-01-15",
      "start_time": "2025-01-15T08:15:00Z",
      "end_time": "2025-01-15T10:30:00Z",
      "status": "COMPLETED",
      "initial_km": 12500,
      "final_km": 12515,
      "distance_km": 15,
      "duration_minutes": 135,
      "total_collected_weight_kg": 1200,
      "points_visited": 25,
      "points_collected": 23,
      "collection_rate": 0.92,
      "initial_notes": "Starting collection, good weather",
      "final_notes": "Collection completed successfully",
      "problems_found": "2 damaged bins at points 5 and 12",
      "driver_rating": 4,
      "gps_records_count": 450,
      "collection_records_count": 23,
      "created_at": "2025-01-15T08:15:00Z"
    }
  }
}
```

## 6.3 Start Execution
**POST** `/api/v1/executions/start`

> **🔒 DRIVER or ADMIN**: Motorista inicia sua coleta no app mobile.

### Headers
```
Authorization: Bearer {jwt_token}  # Driver
```

### Request Body
```json
{
  "assignment_id": 1,               // int, required
  "initial_km": 12515,              // int, required
  "latitude": -25.4284,             // decimal, required (posição atual)
  "longitude": -49.2733,            // decimal, required
  "initial_notes": "Starting collection" // string, optional
}
```

### Response 201
```json
{
  "success": true,
  "data": {
    "execution": {
      "id": 1,
      "assignment_id": 1,
      "execution_date": "2025-01-16",
      "start_time": "2025-01-16T08:15:23Z",
      "status": "IN_PROGRESS",
      "initial_km": 12515,
      "route": {
        "id": 1,
        "name": "Downtown Route A1",
        "collection_points": [
          {
            "id": 1,
            "sequence_order": 1,
            "address": "123 Main Street",
            "latitude": -25.4284,
            "longitude": -49.2733,
            "waste_type": "COMMERCIAL"
          }
        ]
      }
    }
  },
  "message": "Execution started successfully"
}
```

### Response 400 (Validation Errors)
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Cannot start execution",
    "details": {
      "reason": "Driver already has an execution IN_PROGRESS",
      "current_execution_id": 5
    }
  }
}
```

## 6.4 Complete Execution
**PATCH** `/api/v1/executions/{id}/complete`

> **🔒 DRIVER or ADMIN**: Motorista finaliza a coleta.

### Headers
```
Authorization: Bearer {jwt_token}  # Driver
```

### Request Body
```json
{
  "final_km": 12525,                // int, required
  "latitude": -25.4284,             // decimal, required (posição final)
  "longitude": -49.2733,            // decimal, required
  "total_collected_weight_kg": 1200, // decimal, optional
  "final_notes": "Collection completed successfully", // string, optional
  "problems_found": "2 damaged bins",  // string, optional
  "driver_rating": 4                // int, optional (1-5)
}
```

### Response 200
```json
{
  "success": true,
  "data": {
    "execution": {
      "id": 1,
      "status": "COMPLETED",
      "execution_date": "2025-01-16",
      "start_time": "2025-01-16T08:15:23Z",
      "end_time": "2025-01-16T10:30:45Z",
      "duration_minutes": 135,
      "initial_km": 12515,
      "final_km": 12525,
      "distance_km": 10,
      "total_collected_weight_kg": 1200,
      "points_visited": 25,
      "points_collected": 23,
      "collection_rate": 0.92
    }
  },
  "message": "Execution completed successfully"
}
```

## 6.5 Cancel Execution
**PATCH** `/api/v1/executions/{id}/cancel`

> **🔒 DRIVER or ADMIN**: Cancelar execução em andamento.

### Headers
```
Authorization: Bearer {jwt_token}
```

### Request Body
```json
{
  "cancellation_reason": "Heavy rain, unsafe conditions", // string, required
  "latitude": -25.4284,             // decimal, optional
  "longitude": -49.2733             // decimal, optional
}
```

### Response 200
```json
{
  "success": true,
  "data": {
    "execution": {
      "id": 1,
      "status": "CANCELLED",
      "cancellation_reason": "Heavy rain, unsafe conditions"
    }
  },
  "message": "Execution cancelled"
}
```

## 6.6 Get Driver's Current Execution
**GET** `/api/v1/executions/my-current`

> **🔒 DRIVER**: Motorista vê sua execução atual em andamento.

### Headers
```
Authorization: Bearer {jwt_token}  # Driver
```

### Response 200
```json
{
  "success": true,
  "data": {
    "execution": {
      "id": 1,
      "assignment_id": 1,
      "execution_date": "2025-01-16",
      "start_time": "2025-01-16T08:15:23Z",
      "status": "IN_PROGRESS",
      "initial_km": 12515,
      "route": {
        "id": 1,
        "name": "Downtown Route A1",
        "estimated_time_minutes": 120,
        "distance_km": 15.5
      },
      "elapsed_minutes": 45,
      "points_visited": 10,
      "points_remaining": 15
    }
  }
}
```

### Response 404 (No Active Execution)
```json
{
  "success": false,
  "error": {
    "code": "NO_ACTIVE_EXECUTION",
    "message": "No execution in progress"
  }
}
```

---

# 📍 7. GPS TRACKING & EVENTOS (Sistema Unificado)

> **💡 Conceito**: Um único endpoint para rastreamento GPS + eventos + coletas em pontos com fotos

## 7.1 Registrar GPS / Evento / Coleta com Foto
**POST** `/executions/{execution_id}/gps`

### Headers
```
Authorization: Bearer {jwt_token}  # Driver only
Content-Type: multipart/form-data
```

### Request Body (multipart/form-data)

**Campos Obrigatórios:**
```
latitude: -25.4284              // decimal, required
longitude: -49.2733             // decimal, required
```

**Campos Opcionais - Rastreamento:**
```
speed_kmh: 25.5                // decimal, optional
heading_degrees: 180            // int, optional (0-359)
accuracy_meters: 5.0           // decimal, optional
event_type: NORMAL             // enum, optional (default: NORMAL)
is_automatic: true             // boolean, optional (true = GPS auto, false = manual)
```

**Campos Opcionais - Sincronização Offline:**
```
is_offline: true               // boolean, optional (true = sincronização, false = tempo real)
gps_timestamp: 2025-12-01T14:23:45  // ISO-8601, optional (hora da coleta real)
```

**Campos Opcionais - Eventos/Problemas:**
```
description: "Descrição"       // string, optional
photo: (file)                  // image file, optional (max 10MB, JPG/PNG/WebP)
```

**Campos Opcionais - Coleta em Pontos:**
```
point_id: 15                   // int, optional - ID do ponto de coleta
collected_weight_kg: 45.5      // decimal, optional - Peso coletado em kg
point_condition: NORMAL        // enum, optional - NORMAL, SATURATED, DAMAGED, INACCESSIBLE
```

### Event Types

**Percurso:**
- `START` - Início da coleta
- `NORMAL` - Percurso normal (registro GPS periódico)
- `STOP` - Parada qualquer
- `BREAK` - Intervalo/Descanso
- `FUEL` - Abastecimento
- `LUNCH` - Almoço

**Coleta em Pontos:**
- `POINT_ARRIVAL` - Chegada no ponto de coleta
- `POINT_COLLECTED` - Ponto coletado com sucesso ✅
- `POINT_SKIPPED` - Ponto não coletado (pulado) ❌
- `POINT_PROBLEM` - Problema no ponto de coleta ⚠️

**Gerais:**
- `PROBLEM` - Problema geral
- `OBSERVATION` - Observação
- `PHOTO` - Registro fotográfico específico
- `END` - Fim da coleta

### Use Cases

**Caso 1: GPS Normal (rastreamento periódico automático)**
```
POST /api/v1/executions/123/gps
latitude=-25.4284
longitude=-49.2733
speed_kmh=35.5
event_type=NORMAL
is_automatic=true
```
> 💡 O celular envia isso automaticamente a cada 30s

**Caso 2: Parada para Almoço (evento manual)**
```
POST /api/v1/executions/123/gps
latitude=-25.4284
longitude=-49.2733
event_type=LUNCH
is_automatic=false
description=Parada para almoço
```
> 💡 Motorista clicou no botão "Almoço"

**Caso 3: Problema com Foto (evento manual)**
```
POST /api/v1/executions/123/gps
latitude=-25.4284
longitude=-49.2733
event_type=PROBLEM
is_automatic=false
description=Lixeira transbordando, lixo espalhado na calçada
photo=@foto_problema.jpg
```
> 💡 Motorista registrou problema e anexou foto

**Caso 4: Coleta em Ponto (evento manual)**
```
POST /api/v1/executions/123/gps
latitude=-25.4284
longitude=-49.2733
event_type=POINT_COLLECTED
is_automatic=false
point_id=15
collected_weight_kg=45.5
point_condition=NORMAL
description=Coleta realizada, lixeira em bom estado
photo=@foto_lixeira.jpg
```
> 💡 Motorista confirmou coleta no ponto

**Caso 5: Ponto Não Coletado (Pulado)**
```
POST /api/v1/executions/123/gps
latitude=-25.4290
longitude=-49.2740
event_type=POINT_SKIPPED
point_id=16
point_condition=INACCESSIBLE
description=Portão trancado, sem acesso ao local
photo=@foto_portao.jpg
```

**Caso 6: Sincronização Offline (individual)**
```
POST /api/v1/executions/123/gps
latitude=-25.4284
longitude=-49.2733
event_type=POINT_COLLECTED
is_automatic=false
is_offline=true
gps_timestamp=2025-12-01T14:23:45
point_id=15
collected_weight_kg=45.5
```
> 💡 Registrado offline, enviando depois

###  Response 201
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
      "event_type": "PROBLEM",
      "is_automatic": false,
      "is_offline": false,
      "sync_delay_seconds": 5,
      "description": "Lixeira transbordando, lixo espalhado na calçada",
      "photo_url": "/api/v1/files/gps-photos/123/456",
      "point_id": null,
      "collected_weight_kg": null,
      "point_condition": null,
      "created_at": "2025-01-15T08:30:05Z"
    }
  }
}
```

### Notas sobre o campo `id`
- O campo `id` retornado é o identificador único do registro GPS
- Este `id` pode ser usado diretamente para buscar a foto: `/api/v1/files/gps-photos/{execution_id}/{id}`
- O campo `photo_url` já contém a URL completa pronta para uso (se a foto foi enviada)

### Response 400 (Validation Error)
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Latitude and longitude are required"
  }
}
```

## 7.2 Registrar GPS em Lote (Batch - Sincronização Offline)
**POST** `/executions/{execution_id}/gps/batch`

### Headers
```
Authorization: Bearer {jwt_token}  # Driver only
Content-Type: application/json
```

### Request Body (JSON Array)
```json
[
  {
    "latitude": -25.4284,
    "longitude": -49.2733,
    "event_type": "NORMAL",
    "is_automatic": true,
    "is_offline": true,
    "gps_timestamp": "2025-12-01T14:20:00"
  },
  {
    "latitude": -25.4290,
    "longitude": -49.2740,
    "event_type": "POINT_COLLECTED",
    "is_automatic": false,
    "is_offline": true,
    "gps_timestamp": "2025-12-01T14:45:12",
    "point_id": 15,
    "collected_weight_kg": 45.5,
    "point_condition": "NORMAL"
  }
]
```

### Response 201
```json
{
  "success": true,
  "data": {
    "total_records": 2,
    "success_count": 2,
    "error_count": 0,
    "errors": [],
    "saved_records": [
      {
        "id": 1,
        "execution_id": 123,
        "gps_timestamp": "2025-12-01T14:20:00",
        "latitude": -25.4284,
        "longitude": -49.2733,
        "event_type": "NORMAL",
        "is_automatic": true,
        "is_offline": true,
        "photo_url": null,
        "point_id": null,
        "collected_weight_kg": null,
        "point_condition": null
      },
      {
        "id": 2,
        "execution_id": 123,
        "gps_timestamp": "2025-12-01T14:45:12",
        "latitude": -25.4290,
        "longitude": -49.2740,
        "event_type": "POINT_COLLECTED",
        "is_automatic": false,
        "is_offline": true,
        "photo_url": null,
        "point_id": 15,
        "collected_weight_kg": 45.5,
        "point_condition": "NORMAL"
      }
    ]
  }
}
```

### Notas sobre saved_records
- Cada registro retornado contém o campo `id` que é o ID único do registro GPS
- Este `id` pode ser usado para buscar a foto associada: `/api/v1/files/gps-photos/{execution_id}/{id}`
- O campo `photo_url` será `null` para registros criados via batch (fotos devem ser enviadas individualmente)

### Response 400 (Batch Size Limit)
```json
{
  "success": false,
  "error": {
    "code": "BATCH_TOO_LARGE",
    "message": "Maximum batch size is 500 records"
  }
}
```

## 7.3 Obter Rastro GPS Completo
**GET** `/executions/{execution_id}/gps`

### Headers
```
Authorization: Bearer {jwt_token}  # Driver or ADMIN
```

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
      "event_type": "NORMAL",
      "is_automatic": true,
      "is_offline": false,
      "sync_delay_seconds": 0,
      "description": null,
      "photo_url": null,
      "point_id": null,
      "collected_weight_kg": null,
      "point_condition": null
      },
      {
        "id": 2,
        "gps_timestamp": "2025-01-15T09:15:00Z",
        "latitude": -25.4290,
        "longitude": -49.2740,
        "speed_kmh": 0.0,
        "heading_degrees": null,
        "accuracy_meters": 3.0,
        "event_type": "POINT_COLLECTED",
        "is_automatic": false,
        "is_offline": false,
        "sync_delay_seconds": 3,
        "description": "Coleta realizada com sucesso",
        "photo_url": "/api/v1/files/gps-photos/123/2",
        "point_id": 1,
        "collected_weight_kg": 45.5,
        "point_condition": "NORMAL"
      },
      {
        "id": 3,
        "gps_timestamp": "2025-01-15T10:00:00Z",
        "latitude": -25.4302,
        "longitude": -49.2751,
        "speed_kmh": 0.0,
        "heading_degrees": null,
        "accuracy_meters": 3.0,
        "event_type": "POINT_SKIPPED",
        "is_automatic": false,
        "is_offline": false,
        "sync_delay_seconds": 4,
        "description": "Portão trancado, sem acesso",
        "photo_url": "/api/v1/files/gps-photos/123/3",
        "point_id": 2,
        "collected_weight_kg": null,
        "point_condition": "INACCESSIBLE"
      }
    ],
    "execution": {
      "id": 1,
      "route_name": "Downtown Route A1",
      "driver_name": "John Driver",
      "vehicle_plate": "ABC1234",
      "status": "IN_PROGRESS"
    },
    "statistics": {
      "total_points": 450,
      "events_count": {
        "NORMAL": 440,
        "STOP": 5,
        "PROBLEM": 2,
        "LUNCH": 1,
        "BREAK": 2
      },
      "photos_count": 3,
      "distance_calculated_km": 15.2
    }
  }
}
```

## 7.3 Baixar Foto de Evento
**GET** `/files/gps-photos/{execution_id}/{gps_record_id}`

### Headers
```
Authorization: Bearer {jwt_token}  # Driver or ADMIN
```

### Path Parameters
```
execution_id: integer    // ID da execução
gps_record_id: integer   // ID do registro GPS (obtido do campo id do registro GPS)
```

### Response 200
```
Content-Type: image/jpeg (or image/png, image/webp)
Content-Disposition: inline; filename="photo_{gps_record_id}.{ext}"

[Binary image data]
```

### Notas
- O `gps_record_id` é o mesmo valor do campo `id` retornado no registro GPS
- A extensão do arquivo é detectada automaticamente (jpg, jpeg, png, webp)
- A URL completa pode ser obtida do campo `photo_url` retornado nos endpoints de GPS

### Response 404
```json
{
  "success": false,
  "error": {
    "code": "FILE_NOT_FOUND",
    "message": "File not found"
  }
}
```

## 7.4 Armazenamento de Fotos

**Tecnologia**: MinIO (S3-compatible storage)

**Estrutura de Armazenamento**:
```
Bucket: od46s-files
Path: gps-photos/execution_{execution_id}/{gps_record_id}.{ext}

Exemplo:
gps-photos/
  ├── execution_123/
  │   ├── 1.jpg
  │   ├── 2.jpg
  │   └── 3.png
  └── execution_124/
      └── 10.jpg
```

**Organização**:
- Arquivos são organizados por `execution_id` e identificados pelo `gps_record_id`
- Cada registro GPS pode ter no máximo uma foto associada
- A extensão do arquivo é preservada do arquivo original enviado
- O nome do arquivo no MinIO é simplesmente `{gps_record_id}.{ext}`

**Limites e Validações**:
- Tamanho máximo: 10MB por foto
- Formatos aceitos: JPG, JPEG, PNG, WebP
- Armazenamento: Persistente no volume Docker `minio_data`
- Acesso: Apenas usuários autenticados (ADMIN ou DRIVER)

**URLs de Acesso**:
- API: `/api/v1/files/gps-photos/{execution_id}/{gps_record_id}`
- MinIO Console: http://localhost:9001 (minioadmin/minioadmin)
- MinIO API: http://localhost:9000

**Fluxo de Upload**:
1. Registro GPS é criado primeiro (obtém ID)
2. Foto é enviada e armazenada usando o ID do registro GPS
3. URL da foto é salva no campo `photo_url` do registro GPS

---

# 🗑️ 8. COLLECTION RECORDS

## 8.1 Record Collection Point
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

# 📊 9. ANALYTICS & REPORTS

## 9.1 Dashboard Statistics
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

# 📱 10. MOBILE SYNC

## 10.1 Sync Data Download
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

## 10.2 Sync Data Upload
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

# 🔧 11. SYSTEM HEALTH

## 11.1 Health Check
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

## 11.2 Basic Health (simple)
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
  - **POST** `/api/v1/auth/refresh`
  - **GET** `/api/v1/auth/health`
- Users (Admin Only)
  - **GET** `/api/v1/users`
  - **GET** `/api/v1/users/{id}`
  - **POST** `/api/v1/users`
  - **PUT** `/api/v1/users/{id}`
  - **DELETE** `/api/v1/users/{id}`
- Health
  - **GET** `/api/v1/health`
  - **GET** `/health`
  - **GET** `/actuator/health`
- Vehicles
  - **GET** `/api/v1/vehicles`
  - **POST** `/api/v1/vehicles`
  - **PUT** `/api/v1/vehicles/{id}`
  - **PATCH** `/api/v1/vehicles/{id}/status`

## ✅ Implemented
- **User Management (CRUD)**: ✅ COMPLETE
  - GET `/api/v1/users` (with pagination, search, filters, sorting)
  - GET `/api/v1/users/{id}`
  - POST `/api/v1/users`
  - PUT `/api/v1/users/{id}` (✅ FIXED)
  - DELETE `/api/v1/users/{id}`

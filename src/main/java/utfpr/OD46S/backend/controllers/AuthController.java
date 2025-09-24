package utfpr.OD46S.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utfpr.OD46S.backend.entitys.login.AuthResponse;
import utfpr.OD46S.backend.entitys.login.LoginRequest;
import utfpr.OD46S.backend.entitys.login.RegisterRequest;
import utfpr.OD46S.backend.entitys.login.RefreshRequest;
import utfpr.OD46S.backend.services.login.AuthService;

// import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Login endpoint
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"success\": false, \"error\": {\"code\": \"INVALID_CREDENTIALS\", \"message\": \"" + e.getMessage() + "\"}}");
        }
    }

    /**
     * Register endpoint  
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Email já cadastrado")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("{\"success\": false, \"error\": {\"code\": \"EMAIL_EXISTS\", \"message\": \"" + e.getMessage() + "\"}}");
            }
            if (e.getMessage().contains("CPF já cadastrado")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("{\"success\": false, \"error\": {\"code\": \"CPF_EXISTS\", \"message\": \"" + e.getMessage() + "\"}}");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"success\": false, \"error\": {\"code\": \"VALIDATION_ERROR\", \"message\": \"" + e.getMessage() + "\"}}");
        }
    }

    /**
     * Refresh token endpoint
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request.getToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"success\": false, \"error\": {\"code\": \"INVALID_TOKEN\", \"message\": \"" + e.getMessage() + "\"}}");
        }
    }

    /**
     * Health check endpoint for auth service
     * GET /api/v1/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"AUTH_SERVICE_UP\", \"timestamp\": \"" + 
                java.time.LocalDateTime.now() + "\"}");
    }
}

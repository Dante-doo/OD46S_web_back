package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Autenticação", description = "APIs de gerenciamento de autenticação para login, registro e renovação de token")
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
    @Operation(
        summary = "Login do usuário",
        description = "Autentica usuário com email/CPF e senha, retorna token JWT"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Login realizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = """
                    {
                        "token": "eyJhbGciOiJIUzUxMiJ9...",
                        "email": "admin@od46s.com",
                        "name": "System Administrator",
                        "type": "ADMIN"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Credenciais inválidas",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "error": {
                            "code": "INVALID_CREDENTIALS",
                            "message": "Invalid password"
                        }
                    }
                    """)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
        @Parameter(description = "Credenciais de login (email ou CPF e senha)", required = true)
        @RequestBody LoginRequest request) {
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
    @Operation(
        summary = "Registro de usuário",
        description = "Registra novo usuário (Administrador ou Motorista) e retorna token JWT"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Registro realizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = """
                    {
                        "token": "eyJhbGciOiJIUzUxMiJ9...",
                        "email": "newuser@od46s.com",
                        "name": "New User",
                        "type": "DRIVER"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Email ou CPF já existe",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "error": {
                            "code": "EMAIL_EXISTS",
                            "message": "Email already registered"
                        }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Erro de validação",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "error": {
                            "code": "VALIDATION_ERROR",
                            "message": "Driver license data is required for drivers"
                        }
                    }
                    """)
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
        @Parameter(description = "Dados de registro para novo usuário", required = true)
        @RequestBody RegisterRequest request) {
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
    @Operation(
        summary = "Renovar token JWT",
        description = "Renova token JWT com um token válido existente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Token renovado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = """
                    {
                        "token": "eyJhbGciOiJIUzUxMiJ9...",
                        "email": "user@od46s.com",
                        "name": "User Name",
                        "type": "USER"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Token inválido ou expirado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "error": {
                            "code": "INVALID_TOKEN",
                            "message": "Token invalid or expired"
                        }
                    }
                    """)
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
        @Parameter(description = "Token JWT atual para ser renovado", required = true)
        @RequestBody RefreshRequest request) {
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
    @Operation(
        summary = "Verificação de saúde do serviço de autenticação",
        description = "Verifica se o serviço de autenticação está funcionando"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Serviço está saudável",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "status": "AUTH_SERVICE_UP",
                    "timestamp": "2025-09-25T21:30:00"
                }
                """)
        )
    )
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"AUTH_SERVICE_UP\", \"timestamp\": \"" + 
                java.time.LocalDateTime.now() + "\"}");
    }
}

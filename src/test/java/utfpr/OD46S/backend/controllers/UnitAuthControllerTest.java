package utfpr.OD46S.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import utfpr.OD46S.backend.entitys.login.AuthResponse;
import utfpr.OD46S.backend.entitys.login.LoginRequest;
import utfpr.OD46S.backend.entitys.login.RegisterRequest;
import utfpr.OD46S.backend.entitys.login.RefreshRequest;
import utfpr.OD46S.backend.services.login.AuthService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UnitAuthControllerTest {

    private AuthController authController;
    private AuthService authService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        authController = new AuthController();
        // Usar reflection para injetar o mock
        try {
            var field = AuthController.class.getDeclaredField("authService");
            field.setAccessible(true);
            field.set(authController, authService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock", e);
        }
        objectMapper = new ObjectMapper();
    }

    @Test
    void testHealthCheck() {
        // When
        ResponseEntity<String> response = authController.health();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("AUTH_SERVICE_UP"));
        assertTrue(response.getBody().contains("timestamp"));
    }

    @Test
    void testLogin_Success() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@od46s.com");
        loginRequest.setPassword("password123");

        AuthResponse mockResponse = new AuthResponse(
            "eyJhbGciOiJIUzUxMiJ9.test.token",
            "test@od46s.com",
            "Test User",
            "ADMIN"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@od46s.com");
        loginRequest.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Senha inválida"));

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("INVALID_CREDENTIALS"));
        assertTrue(response.getBody().toString().contains("Senha inválida"));
    }

    @Test
    void testRegister_Success() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("New User");
        registerRequest.setEmail("newuser@od46s.com");
        registerRequest.setCpf("12345678901");
        registerRequest.setPassword("password123");
        registerRequest.setType("ADMIN");
        registerRequest.setAccessLevel("ADMIN");
        registerRequest.setDepartment("TI");

        AuthResponse mockResponse = new AuthResponse(
            "eyJhbGciOiJIUzUxMiJ9.test.token",
            "newuser@od46s.com",
            "New User",
            "ADMIN"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService, times(1)).register(registerRequest);
    }

    @Test
    void testRegister_EmailExists() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("New User");
        registerRequest.setEmail("existing@od46s.com");
        registerRequest.setCpf("12345678901");
        registerRequest.setPassword("password123");
        registerRequest.setType("ADMIN");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email já cadastrado"));

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("EMAIL_EXISTS"));
        assertTrue(response.getBody().toString().contains("Email já cadastrado"));
    }

    @Test
    void testRefresh_Success() {
        // Given
        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setToken("eyJhbGciOiJIUzUxMiJ9.old.token");

        AuthResponse mockResponse = new AuthResponse(
            "eyJhbGciOiJIUzUxMiJ9.new.token",
            "test@od46s.com",
            "Test User",
            "ADMIN"
        );

        when(authService.refreshToken(any(String.class))).thenReturn(mockResponse);

        // When
        ResponseEntity<?> response = authController.refresh(refreshRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService, times(1)).refreshToken(refreshRequest.getToken());
    }

    @Test
    void testRefresh_InvalidToken() {
        // Given
        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setToken("invalid.token");

        when(authService.refreshToken(any(String.class)))
                .thenThrow(new RuntimeException("Token inválido ou expirado"));

        // When
        ResponseEntity<?> response = authController.refresh(refreshRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("INVALID_TOKEN"));
        assertTrue(response.getBody().toString().contains("Token inválido ou expirado"));
    }
}

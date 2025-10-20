package utfpr.OD46S.backend.services.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import utfpr.OD46S.backend.entitys.Usuario;
import utfpr.OD46S.backend.entitys.login.AuthResponse;
import utfpr.OD46S.backend.entitys.login.LoginRequest;
import utfpr.OD46S.backend.repositorys.UsuarioRepository;
import utfpr.OD46S.backend.utils.JwtUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtil;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setName("Test User");
        usuario.setEmail("test@example.com");
        usuario.setCpf("12345678901");
        usuario.setPassword("encodedPassword");
        usuario.setActive(true);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testLogin_Success() {
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("mockToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertEquals("USER", response.getType());
    }

    @Test
    void testLogin_UserNotFound() {
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_InvalidPassword() {
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testRefreshToken_Success() {
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        when(jwtUtil.getEmailFromToken("validToken")).thenReturn("test@example.com");
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(jwtUtil.generateToken("test@example.com")).thenReturn("newToken");

        AuthResponse response = authService.refreshToken("validToken");

        assertNotNull(response);
        assertEquals("newToken", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertEquals("USER", response.getType());
    }

    @Test
    void testRefreshToken_InvalidToken() {
        when(jwtUtil.validateToken("invalidToken")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.refreshToken("invalidToken"));
    }

    @Test
    void testRefreshToken_UserNotFound() {
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        when(jwtUtil.getEmailFromToken("validToken")).thenReturn("test@example.com");
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.refreshToken("validToken"));
    }
}
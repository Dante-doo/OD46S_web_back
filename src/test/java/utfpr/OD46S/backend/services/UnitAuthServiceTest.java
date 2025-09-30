package utfpr.OD46S.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import utfpr.OD46S.backend.entitys.Administrator;
import utfpr.OD46S.backend.entitys.Motorista;
import utfpr.OD46S.backend.entitys.Usuario;
import utfpr.OD46S.backend.entitys.login.AuthResponse;
import utfpr.OD46S.backend.entitys.login.LoginRequest;
import utfpr.OD46S.backend.entitys.login.RegisterRequest;
import utfpr.OD46S.backend.enums.CategoriaCNH;
import utfpr.OD46S.backend.repositorys.AdministratorRepository;
import utfpr.OD46S.backend.repositorys.MotoristaRepository;
import utfpr.OD46S.backend.repositorys.UsuarioRepository;
import utfpr.OD46S.backend.utils.JwtUtils;
import utfpr.OD46S.backend.services.login.AuthService;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitAuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AdministratorRepository administratorRepository;

    @Mock
    private MotoristaRepository motoristaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtil;

    @InjectMocks
    private AuthService authService;

    private Usuario mockUsuario;
    private Administrator mockAdministrator;
    private Motorista mockMotorista;

    @BeforeEach
    void setUp() {
        mockUsuario = new Usuario();
        mockUsuario.setId(1L);
        mockUsuario.setName("Test User");
        mockUsuario.setEmail("test@od46s.com");
        mockUsuario.setCpf("12345678901");
        mockUsuario.setPassword("$2a$10$encoded.password");
        mockUsuario.setActive(true);

        mockAdministrator = new Administrator();
        mockAdministrator.setId(1L);
        mockAdministrator.setAccessLevel("ADMIN");
        mockAdministrator.setDepartment("TI");

        mockMotorista = new Motorista();
        mockMotorista.setId(2L);
        mockMotorista.setLicenseNumber("12345678901");
        mockMotorista.setLicenseCategory(CategoriaCNH.B);
        mockMotorista.setLicenseExpiry(LocalDate.of(2030, 12, 31));
        mockMotorista.setEnabled(true);
    }

    @Test
    void testLogin_Success_Admin() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@od46s.com");
        loginRequest.setPassword("password123");

        when(usuarioRepository.findByEmail("test@od46s.com"))
                .thenReturn(Optional.of(mockUsuario));
        when(passwordEncoder.matches("password123", "$2a$10$encoded.password"))
                .thenReturn(true);
        when(administratorRepository.findById(1L))
                .thenReturn(Optional.of(mockAdministrator));
        when(jwtUtil.generateToken("test@od46s.com"))
                .thenReturn("jwt.token");

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("jwt.token", result.getToken());
        assertEquals("test@od46s.com", result.getEmail());
        assertEquals("Test User", result.getName());
        assertEquals("ADMIN", result.getType());
    }

    @Test
    void testLogin_Success_Driver() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("driver@od46s.com");
        loginRequest.setPassword("password123");

        Usuario driverUser = new Usuario();
        driverUser.setId(2L);
        driverUser.setName("Driver User");
        driverUser.setEmail("driver@od46s.com");
        driverUser.setPassword("$2a$10$encoded.password");
        driverUser.setActive(true);

        when(usuarioRepository.findByEmail("driver@od46s.com"))
                .thenReturn(Optional.of(driverUser));
        when(passwordEncoder.matches("password123", "$2a$10$encoded.password"))
                .thenReturn(true);
        when(administratorRepository.findById(2L))
                .thenReturn(Optional.empty());
        when(motoristaRepository.findById(2L))
                .thenReturn(Optional.of(mockMotorista));
        when(jwtUtil.generateToken("driver@od46s.com"))
                .thenReturn("jwt.token");

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("jwt.token", result.getToken());
        assertEquals("driver@od46s.com", result.getEmail());
        assertEquals("Driver User", result.getName());
        assertEquals("DRIVER", result.getType());
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@od46s.com");
        loginRequest.setPassword("password123");

        when(usuarioRepository.findByEmail("nonexistent@od46s.com"))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("Usuário não encontrado por email", exception.getMessage());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@od46s.com");
        loginRequest.setPassword("wrongpassword");

        when(usuarioRepository.findByEmail("test@od46s.com"))
                .thenReturn(Optional.of(mockUsuario));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encoded.password"))
                .thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("Senha inválida", exception.getMessage());
    }

    @Test
    void testLogin_InactiveUser() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@od46s.com");
        loginRequest.setPassword("password123");

        mockUsuario.setActive(false);
        when(usuarioRepository.findByEmail("test@od46s.com"))
                .thenReturn(Optional.of(mockUsuario));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("Usuário inativo", exception.getMessage());
    }

    @Test
    void testRegister_Success_Admin() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("New Admin");
        registerRequest.setEmail("newadmin@od46s.com");
        registerRequest.setCpf("98765432100");
        registerRequest.setPassword("password123");
        registerRequest.setType("ADMIN");
        registerRequest.setAccessLevel("ADMIN");
        registerRequest.setDepartment("TI");

        when(usuarioRepository.existsByEmail("newadmin@od46s.com"))
                .thenReturn(false);
        when(usuarioRepository.existsByCpf("98765432100"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("$2a$10$encoded.password");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(mockUsuario);
        when(administratorRepository.save(any(Administrator.class)))
                .thenReturn(mockAdministrator);
        when(jwtUtil.generateToken("test@od46s.com"))
                .thenReturn("jwt.token");

        // When
        AuthResponse result = authService.register(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals("jwt.token", result.getToken());
        assertEquals("test@od46s.com", result.getEmail());
        assertEquals("Test User", result.getName());
        assertEquals("ADMIN", result.getType());
    }

    @Test
    void testRegister_EmailExists() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("existing@od46s.com");
        registerRequest.setPassword("password123");
        registerRequest.setType("ADMIN");

        when(usuarioRepository.existsByEmail("existing@od46s.com"))
                .thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });
        assertEquals("Email já cadastrado", exception.getMessage());
    }

    @Test
    void testRefreshToken_Success() {
        // Given
        String token = "valid.jwt.token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getEmailFromToken(token)).thenReturn("test@od46s.com");
        when(usuarioRepository.findByEmail("test@od46s.com"))
                .thenReturn(Optional.of(mockUsuario));
        when(administratorRepository.findById(1L))
                .thenReturn(Optional.of(mockAdministrator));
        when(jwtUtil.generateToken("test@od46s.com"))
                .thenReturn("new.jwt.token");

        // When
        AuthResponse result = authService.refreshToken(token);

        // Then
        assertNotNull(result);
        assertEquals("new.jwt.token", result.getToken());
        assertEquals("test@od46s.com", result.getEmail());
        assertEquals("Test User", result.getName());
        assertEquals("ADMIN", result.getType());
    }

    @Test
    void testRefreshToken_InvalidToken() {
        // Given
        String token = "invalid.token";
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(token);
        });
        assertTrue(exception.getMessage().contains("Erro ao renovar token"));
    }
}

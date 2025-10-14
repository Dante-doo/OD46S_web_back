package utfpr.OD46S.backend.services.login;

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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

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

    private Usuario usuario;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private Administrator administrator;
    private Motorista motorista;

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

        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setCpf("12345678901");
        registerRequest.setPassword("password123");
        registerRequest.setType("ADMIN");

        administrator = new Administrator();
        administrator.setId(1L);
        administrator.setAccessLevel("ADMIN");
        administrator.setDepartment("IT");
        administrator.setCorporatePhone("11987654321");

        motorista = new Motorista();
        motorista.setId(1L);
        motorista.setLicenseNumber("CNH123456789");
        motorista.setLicenseCategory(CategoriaCNH.B);
        motorista.setLicenseExpiry(LocalDate.of(2025, 12, 31));
        motorista.setPhone("47999999999");
        motorista.setEnabled(true);
    }

    @Test
    void testLogin_Success_WithEmail() {
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(administratorRepository.findById(1L)).thenReturn(Optional.of(administrator));
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwtToken");

        AuthResponse result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("jwtToken", result.getToken());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
        assertEquals("ADMIN", result.getType());
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(jwtUtil, times(1)).generateToken("test@example.com");
    }

    @Test
    void testLogin_Success_WithCpf() {
        loginRequest.setEmail(null);
        loginRequest.setCpf("12345678901");

        when(usuarioRepository.findByCpf("12345678901")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(administratorRepository.findById(1L)).thenReturn(Optional.empty());
        when(motoristaRepository.findById(1L)).thenReturn(Optional.of(motorista));
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwtToken");

        AuthResponse result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("jwtToken", result.getToken());
        assertEquals("DRIVER", result.getType());
        verify(usuarioRepository, times(1)).findByCpf("12345678901");
    }

    @Test
    void testLogin_UserNotFound_ByEmail() {
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Usuário não encontrado por email", exception.getMessage());
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testLogin_UserNotFound_ByCpf() {
        loginRequest.setEmail(null);
        loginRequest.setCpf("12345678901");

        when(usuarioRepository.findByCpf("12345678901")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Usuário não encontrado por CPF", exception.getMessage());
        verify(usuarioRepository, times(1)).findByCpf("12345678901");
    }

    @Test
    void testLogin_NoEmailOrCpf() {
        loginRequest.setEmail(null);
        loginRequest.setCpf(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Necessário informar email ou CPF", exception.getMessage());
    }

    @Test
    void testLogin_InactiveUser() {
        usuario.setActive(false);
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Usuário inativo", exception.getMessage());
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testLogin_InvalidPassword() {
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Senha inválida", exception.getMessage());
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
    }

    @Test
    void testLogin_UserType_Default() {
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(administratorRepository.findById(1L)).thenReturn(Optional.empty());
        when(motoristaRepository.findById(1L)).thenReturn(Optional.empty());
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwtToken");

        AuthResponse result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("USER", result.getType());
    }

    @Test
    void testRegister_Admin_Success() {
        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(administratorRepository.save(any(Administrator.class))).thenReturn(administrator);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwtToken");

        AuthResponse result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("jwtToken", result.getToken());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
        assertEquals("ADMIN", result.getType());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(administratorRepository, times(1)).save(any(Administrator.class));
        verify(jwtUtil, times(1)).generateToken("test@example.com");
    }

    @Test
    void testRegister_Driver_Success() {
        registerRequest.setType("DRIVER");
        registerRequest.setLicenseNumber("CNH123456789");
        registerRequest.setLicenseCategory("B");
        registerRequest.setLicenseExpiry("2025-12-31");
        registerRequest.setPhone("47999999999");

        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);
        when(motoristaRepository.existsByLicenseNumber("CNH123456789")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(motoristaRepository.save(any(Motorista.class))).thenReturn(motorista);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwtToken");

        AuthResponse result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("jwtToken", result.getToken());
        assertEquals("DRIVER", result.getType());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(motoristaRepository, times(1)).save(any(Motorista.class));
    }

    @Test
    void testRegister_EmailExists() {
        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        assertEquals("Email já cadastrado", exception.getMessage());
        verify(usuarioRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    void testRegister_CpfExists() {
        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        assertEquals("CPF já cadastrado", exception.getMessage());
        verify(usuarioRepository, times(1)).existsByCpf("12345678901");
    }

    @Test
    void testRegister_Driver_MissingLicenseData() {
        registerRequest.setType("DRIVER");
        registerRequest.setLicenseNumber(null);

        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        assertEquals("Dados da CNH são obrigatórios para motoristas", exception.getMessage());
    }

    @Test
    void testRegister_Driver_LicenseNumberExists() {
        registerRequest.setType("DRIVER");
        registerRequest.setLicenseNumber("CNH123456789");
        registerRequest.setLicenseCategory("B");
        registerRequest.setLicenseExpiry("2025-12-31");

        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);
        when(motoristaRepository.existsByLicenseNumber("CNH123456789")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        assertEquals("Número da CNH já cadastrado", exception.getMessage());
        verify(motoristaRepository, times(1)).existsByLicenseNumber("CNH123456789");
    }

    @Test
    void testRefreshToken_Success() {
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        when(jwtUtil.getEmailFromToken("validToken")).thenReturn("test@example.com");
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(administratorRepository.findById(1L)).thenReturn(Optional.of(administrator));
        when(jwtUtil.generateToken("test@example.com")).thenReturn("newToken");

        AuthResponse result = authService.refreshToken("validToken");

        assertNotNull(result);
        assertEquals("newToken", result.getToken());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
        assertEquals("ADMIN", result.getType());
        verify(jwtUtil, times(1)).validateToken("validToken");
        verify(jwtUtil, times(1)).getEmailFromToken("validToken");
        verify(jwtUtil, times(1)).generateToken("test@example.com");
    }

    @Test
    void testRefreshToken_InvalidToken() {
        when(jwtUtil.validateToken("invalidToken")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.refreshToken("invalidToken"));
        assertTrue(exception.getMessage().contains("Erro ao renovar token"));
        verify(jwtUtil, times(1)).validateToken("invalidToken");
    }

    @Test
    void testRefreshToken_UserNotFound() {
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        when(jwtUtil.getEmailFromToken("validToken")).thenReturn("test@example.com");
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.refreshToken("validToken"));
        assertTrue(exception.getMessage().contains("Erro ao renovar token"));
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testRefreshToken_InactiveUser() {
        usuario.setActive(false);
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        when(jwtUtil.getEmailFromToken("validToken")).thenReturn("test@example.com");
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.refreshToken("validToken"));
        assertTrue(exception.getMessage().contains("Erro ao renovar token"));
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testRegister_Admin_WithAccessLevel() {
        registerRequest.setAccessLevel("SUPER_ADMIN");
        registerRequest.setDepartment("HR");
        registerRequest.setCorporatePhone("1199887766");

        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(administratorRepository.save(any(Administrator.class))).thenReturn(administrator);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwtToken");

        AuthResponse result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("ADMIN", result.getType());
        verify(administratorRepository, times(1)).save(any(Administrator.class));
    }
}

package utfpr.OD46S.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import utfpr.OD46S.backend.dtos.UsuarioDTO;
import utfpr.OD46S.backend.entitys.Administrator;
import utfpr.OD46S.backend.entitys.Motorista;
import utfpr.OD46S.backend.entitys.Usuario;
import utfpr.OD46S.backend.enums.CategoriaCNH;
import utfpr.OD46S.backend.repositorys.AdministratorRepository;
import utfpr.OD46S.backend.repositorys.MotoristaRepository;
import utfpr.OD46S.backend.repositorys.UsuarioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AdministratorRepository administratorRepository;

    @Mock
    private MotoristaRepository motoristaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private UsuarioDTO usuarioDTO;
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
        usuario.setCreatedAt(LocalDateTime.now());
        usuario.setUpdatedAt(LocalDateTime.now());

        administrator = new Administrator();
        administrator.setId(1L);
        administrator.setAccessLevel("ADMIN");
        administrator.setDepartment("IT");
        administrator.setCorporatePhone("11987654321");

        motorista = new Motorista();
        motorista.setId(1L);
        motorista.setLicenseNumber("12345678901");
        motorista.setLicenseCategory(CategoriaCNH.B);
        motorista.setLicenseExpiry(LocalDate.of(2025, 12, 31));
        motorista.setPhone("47999999999");
        motorista.setEnabled(true);

        usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(1L);
        usuarioDTO.setName("Test User");
        usuarioDTO.setEmail("test@example.com");
        usuarioDTO.setCpf("12345678901");
        usuarioDTO.setPassword("rawPassword");
        usuarioDTO.setActive(true);
        usuarioDTO.setType("ADMIN");
        usuarioDTO.setAccessLevel("ADMIN");
        usuarioDTO.setDepartment("IT");
        usuarioDTO.setCorporatePhone("11987654321");
    }

    @Test
    void testListarUsuarios_Success() {
        List<Usuario> usuarios = Arrays.asList(usuario);
        Page<Usuario> page = new PageImpl<>(usuarios, PageRequest.of(0, 20), 1);

        when(usuarioRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(administratorRepository.existsById(1L)).thenReturn(true);
        when(administratorRepository.findById(1L)).thenReturn(Optional.of(administrator));

        Page<UsuarioDTO> result = usuarioService.listarUsuarios("", "", null, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(usuarioRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testListarUsuarios_WithActiveFilter() {
        List<Usuario> usuarios = Arrays.asList(usuario);
        Page<Usuario> page = new PageImpl<>(usuarios, PageRequest.of(0, 20), 1);

        when(usuarioRepository.findByActive(true, PageRequest.of(0, 20))).thenReturn(page);
        when(administratorRepository.existsById(1L)).thenReturn(true);
        when(administratorRepository.findById(1L)).thenReturn(Optional.of(administrator));

        Page<UsuarioDTO> result = usuarioService.listarUsuarios("", "", true, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(usuarioRepository, times(1)).findByActive(true, PageRequest.of(0, 20));
    }

    @Test
    void testListarUsuarios_WithSearchFilter() {
        List<Usuario> usuarios = Arrays.asList(usuario);
        Page<Usuario> page = new PageImpl<>(usuarios, PageRequest.of(0, 20), 1);

        when(usuarioRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(administratorRepository.existsById(1L)).thenReturn(true);
        when(administratorRepository.findById(1L)).thenReturn(Optional.of(administrator));

        Page<UsuarioDTO> result = usuarioService.listarUsuarios("Test", "", null, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(usuarioRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testListarUsuarios_WithTypeFilter() {
        List<Usuario> usuarios = Arrays.asList(usuario);
        Page<Usuario> page = new PageImpl<>(usuarios, PageRequest.of(0, 20), 1);

        when(usuarioRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(administratorRepository.existsById(1L)).thenReturn(true);
        when(administratorRepository.findById(1L)).thenReturn(Optional.of(administrator));

        Page<UsuarioDTO> result = usuarioService.listarUsuarios("", "ADMIN", null, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(usuarioRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testObterUsuario_AdminSuccess() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(administratorRepository.existsById(1L)).thenReturn(true);
        when(administratorRepository.findById(1L)).thenReturn(Optional.of(administrator));

        UsuarioDTO result = usuarioService.obterUsuario(1L);

        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("ADMIN", result.getType());
        assertEquals("IT", result.getDepartment());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(administratorRepository, times(1)).existsById(1L);
        verify(administratorRepository, times(1)).findById(1L);
    }

    @Test
    void testObterUsuario_DriverSuccess() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(administratorRepository.existsById(1L)).thenReturn(false);
        when(motoristaRepository.existsById(1L)).thenReturn(true);
        when(motoristaRepository.findById(1L)).thenReturn(Optional.of(motorista));

        UsuarioDTO result = usuarioService.obterUsuario(1L);

        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("DRIVER", result.getType());
        assertEquals("12345678901", result.getLicenseNumber());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(administratorRepository, times(1)).existsById(1L);
        verify(motoristaRepository, times(1)).existsById(1L);
        verify(motoristaRepository, times(1)).findById(1L);
    }

    @Test
    void testObterUsuario_NotFound() {
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> usuarioService.obterUsuario(1L));
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void testCriarUsuario_EmailAlreadyExists() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> usuarioService.criarUsuario(usuarioDTO));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void testCriarUsuario_CpfAlreadyExists() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> usuarioService.criarUsuario(usuarioDTO));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void testCriarUsuario_DriverLicenseNumberAlreadyExists() {
        usuarioDTO.setType("DRIVER");
        usuarioDTO.setLicenseNumber("existingLicense");
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(motoristaRepository.existsByLicenseNumber("existingLicense")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> usuarioService.criarUsuario(usuarioDTO));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void testAtualizarUsuario_NotFound() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> usuarioService.atualizarUsuario(1L, usuarioDTO));
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void testRemoverUsuario_NotFound() {
        when(usuarioRepository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> usuarioService.removerUsuario(1L));
        verify(usuarioRepository, times(1)).existsById(1L);
    }
}
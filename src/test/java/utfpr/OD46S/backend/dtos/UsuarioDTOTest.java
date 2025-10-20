package utfpr.OD46S.backend.dtos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utfpr.OD46S.backend.entitys.Usuario;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioDTOTest {

    private UsuarioDTO usuarioDTO;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(1L);
        usuarioDTO.setName("Test User");
        usuarioDTO.setEmail("test@example.com");
        usuarioDTO.setCpf("12345678901");
        usuarioDTO.setPassword("password123");
        usuarioDTO.setActive(true);
        usuarioDTO.setType("ADMIN");
        usuarioDTO.setAccessLevel("ADMIN");
        usuarioDTO.setDepartment("IT");
        usuarioDTO.setCorporatePhone("11987654321");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setName("Test User");
        usuario.setEmail("test@example.com");
        usuario.setCpf("12345678901");
        usuario.setPassword("password123");
        usuario.setActive(true);
        usuario.setCreatedAt(LocalDateTime.now());
        usuario.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, usuarioDTO.getId());
        assertEquals("Test User", usuarioDTO.getName());
        assertEquals("test@example.com", usuarioDTO.getEmail());
        assertEquals("12345678901", usuarioDTO.getCpf());
        assertEquals("password123", usuarioDTO.getPassword());
        assertTrue(usuarioDTO.getActive());
        assertEquals("ADMIN", usuarioDTO.getType());
        assertEquals("ADMIN", usuarioDTO.getAccessLevel());
        assertEquals("IT", usuarioDTO.getDepartment());
        assertEquals("11987654321", usuarioDTO.getCorporatePhone());
    }

    @Test
    void testConstructor() {
        UsuarioDTO newUsuarioDTO = new UsuarioDTO();
        assertNotNull(newUsuarioDTO);
    }

    @Test
    void testToEntity() {
        Usuario entity = usuarioDTO.toEntity();

        assertNotNull(entity);
        assertEquals(usuarioDTO.getName(), entity.getName());
        assertEquals(usuarioDTO.getEmail(), entity.getEmail());
        assertEquals(usuarioDTO.getCpf(), entity.getCpf());
        assertEquals(usuarioDTO.getPassword(), entity.getPassword());
        assertEquals(usuarioDTO.getActive(), entity.getActive());
    }

    @Test
    void testFromEntity() {
        UsuarioDTO result = UsuarioDTO.fromEntity(usuario);

        assertNotNull(result);
        assertEquals(usuario.getId(), result.getId());
        assertEquals(usuario.getName(), result.getName());
        assertEquals(usuario.getEmail(), result.getEmail());
        assertEquals("123.***.***-01", result.getCpf()); // CPF mascarado
        assertNull(result.getPassword()); // Senha não é retornada por segurança
        assertEquals(usuario.getActive(), result.getActive());
        assertNull(result.getType());
        assertNull(result.getAccessLevel());
        assertNull(result.getDepartment());
        assertNull(result.getCorporatePhone());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void testMaskCpf() {
        // Test with valid CPF
        String maskedCpf = UsuarioDTO.fromEntity(usuario).getCpf();
        assertEquals("123.***.***-01", maskedCpf);

        // Test with null CPF
        usuario.setCpf(null);
        maskedCpf = UsuarioDTO.fromEntity(usuario).getCpf();
        assertNull(maskedCpf);

        // Test with invalid length CPF
        usuario.setCpf("123");
        maskedCpf = UsuarioDTO.fromEntity(usuario).getCpf();
        assertEquals("123", maskedCpf);
    }

    @Test
    void testMaskCpf_ShortCpf() {
        usuario.setCpf("123456");
        String maskedCpf = UsuarioDTO.fromEntity(usuario).getCpf();
        assertEquals("123456", maskedCpf);
    }

    @Test
    void testMaskCpf_ExactLength() {
        usuario.setCpf("12345678901");
        String maskedCpf = UsuarioDTO.fromEntity(usuario).getCpf();
        assertEquals("123.***.***-01", maskedCpf);
    }

    @Test
    void testMaskCpf_LongCpf() {
        usuario.setCpf("123456789012");
        String maskedCpf = UsuarioDTO.fromEntity(usuario).getCpf();
        // Test that CPF masking works for long CPF
        assertNotNull(maskedCpf);
        // For long CPF, it should return the original value if not exactly 11 digits
        assertEquals("123456789012", maskedCpf);
    }

    @Test
    void testDriverFields() {
        usuarioDTO.setType("DRIVER");
        usuarioDTO.setLicenseNumber("CNH123456789");
        usuarioDTO.setLicenseCategory("B");
        usuarioDTO.setLicenseExpiry("2025-12-31");
        usuarioDTO.setPhone("47999999999");
        usuarioDTO.setEnabled(true);

        assertEquals("DRIVER", usuarioDTO.getType());
        assertEquals("CNH123456789", usuarioDTO.getLicenseNumber());
        assertEquals("B", usuarioDTO.getLicenseCategory());
        assertEquals("2025-12-31", usuarioDTO.getLicenseExpiry());
        assertEquals("47999999999", usuarioDTO.getPhone());
        assertTrue(usuarioDTO.getEnabled());
    }

    @Test
    void testAdminFields() {
        usuarioDTO.setType("ADMIN");
        usuarioDTO.setAccessLevel("SUPER_ADMIN");
        usuarioDTO.setDepartment("Human Resources");
        usuarioDTO.setCorporatePhone("1199887766");

        assertEquals("ADMIN", usuarioDTO.getType());
        assertEquals("SUPER_ADMIN", usuarioDTO.getAccessLevel());
        assertEquals("Human Resources", usuarioDTO.getDepartment());
        assertEquals("1199887766", usuarioDTO.getCorporatePhone());
    }

    @Test
    void testNullFields() {
        UsuarioDTO nullDTO = new UsuarioDTO();
        
        assertNull(nullDTO.getId());
        assertNull(nullDTO.getName());
        assertNull(nullDTO.getEmail());
        assertNull(nullDTO.getCpf());
        assertNull(nullDTO.getPassword());
        // Active has default value of true
        assertTrue(nullDTO.getActive());
        assertNull(nullDTO.getType());
        assertNull(nullDTO.getAccessLevel());
        assertNull(nullDTO.getDepartment());
        assertNull(nullDTO.getCorporatePhone());
        assertNull(nullDTO.getLicenseNumber());
        assertNull(nullDTO.getLicenseCategory());
        assertNull(nullDTO.getLicenseExpiry());
        assertNull(nullDTO.getPhone());
        assertNull(nullDTO.getEnabled());
    }

    @Test
    void testBooleanFields() {
        usuarioDTO.setActive(false);
        assertFalse(usuarioDTO.getActive());

        usuarioDTO.setActive(true);
        assertTrue(usuarioDTO.getActive());

        usuarioDTO.setEnabled(false);
        assertFalse(usuarioDTO.getEnabled());

        usuarioDTO.setEnabled(true);
        assertTrue(usuarioDTO.getEnabled());
    }

    @Test
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        usuarioDTO.setCreatedAt(now);
        usuarioDTO.setUpdatedAt(now);

        assertEquals(now, usuarioDTO.getCreatedAt());
        assertEquals(now, usuarioDTO.getUpdatedAt());
    }

    @Test
    void testToEntityWithNullFields() {
        UsuarioDTO nullDTO = new UsuarioDTO();
        Usuario entity = nullDTO.toEntity();

        assertNotNull(entity);
        assertNull(entity.getName());
        assertNull(entity.getEmail());
        assertNull(entity.getCpf());
        assertNull(entity.getPassword());
        // Active has default value of true
        assertTrue(entity.getActive());
    }

    @Test
    void testFromEntityWithNullFields() {
        Usuario nullUsuario = new Usuario();
        UsuarioDTO result = UsuarioDTO.fromEntity(nullUsuario);

        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getEmail());
        assertNull(result.getCpf());
        assertNull(result.getPassword());
        // Active has default value of true
        assertTrue(result.getActive());
    }
}
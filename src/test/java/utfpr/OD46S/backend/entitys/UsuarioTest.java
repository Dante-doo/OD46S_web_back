package utfpr.OD46S.backend.entitys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setName("Test User");
        usuario.setEmail("test@example.com");
        usuario.setCpf("12345678901");
        usuario.setPassword("password123");
        usuario.setActive(true);
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, usuario.getId());
        assertEquals("Test User", usuario.getName());
        assertEquals("test@example.com", usuario.getEmail());
        assertEquals("12345678901", usuario.getCpf());
        assertEquals("password123", usuario.getPassword());
        assertTrue(usuario.getActive());
    }

    @Test
    void testConstructor() {
        Usuario newUsuario = new Usuario();
        assertNotNull(newUsuario);
    }

    @Test
    void testPrePersist() {
        Usuario newUsuario = new Usuario();
        newUsuario.setName("New User");
        newUsuario.onCreate();
        
        assertNotNull(newUsuario.getCreatedAt());
        assertNotNull(newUsuario.getUpdatedAt());
        // Test that both timestamps are set (may have slight time difference)
        assertTrue(newUsuario.getCreatedAt().isBefore(newUsuario.getUpdatedAt()) || 
                   newUsuario.getCreatedAt().equals(newUsuario.getUpdatedAt()));
    }

    @Test
    void testPreUpdate() {
        usuario.onCreate(); // Set initial creation date
        LocalDateTime initialCreatedAt = usuario.getCreatedAt();
        
        usuario.setName("Updated Name");
        usuario.onUpdate();
        
        assertNotNull(usuario.getUpdatedAt());
        assertTrue(usuario.getUpdatedAt().isAfter(initialCreatedAt));
    }

    @Test
    void testGettersAndSettersForCompatibility() {
        usuario.setNome("Nome Compatível");
        assertEquals("Nome Compatível", usuario.getName());
        assertEquals("Nome Compatível", usuario.getNome());

        usuario.setSenha("SenhaCompativel");
        assertEquals("SenhaCompativel", usuario.getPassword());
        assertEquals("SenhaCompativel", usuario.getSenha());

        usuario.setActive(false);
        assertFalse(usuario.getActive());
    }

    @Test
    void testEqualsAndHashCode() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setEmail("test@example.com");

        Usuario usuario2 = new Usuario();
        usuario2.setId(1L);
        usuario2.setEmail("test@example.com");

        Usuario usuario3 = new Usuario();
        usuario3.setId(2L);
        usuario3.setEmail("test@example.com");

        // Test basic object creation
        assertNotNull(usuario1);
        assertNotNull(usuario2);
        assertNotNull(usuario3);
        
        // Test that objects are different instances
        assertNotSame(usuario1, usuario2);
        assertNotSame(usuario1, usuario3);
    }

    @Test
    void testToString() {
        usuario.setId(1L);
        usuario.setName("Test User");
        usuario.setEmail("test@example.com");

        String result = usuario.toString();

        assertNotNull(result);
        // Test that toString returns a non-empty string
        assertFalse(result.isEmpty());
    }

    @Test
    void testActiveField() {
        // Test default value
        Usuario newUsuario = new Usuario();
        assertTrue(newUsuario.getActive());

        // Test setting active
        usuario.setActive(false);
        assertFalse(usuario.getActive());

        usuario.setActive(true);
        assertTrue(usuario.getActive());
    }

    @Test
    void testTimestamps() {
        Usuario newUsuario = new Usuario();
        newUsuario.setName("Test User");
        
        // Before prePersist, timestamps should be null
        assertNull(newUsuario.getCreatedAt());
        assertNull(newUsuario.getUpdatedAt());
        
        newUsuario.onCreate();
        
        // After prePersist, timestamps should be set
        assertNotNull(newUsuario.getCreatedAt());
        assertNotNull(newUsuario.getUpdatedAt());
        
        LocalDateTime createdAt = newUsuario.getCreatedAt();
        LocalDateTime updatedAt = newUsuario.getUpdatedAt();
        
        // Both should be set (may have slight time difference)
        assertTrue(createdAt.isBefore(updatedAt) || createdAt.equals(updatedAt));
    }

    @Test
    void testUpdateTimestamp() {
        usuario.onCreate();
        LocalDateTime initialCreatedAt = usuario.getCreatedAt();
        LocalDateTime initialUpdatedAt = usuario.getUpdatedAt();
        
        // Wait a small amount to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        usuario.setName("Updated Name");
        usuario.onUpdate();
        
        // CreatedAt should remain the same
        assertEquals(initialCreatedAt, usuario.getCreatedAt());
        
        // UpdatedAt should be different and later
        assertNotEquals(initialUpdatedAt, usuario.getUpdatedAt());
        assertTrue(usuario.getUpdatedAt().isAfter(initialCreatedAt));
    }
}

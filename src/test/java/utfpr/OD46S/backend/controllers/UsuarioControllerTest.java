package utfpr.OD46S.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import utfpr.OD46S.backend.dtos.UsuarioDTO;
import utfpr.OD46S.backend.services.UsuarioService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private ObjectMapper objectMapper;
    private UsuarioDTO usuarioDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        usuarioDTO = UsuarioDTO.builder()
                .id(1L)
                .name("Test User")
                .email("test@od46s.com")
                .cpf("12345678901")
                .type("ADMIN")
                .active(true)
                .accessLevel("ADMIN")
                .department("TI")
                .corporatePhone("47999999999")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testListarUsuarios_Success() {
        // Given
        List<UsuarioDTO> usuarios = Arrays.asList(usuarioDTO);
        Page<UsuarioDTO> page = new PageImpl<>(usuarios, PageRequest.of(0, 20), 1);
        
        when(usuarioService.listarUsuarios(anyString(), anyString(), anyBoolean(), any(Pageable.class)))
                .thenReturn(page);

        // When
        ResponseEntity<Map<String, Object>> response = usuarioController.listar(
                1, 20, "test", "ADMIN", true, "name", "asc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("success"));
        assertTrue(response.getBody().containsKey("data"));
        assertTrue((Boolean) response.getBody().get("success"));
        
        verify(usuarioService, times(1)).listarUsuarios(eq("test"), eq("ADMIN"), eq(true), any(Pageable.class));
    }

    @Test
    void testObterUsuario_Success() {
        // Given
        when(usuarioService.obterUsuario(1L)).thenReturn(usuarioDTO);

        // When
        ResponseEntity<Map<String, Object>> response = usuarioController.obterUsuario(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("success"));
        assertTrue(response.getBody().containsKey("data"));
        assertTrue((Boolean) response.getBody().get("success"));
        
        verify(usuarioService, times(1)).obterUsuario(1L);
    }

    @Test
    void testObterUsuario_NotFound() {
        // Given
        when(usuarioService.obterUsuario(999L))
                .thenThrow(new RuntimeException("Usuário não encontrado"));

        // When
        ResponseEntity<Map<String, Object>> response = usuarioController.obterUsuario(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("success"));
        assertTrue(response.getBody().containsKey("error"));
        assertFalse((Boolean) response.getBody().get("success"));
        
        verify(usuarioService, times(1)).obterUsuario(999L);
    }

    @Test
    void testCriarUsuario_Success() {
        // Given
        when(usuarioService.criarUsuario(any(UsuarioDTO.class))).thenReturn(usuarioDTO);

        // When
        ResponseEntity<Map<String, Object>> response = usuarioController.criarUsuario(usuarioDTO);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("success"));
        assertTrue(response.getBody().containsKey("data"));
        assertTrue(response.getBody().containsKey("message"));
        assertTrue((Boolean) response.getBody().get("success"));
        
        verify(usuarioService, times(1)).criarUsuario(usuarioDTO);
    }

    @Test
    void testCriarUsuario_EmailExists() {
        // Given
        when(usuarioService.criarUsuario(any(UsuarioDTO.class)))
                .thenThrow(new RuntimeException("Email já cadastrado"));

        // When
        ResponseEntity<Map<String, Object>> response = usuarioController.criarUsuario(usuarioDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("success"));
        assertTrue(response.getBody().containsKey("error"));
        assertFalse((Boolean) response.getBody().get("success"));
        
        verify(usuarioService, times(1)).criarUsuario(usuarioDTO);
    }

    @Test
    void testAtualizarUsuario_Success() {
        // Given
        UsuarioDTO updatedDTO = UsuarioDTO.builder()
                .name("Updated User")
                .phone("47988888888")
                .build();
        
        when(usuarioService.atualizarUsuario(eq(1L), any(UsuarioDTO.class))).thenReturn(usuarioDTO);

        // When
        ResponseEntity<Map<String, Object>> response = usuarioController.atualizarUsuario(1L, updatedDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("success"));
        assertTrue(response.getBody().containsKey("data"));
        assertTrue(response.getBody().containsKey("message"));
        assertTrue((Boolean) response.getBody().get("success"));
        
        verify(usuarioService, times(1)).atualizarUsuario(1L, updatedDTO);
    }

    @Test
    void testAtualizarUsuario_NotFound() {
        // Given
        when(usuarioService.atualizarUsuario(eq(999L), any(UsuarioDTO.class)))
                .thenThrow(new RuntimeException("Usuário não encontrado"));

        // When
        ResponseEntity<Map<String, Object>> response = usuarioController.atualizarUsuario(999L, usuarioDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("success"));
        assertTrue(response.getBody().containsKey("error"));
        assertFalse((Boolean) response.getBody().get("success"));
        
        verify(usuarioService, times(1)).atualizarUsuario(999L, usuarioDTO);
    }

    @Test
    void testRemoverUsuario_Success() {
        // Given
        doNothing().when(usuarioService).removerUsuario(1L);

        // When
        ResponseEntity<Map<String, Object>> response = usuarioController.removerUsuario(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("success"));
        assertTrue(response.getBody().containsKey("message"));
        assertTrue((Boolean) response.getBody().get("success"));
        
        verify(usuarioService, times(1)).removerUsuario(1L);
    }

    @Test
    void testRemoverUsuario_NotFound() {
        // Given
        doThrow(new RuntimeException("Usuário não encontrado"))
                .when(usuarioService).removerUsuario(999L);

        // When
        ResponseEntity<Map<String, Object>> response = usuarioController.removerUsuario(999L);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("success"));
        assertTrue(response.getBody().containsKey("error"));
        assertFalse((Boolean) response.getBody().get("success"));
        
        verify(usuarioService, times(1)).removerUsuario(999L);
    }
}

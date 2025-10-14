package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import utfpr.OD46S.backend.dtos.UsuarioDTO;
import utfpr.OD46S.backend.services.UsuarioService;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Usuários", description = "Gestão de usuários")
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
@Validated
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Listar usuários (paginado)")
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar(
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Itens por página") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Buscar por nome ou email") @RequestParam(required = false) String search,
            @Parameter(description = "Filtrar por tipo") @RequestParam(required = false) String type,
            @Parameter(description = "Filtrar por status ativo") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "name") String sort,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "asc") String order) {
        
        // Validar parâmetros
        if (page < 1) page = 1;
        if (limit < 1 || limit > 100) limit = 20;
        
        // Criar objeto de paginação
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort));
        
        Page<UsuarioDTO> result = usuarioService.listarUsuarios(search, type, active, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", Map.of(
            "users", result.getContent(),
            "pagination", Map.of(
                "current_page", result.getNumber() + 1,
                "per_page", result.getSize(),
                "total", result.getTotalElements(),
                "total_pages", result.getTotalPages(),
                "has_next", result.hasNext(),
                "has_prev", result.hasPrevious()
            )
        ));
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obter usuário específico")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obterUsuario(@PathVariable Long id) {
        try {
            UsuarioDTO usuario = usuarioService.obterUsuario(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("user", usuario));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of(
                "code", "USER_NOT_FOUND",
                "message", "Usuário não encontrado"
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Criar novo usuário")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> criarUsuario(@Valid @RequestBody UsuarioDTO dto) {
        try {
            UsuarioDTO usuario = usuarioService.criarUsuario(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("user", usuario));
            response.put("message", "Usuário criado com sucesso");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of(
                "code", "USER_CREATION_FAILED",
                "message", e.getMessage()
            ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "Atualizar usuário")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> atualizarUsuario(@PathVariable Long id, @RequestBody UsuarioDTO dto) {
        try {
            UsuarioDTO usuario = usuarioService.atualizarUsuario(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("user", usuario));
            response.put("message", "Usuário atualizado com sucesso");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of(
                "code", "USER_UPDATE_FAILED",
                "message", e.getMessage()
            ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "Remover usuário")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> removerUsuario(@PathVariable Long id) {
        try {
            usuarioService.removerUsuario(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuário removido com sucesso");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", Map.of(
                "code", "USER_DELETE_FAILED",
                "message", e.getMessage()
            ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}

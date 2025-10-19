package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import utfpr.OD46S.backend.dtos.RouteCollectionPointDTO;
import utfpr.OD46S.backend.dtos.RouteDTO;
import utfpr.OD46S.backend.services.RouteService;

import java.util.List;
import java.util.Map;

@Tag(name = "Rotas", description = "Gestão de rotas de coleta")
@RestController
@RequestMapping("/api/v1/routes")
@CrossOrigin(origins = "*")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @Operation(summary = "Listar rotas")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public ResponseEntity<Map<String, Object>> listar(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String search,
        @RequestParam(name = "collection_type", required = false) String collectionType,
        @RequestParam(required = false) String priority,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Map<String, Object> response = routeService.listarTodos(
            page, limit, search, collectionType, priority, active, sort, order
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar rota por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public ResponseEntity<Map<String, Object>> buscarPorId(@PathVariable Long id) {
        Map<String, Object> response = routeService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Criar nova rota")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> criar(@RequestBody RouteDTO dto) {
        // Get current user ID (mock value for now, should get from JWT)
        Long createdBy = getCurrentUserId();
        Map<String, Object> response = routeService.criar(dto, createdBy);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Adicionar ponto à rota")
    @PostMapping("/{id}/points")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adicionarPonto(
        @PathVariable Long id,
        @RequestBody RouteCollectionPointDTO pointDTO
    ) {
        Map<String, Object> response = routeService.adicionarPonto(id, pointDTO);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Reordenar pontos de coleta")
    @PutMapping("/{id}/points/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reordenarPontos(
        @PathVariable Long id,
        @RequestBody List<Map<String, Integer>> reorderList
    ) {
        Map<String, Object> response = routeService.reordenarPontos(id, reorderList);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        // This is a simplified version - in a real implementation,
        // you would extract the user ID from the JWT token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            // For now, return a default admin ID
            // TODO: Extract actual user ID from JWT claims
            return 1L;
        }
        return 1L; // Default admin ID
    }
}


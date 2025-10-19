package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import utfpr.OD46S.backend.enums.AssignmentStatus;
import utfpr.OD46S.backend.services.AssignmentService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/assignments")
@Tag(name = "Assignments", description = "Gestão de Escalas/Atribuições (Route + Driver + Vehicle)")
@SecurityRequirement(name = "bearer-key")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @Operation(
            summary = "Listar escalas/atribuições",
            description = "Lista todas as escalas com filtros opcionais. Admin vê todas, Driver vê apenas suas."
    )
    public ResponseEntity<Map<String, Object>> listarAssignments(
            @RequestParam(required = false) Long route_id,
            @RequestParam(required = false) Long driver_id,
            @RequestParam(required = false) Long vehicle_id,
            @RequestParam(required = false) AssignmentStatus status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "desc") String order,
            Authentication authentication) {
        
        // Se for driver, filtra apenas suas escalas
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DRIVER"))) {
            // TODO: Implementar lógica para pegar driver_id do usuário autenticado
            // Por enquanto, manteremos o filtro passado
        }

        Map<String, Object> response = assignmentService.listarAssignments(
                route_id, driver_id, vehicle_id, status, page, limit, sort, order);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @Operation(
            summary = "Obter detalhes de uma escala",
            description = "Retorna informações detalhadas de uma escala específica"
    )
    public ResponseEntity<Map<String, Object>> obterAssignmentPorId(@PathVariable Long id) {
        try {
            Map<String, Object> response = assignmentService.obterAssignmentPorId(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "error", Map.of(
                                    "code", "ASSIGNMENT_NOT_FOUND",
                                    "message", e.getMessage()
                            )
                    ));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Criar escala/atribuição",
            description = "Cria uma nova escala vinculando rota, motorista e veículo. Apenas ADMIN."
    )
    public ResponseEntity<Map<String, Object>> criarAssignment(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            String authenticatedUserEmail = authentication.getName();
            Map<String, Object> response = assignmentService.criarAssignment(request, authenticatedUserEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            String errorCode = "VALIDATION_ERROR";

            if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
                errorCode = "RESOURCE_NOT_FOUND";
            } else if (e.getMessage().contains("already has")) {
                status = HttpStatus.CONFLICT;
                errorCode = "ASSIGNMENT_CONFLICT";
            }

            return ResponseEntity.status(status)
                    .body(Map.of(
                            "success", false,
                            "error", Map.of(
                                    "code", errorCode,
                                    "message", e.getMessage()
                            )
                    ));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Atualizar escala",
            description = "Atualiza uma escala existente. Apenas ADMIN."
    )
    public ResponseEntity<Map<String, Object>> atualizarAssignment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> response = assignmentService.atualizarAssignment(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            }

            return ResponseEntity.status(status)
                    .body(Map.of(
                            "success", false,
                            "error", Map.of(
                                    "code", "UPDATE_ERROR",
                                    "message", e.getMessage()
                            )
                    ));
        }
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Desativar escala",
            description = "Desativa uma escala, encerrando-a. Apenas ADMIN."
    )
    public ResponseEntity<Map<String, Object>> desativarAssignment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            if (request == null) {
                request = Map.of();
            }
            Map<String, Object> response = assignmentService.desativarAssignment(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            return ResponseEntity.status(status)
                    .body(Map.of(
                            "success", false,
                            "error", Map.of(
                                    "code", "ASSIGNMENT_NOT_FOUND",
                                    "message", e.getMessage()
                            )
                    ));
        }
    }

    @GetMapping("/my-current")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(
            summary = "Obter escala atual do motorista",
            description = "Retorna a escala ativa do motorista autenticado. Apenas DRIVER."
    )
    public ResponseEntity<Map<String, Object>> obterMinhaEscalaAtual(Authentication authentication) {
        String driverEmail = authentication.getName();
        Map<String, Object> response = assignmentService.obterAssignmentAtualDoMotorista(driverEmail);
        
        if (Boolean.FALSE.equals(response.get("success"))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}


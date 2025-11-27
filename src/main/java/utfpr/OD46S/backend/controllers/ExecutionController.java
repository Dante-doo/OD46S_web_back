package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import utfpr.OD46S.backend.enums.ExecutionStatus;
import utfpr.OD46S.backend.services.ExecutionService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/executions")
@Tag(name = "Executions", description = "Gestão de Execuções de Coletas")
@SecurityRequirement(name = "bearer-key")
public class ExecutionController {

    @Autowired
    private ExecutionService executionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @Operation(
            summary = "Listar histórico de execuções",
            description = "Lista todas as execuções de coletas com filtros opcionais. Admin vê todas, Driver vê apenas suas."
    )
    public ResponseEntity<Map<String, Object>> listarExecutions(
            @RequestParam(required = false) Long assignment_id,
            @RequestParam(required = false) Long driver_id,
            @RequestParam(required = false) ExecutionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start_date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end_date,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "desc") String order,
            Authentication authentication) {

        // Se for driver, filtra apenas suas execuções
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DRIVER"))) {
            // TODO: Implementar lógica para pegar driver_id do usuário autenticado
            // Por enquanto, manteremos o filtro passado
        }

        Map<String, Object> response = executionService.listarExecutions(
                assignment_id, driver_id, status, start_date, end_date, page, limit, sort, order);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @Operation(
            summary = "Obter detalhes de uma execução",
            description = "Retorna informações detalhadas de uma execução específica"
    )
    public ResponseEntity<Map<String, Object>> obterExecutionPorId(@PathVariable Long id) {
        try {
            Map<String, Object> response = executionService.obterExecutionPorId(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "error", Map.of(
                                    "code", "EXECUTION_NOT_FOUND",
                                    "message", e.getMessage()
                            )
                    ));
        }
    }

    @PostMapping("/start")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(
            summary = "Iniciar coleta",
            description = "Motorista inicia uma nova execução de coleta. Apenas DRIVER."
    )
    public ResponseEntity<Map<String, Object>> iniciarExecution(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            String driverEmail = authentication.getName();
            Map<String, Object> response = executionService.iniciarExecution(request, driverEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            String errorCode = "VALIDATION_ERROR";

            if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
                errorCode = "RESOURCE_NOT_FOUND";
            } else if (e.getMessage().contains("already has") || e.getMessage().contains("already exists")) {
                status = HttpStatus.CONFLICT;
                errorCode = "EXECUTION_CONFLICT";
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

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @Operation(
            summary = "Finalizar coleta",
            description = "Finaliza uma execução de coleta em andamento"
    )
    public ResponseEntity<Map<String, Object>> finalizarExecution(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> response = executionService.finalizarExecution(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            String errorCode = "UPDATE_ERROR";

            if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
                errorCode = "EXECUTION_NOT_FOUND";
            } else if (e.getMessage().contains("not in progress")) {
                status = HttpStatus.CONFLICT;
                errorCode = "EXECUTION_NOT_IN_PROGRESS";
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

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @Operation(
            summary = "Cancelar execução",
            description = "Cancela uma execução de coleta em andamento"
    )
    public ResponseEntity<Map<String, Object>> cancelarExecution(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> response = executionService.cancelarExecution(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            String errorCode = "CANCEL_ERROR";

            if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
                errorCode = "EXECUTION_NOT_FOUND";
            } else if (e.getMessage().contains("not in progress")) {
                status = HttpStatus.CONFLICT;
                errorCode = "EXECUTION_NOT_IN_PROGRESS";
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

    @GetMapping("/my-current")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(
            summary = "Obter coleta em andamento",
            description = "Retorna a execução de coleta em andamento do motorista autenticado. Apenas DRIVER."
    )
    public ResponseEntity<Map<String, Object>> obterMinhaColetaAtual(Authentication authentication) {
        String driverEmail = authentication.getName();
        Map<String, Object> response = executionService.obterExecutionAtualDoMotorista(driverEmail);

        if (Boolean.FALSE.equals(response.get("success"))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(response);
    }
}


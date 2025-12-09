package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import utfpr.OD46S.backend.dtos.VeiculoDTO;
import utfpr.OD46S.backend.enums.StatusVeiculo;
import utfpr.OD46S.backend.services.VeiculoService;

import java.util.Map;

@Tag(name = "Veículos", description = "Gestão de veículos")
@RestController
@RequestMapping("/api/v1/vehicles")
@CrossOrigin(origins = "*")
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

    @Operation(summary = "Listar veículos")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order
    ) {
        // Se não houver parâmetros de paginação, retornar lista simples (compatibilidade)
        if (page == null && limit == null && search == null && status == null && active == null) {
            return ResponseEntity.ok(veiculoService.listarTodos());
        }
        
        // Caso contrário, retornar dados paginados
        Map<String, Object> response = veiculoService.listarTodosPaginado(
            page, limit, search, status, active, sort, order
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cadastrar veículo")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cadastrar(@RequestBody VeiculoDTO dto) {
        try {
            VeiculoDTO veiculo = veiculoService.cadastrar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(veiculo);
        } catch (RuntimeException e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            String errorCode = "VALIDATION_ERROR";
            
            if (e.getMessage().contains("já cadastrada")) {
                status = HttpStatus.CONFLICT;
                errorCode = "DUPLICATE_LICENSE_PLATE";
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

    @Operation(summary = "Atualizar veículo")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody VeiculoDTO dto) {
        try {
            VeiculoDTO veiculo = veiculoService.atualizar(id, dto);
            return ResponseEntity.ok(veiculo);
        } catch (RuntimeException e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            String errorCode = "UPDATE_ERROR";
            
            if (e.getMessage().contains("não encontrado")) {
                status = HttpStatus.NOT_FOUND;
                errorCode = "VEHICLE_NOT_FOUND";
            } else if (e.getMessage().contains("já cadastrada")) {
                status = HttpStatus.CONFLICT;
                errorCode = "DUPLICATE_LICENSE_PLATE";
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

    @Operation(summary = "Alterar status do veículo")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public ResponseEntity<?> alterarStatus(@PathVariable Long id, @RequestParam StatusVeiculo status) {
        try {
            VeiculoDTO veiculo = veiculoService.alterarStatus(id, status);
            return ResponseEntity.ok(veiculo);
        } catch (RuntimeException e) {
            HttpStatus httpStatus = HttpStatus.NOT_FOUND;
            String errorCode = "VEHICLE_NOT_FOUND";
            
            return ResponseEntity.status(httpStatus)
                    .body(Map.of(
                            "success", false,
                            "error", Map.of(
                                    "code", errorCode,
                                    "message", e.getMessage()
                            )
                    ));
        }
    }
}



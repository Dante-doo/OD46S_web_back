package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import utfpr.OD46S.backend.services.GPSTrackingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/executions")
@Tag(name = "GPS Tracking", description = "Rastreamento GPS durante execuções de coletas")
@SecurityRequirement(name = "bearer-key")
public class GPSTrackingController {

    @Autowired
    private GPSTrackingService gpsTrackingService;

    @PostMapping("/{executionId}/gps")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(
            summary = "Registrar posição GPS",
            description = "Registra uma ou múltiplas posições GPS durante a execução da coleta. Apenas DRIVER."
    )
    public ResponseEntity<?> registrarPosicaoGPS(
            @PathVariable Long executionId,
            @RequestBody Object request) {
        try {
            Map<String, Object> response;
            
            // Verificar se é uma lista de posições ou uma única posição
            if (request instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> posicoes = (List<Map<String, Object>>) request;
                response = gpsTrackingService.registrarMultiplasPosicoes(executionId, posicoes);
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> posicao = (Map<String, Object>) request;
                response = gpsTrackingService.registrarPosicaoGPS(executionId, posicao);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            String errorCode = "VALIDATION_ERROR";

            if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
                errorCode = "EXECUTION_NOT_FOUND";
            } else if (e.getMessage().contains("not in progress")) {
                status = HttpStatus.CONFLICT;
                errorCode = "EXECUTION_NOT_IN_PROGRESS";
            } else if (e.getMessage().contains("between")) {
                errorCode = "INVALID_COORDINATES";
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

    @GetMapping("/{executionId}/gps")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @Operation(
            summary = "Obter rastro GPS",
            description = "Retorna o rastro completo de GPS de uma execução, com estatísticas de distância e pontos"
    )
    public ResponseEntity<?> obterRastroGPS(
            @PathVariable Long executionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start_time,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end_time) {
        try {
            Map<String, Object> response = gpsTrackingService.obterRastroGPS(executionId, start_time, end_time);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            String errorCode = "EXECUTION_NOT_FOUND";

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
}


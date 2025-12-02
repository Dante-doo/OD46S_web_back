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
import org.springframework.web.multipart.MultipartFile;
import utfpr.OD46S.backend.services.GPSTrackingService;
import utfpr.OD46S.backend.services.MinioStorageService;

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

    @Autowired
    private MinioStorageService minioStorageService;

    @PostMapping("/{executionId}/gps")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(
            summary = "Registrar posição GPS / Evento",
            description = "Registra posição GPS, eventos (paradas, problemas, etc) e foto opcional durante a execução. Apenas DRIVER."
    )
    public ResponseEntity<?> registrarPosicaoGPS(
            @PathVariable Long executionId,
            @RequestParam(value = "latitude") String latitude,
            @RequestParam(value = "longitude") String longitude,
            @RequestParam(value = "speed_kmh", required = false) String speedKmh,
            @RequestParam(value = "heading_degrees", required = false) String headingDegrees,
            @RequestParam(value = "accuracy_meters", required = false) String accuracyMeters,
            @RequestParam(value = "event_type", defaultValue = "NORMAL") String eventType,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        try {
            // Construir mapa de request
            Map<String, Object> request = new java.util.HashMap<>();
            request.put("latitude", latitude);
            request.put("longitude", longitude);
            if (speedKmh != null) request.put("speed_kmh", speedKmh);
            if (headingDegrees != null) request.put("heading_degrees", headingDegrees);
            if (accuracyMeters != null) request.put("accuracy_meters", accuracyMeters);
            request.put("event_type", eventType);
            if (description != null) request.put("description", description);

            // Upload de foto se fornecida
            if (photo != null && !photo.isEmpty()) {
                String photoUrl = minioStorageService.storeGPSPhoto(executionId, photo);
                request.put("photo_url", photoUrl);
            }

            Map<String, Object> response = gpsTrackingService.registrarPosicaoGPS(executionId, request);
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


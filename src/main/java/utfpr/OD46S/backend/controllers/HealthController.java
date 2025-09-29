package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Saúde do Sistema", description = "APIs para verificação de saúde e status do sistema")
@RestController
public class HealthController {

    @Operation(
        summary = "Verificação detalhada de saúde",
        description = "Retorna informações detalhadas sobre o status e saúde do sistema"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Sistema funcionando corretamente",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "success": true,
                    "data": {
                        "status": "UP",
                        "timestamp": "2025-09-25T22:30:00",
                        "version": "1.0.0",
                        "message": "Sistema OD46S operacional"
                    }
                }
                """)
        )
    )
    @GetMapping("/api/v1/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("version", "1.0.0");
        data.put("message", "Sistema OD46S operacional");
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Verificação simples de saúde",
        description = "Retorna status básico do sistema"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Status básico do sistema",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "status": "UP",
                    "timestamp": "2025-09-25T22:30:00"
                }
                """)
        )
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> simpleHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}

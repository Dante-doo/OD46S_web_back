package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import utfpr.OD46S.backend.services.MinioStorageService;

import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "Files", description = "Acesso a arquivos armazenados")
@SecurityRequirement(name = "bearer-key")
public class FileController {

    @Autowired
    private MinioStorageService minioStorageService;

    @GetMapping("/gps-photos/{executionId}/{gpsRecordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @Operation(
            summary = "Baixar foto GPS/Evento",
            description = "Recupera uma foto armazenada de um registro GPS/evento pelo ID do registro. Requer autenticação."
    )
    public ResponseEntity<InputStreamResource> downloadGPSPhoto(
            @PathVariable Long executionId,
            @PathVariable Long gpsRecordId) {
        
        try {
            InputStream inputStream = minioStorageService.getGPSPhoto(executionId, gpsRecordId);

            // Obter extensão do arquivo
            String extension = minioStorageService.getGPSPhotoExtension(executionId, gpsRecordId);
            
            // Determinar content type
            String contentType = determineContentType(extension);
            
            // Nome do arquivo para download
            String filename = String.format("photo_%d.%s", gpsRecordId, extension);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}


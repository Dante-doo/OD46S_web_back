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

    @GetMapping("/gps-photos/{executionId}/{filename}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @Operation(
            summary = "Baixar foto GPS/Evento",
            description = "Recupera uma foto armazenada de um registro GPS/evento"
    )
    public ResponseEntity<InputStreamResource> downloadGPSPhoto(
            @PathVariable Long executionId,
            @PathVariable String filename) {
        
        try {
            String objectName = String.format("gps-photos/execution_%d/%s", executionId, filename);
            
            InputStream inputStream = minioStorageService.getFile(objectName);

            // Determinar content type
            String contentType = determineContentType(filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}


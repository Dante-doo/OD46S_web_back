package utfpr.OD46S.backend.services;

import io.minio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class MinioStorageService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    /**
     * Armazena uma foto de GPS/evento no MinIO
     */
    public String storeGPSPhoto(Long executionId, MultipartFile file) {
        try {
            // Criar bucket se não existir
            ensureBucketExists();

            // Validar arquivo
            validateFile(file);

            // Gerar nome único para o arquivo
            String fileName = generateUniqueFileName(file.getOriginalFilename());

            // Definir o path no MinIO
            String objectName = String.format("gps-photos/execution_%d/%s", executionId, fileName);

            // Upload do arquivo
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            // Retornar URL de acesso
            return String.format("/api/v1/files/gps-photos/%d/%s", executionId, fileName);

        } catch (Exception e) {
            throw new RuntimeException("Failed to store file in MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera um arquivo do MinIO
     */
    public InputStream getFile(String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve file from MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Garante que o bucket existe, cria se necessário
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to check/create bucket: " + e.getMessage(), e);
        }
    }

    /**
     * Valida o arquivo enviado
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validar tamanho (máximo 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }

        // Validar tipo de arquivo
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.startsWith("image/jpeg") && 
             !contentType.startsWith("image/png") && 
             !contentType.startsWith("image/webp"))) {
            throw new IllegalArgumentException("Only JPEG, PNG and WebP images are allowed");
        }
    }

    /**
     * Gera um nome único para o arquivo
     */
    private String generateUniqueFileName(String originalFilename) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return String.format("photo_%s_%s%s", 
                           timestamp, 
                           UUID.randomUUID().toString().substring(0, 8),
                           extension);
    }
}


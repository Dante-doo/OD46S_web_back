package utfpr.OD46S.backend.services;

import io.minio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class MinioStorageService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    /**
     * Armazena uma foto de GPS/evento no MinIO usando o ID do registro GPS
     * Organiza os arquivos por executionId e usa o gpsRecordId como identificador
     */
    public String storeGPSPhoto(Long executionId, Long gpsRecordId, MultipartFile file) {
        try {
            // Criar bucket se não existir
            ensureBucketExists();

            // Validar arquivo
            validateFile(file);

            // Obter extensão do arquivo original
            String extension = getFileExtension(file.getOriginalFilename());

            // Definir o path no MinIO usando o ID do registro GPS
            // Formato: gps-photos/execution_{id}/{gpsRecordId}.{ext}
            String objectName = String.format("gps-photos/execution_%d/%d%s", executionId, gpsRecordId, extension);

            // Upload do arquivo
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            // Retornar URL de acesso usando o ID do registro
            return String.format("/api/v1/files/gps-photos/%d/%d", executionId, gpsRecordId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to store file in MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera uma foto GPS pelo ID do registro
     */
    public InputStream getGPSPhoto(Long executionId, Long gpsRecordId) {
        try {
            // Tentar diferentes extensões comuns
            String[] extensions = {".jpg", ".jpeg", ".png", ".webp"};
            
            for (String ext : extensions) {
                String objectName = String.format("gps-photos/execution_%d/%d%s", executionId, gpsRecordId, ext);
                try {
                    return minioClient.getObject(
                        GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
                    );
                } catch (Exception e) {
                    // Tentar próxima extensão
                    continue;
                }
            }
            
            throw new RuntimeException("GPS photo not found for record ID: " + gpsRecordId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve GPS photo from MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Obtém a extensão do arquivo GPS pelo ID do registro
     */
    public String getGPSPhotoExtension(Long executionId, Long gpsRecordId) {
        String[] extensions = {".jpg", ".jpeg", ".png", ".webp"};
        
        for (String ext : extensions) {
            String objectName = String.format("gps-photos/execution_%d/%d%s", executionId, gpsRecordId, ext);
            try {
                minioClient.statObject(
                    StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
                );
                return ext.substring(1); // Remove o ponto
            } catch (Exception e) {
                continue;
            }
        }
        return "jpg"; // Default
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
     * Obtém a extensão do arquivo
     */
    private String getFileExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return ".jpg"; // Default
    }
}


package utfpr.OD46S.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import utfpr.OD46S.backend.dtos.GPSRecordDTO;
import utfpr.OD46S.backend.entitys.GPSRecord;
import utfpr.OD46S.backend.entitys.RouteExecution;
import utfpr.OD46S.backend.enums.ExecutionStatus;
import utfpr.OD46S.backend.repositorys.GPSRecordRepository;
import utfpr.OD46S.backend.repositorys.RouteExecutionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GPSTrackingService {

    @Autowired
    private GPSRecordRepository gpsRecordRepository;

    @Autowired
    private RouteExecutionRepository executionRepository;

    @Autowired
    private MinioStorageService minioStorageService;

    @Transactional
    public Map<String, Object> registrarPosicaoGPS(Long executionId, Map<String, Object> request, MultipartFile photo) {
        // Verificar se execution existe e está em progresso
        RouteExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found"));

        if (execution.getStatus() != ExecutionStatus.IN_PROGRESS) {
            throw new RuntimeException("Cannot register GPS for execution that is not in progress");
        }

        // Extrair dados do request
        BigDecimal latitude = getBigDecimalFromMap(request, "latitude");
        BigDecimal longitude = getBigDecimalFromMap(request, "longitude");

        if (latitude == null || longitude == null) {
            throw new RuntimeException("Latitude and longitude are required");
        }

        // Validar latitude e longitude
        if (latitude.compareTo(new BigDecimal("-90")) < 0 || latitude.compareTo(new BigDecimal("90")) > 0) {
            throw new RuntimeException("Latitude must be between -90 and 90");
        }
        if (longitude.compareTo(new BigDecimal("-180")) < 0 || longitude.compareTo(new BigDecimal("180")) > 0) {
            throw new RuntimeException("Longitude must be between -180 and 180");
        }

        // Criar GPS record
        GPSRecord gpsRecord = new GPSRecord(execution, latitude, longitude);

        // Campos opcionais
        if (request.containsKey("speed_kmh")) {
            gpsRecord.setSpeedKmh(getBigDecimalFromMap(request, "speed_kmh"));
        }

        if (request.containsKey("heading_degrees")) {
            gpsRecord.setHeadingDegrees(getIntegerFromMap(request, "heading_degrees"));
        }

        if (request.containsKey("accuracy_meters")) {
            gpsRecord.setAccuracyMeters(getBigDecimalFromMap(request, "accuracy_meters"));
        }

        if (request.containsKey("event_type")) {
            String eventType = (String) request.get("event_type");
            if (eventType != null && !eventType.isEmpty()) {
                gpsRecord.setEventType(eventType);
                // Se não for NORMAL/START/END, provavelmente é manual
                if (!"NORMAL".equals(eventType) && !"START".equals(eventType) && !"END".equals(eventType)) {
                    gpsRecord.setIsAutomatic(false);
                }
            }
        }
        
        // Permite sobrescrever is_automatic se fornecido
        if (request.containsKey("is_automatic")) {
            Object isAutoObj = request.get("is_automatic");
            if (isAutoObj != null) {
                gpsRecord.setIsAutomatic(isAutoObj instanceof Boolean ? (Boolean) isAutoObj : Boolean.parseBoolean(isAutoObj.toString()));
            }
        }
        
        // Processa is_offline (indica sincronização offline)
        if (request.containsKey("is_offline")) {
            Object isOfflineObj = request.get("is_offline");
            if (isOfflineObj != null) {
                gpsRecord.setIsOffline(isOfflineObj instanceof Boolean ? (Boolean) isOfflineObj : Boolean.parseBoolean(isOfflineObj.toString()));
            }
        }
        
        // Processa gps_timestamp customizado (para registros offline)
        if (request.containsKey("gps_timestamp")) {
            Object timestampObj = request.get("gps_timestamp");
            if (timestampObj != null) {
                if (timestampObj instanceof LocalDateTime) {
                    gpsRecord.setGpsTimestamp((LocalDateTime) timestampObj);
                } else if (timestampObj instanceof String) {
                    try {
                        gpsRecord.setGpsTimestamp(LocalDateTime.parse((String) timestampObj));
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid gps_timestamp format. Use ISO-8601: yyyy-MM-ddTHH:mm:ss");
                    }
                }
                // Se forneceu gps_timestamp customizado, provavelmente é offline
                if (!request.containsKey("is_offline")) {
                    gpsRecord.setIsOffline(true);
                }
            }
        }

        if (request.containsKey("description")) {
            String description = (String) request.get("description");
            if (description != null && !description.trim().isEmpty()) {
                gpsRecord.setDescription(description);
            }
        }

        // Campos de coleta (opcionais)
        if (request.containsKey("point_id")) {
            Object pointIdObj = request.get("point_id");
            if (pointIdObj != null) {
                gpsRecord.setPointId(pointIdObj instanceof Long ? (Long) pointIdObj : Long.parseLong(pointIdObj.toString()));
            }
        }
        
        if (request.containsKey("collected_weight_kg")) {
            BigDecimal weightKg = getBigDecimalFromMap(request, "collected_weight_kg");
            if (weightKg != null) {
                gpsRecord.setCollectedWeightKg(weightKg);
            }
        }
        
        if (request.containsKey("point_condition")) {
            String pointCondition = (String) request.get("point_condition");
            if (pointCondition != null && !pointCondition.trim().isEmpty()) {
                gpsRecord.setPointCondition(pointCondition);
            }
        }

        // Salvar registro primeiro para obter o ID
        gpsRecordRepository.save(gpsRecord);

        // Upload de foto após salvar o registro (para usar o ID do registro)
        if (photo != null && !photo.isEmpty()) {
            try {
                String photoUrl = minioStorageService.storeGPSPhoto(executionId, gpsRecord.getId(), photo);
                gpsRecord.setPhotoUrl(photoUrl);
                // Atualizar registro com a photo_url
                gpsRecordRepository.save(gpsRecord);
            } catch (Exception e) {
                // Se falhar o upload, continua sem a foto mas loga o erro
                // Em produção, considere fazer rollback ou tratar de forma diferente
                throw new RuntimeException("Failed to upload photo: " + e.getMessage(), e);
            }
        } else if (request.containsKey("photo_url")) {
            // Se já veio com photo_url (caso de sincronização offline)
            String photoUrl = (String) request.get("photo_url");
            if (photoUrl != null && !photoUrl.trim().isEmpty()) {
                gpsRecord.setPhotoUrl(photoUrl);
                gpsRecordRepository.save(gpsRecord);
            }
        }

        GPSRecordDTO dto = toDTO(gpsRecord);

        Map<String, Object> data = new HashMap<>();
        data.put("gps_record", dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "GPS position registered successfully");

        return response;
    }

    @Transactional
    public Map<String, Object> registrarMultiplasPosicoes(Long executionId, List<Map<String, Object>> posicoes) {
        // Verificar se execution existe e está em progresso
        RouteExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found"));

        if (execution.getStatus() != ExecutionStatus.IN_PROGRESS) {
            throw new RuntimeException("Cannot register GPS for execution that is not in progress");
        }

        List<GPSRecord> records = posicoes.stream().map(request -> {
            BigDecimal latitude = getBigDecimalFromMap(request, "latitude");
            BigDecimal longitude = getBigDecimalFromMap(request, "longitude");

            if (latitude == null || longitude == null) {
                throw new RuntimeException("Latitude and longitude are required for all positions");
            }

            GPSRecord gpsRecord = new GPSRecord(execution, latitude, longitude);

            // Processa gps_timestamp (pode ser LocalDateTime ou String)
            if (request.containsKey("gps_timestamp") && request.get("gps_timestamp") != null) {
                Object timestampObj = request.get("gps_timestamp");
                if (timestampObj instanceof LocalDateTime) {
                    gpsRecord.setGpsTimestamp((LocalDateTime) timestampObj);
                } else if (timestampObj instanceof String) {
                    try {
                        gpsRecord.setGpsTimestamp(LocalDateTime.parse((String) timestampObj));
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid gps_timestamp format. Use ISO-8601: yyyy-MM-ddTHH:mm:ss");
                    }
                }
            }

            if (request.containsKey("speed_kmh")) {
                gpsRecord.setSpeedKmh(getBigDecimalFromMap(request, "speed_kmh"));
            }

            if (request.containsKey("heading_degrees")) {
                gpsRecord.setHeadingDegrees(getIntegerFromMap(request, "heading_degrees"));
            }

            if (request.containsKey("accuracy_meters")) {
                gpsRecord.setAccuracyMeters(getBigDecimalFromMap(request, "accuracy_meters"));
            }

            if (request.containsKey("event_type")) {
                String eventType = (String) request.get("event_type");
                if (eventType != null && !eventType.isEmpty()) {
                    gpsRecord.setEventType(eventType);
                }
            }

            return gpsRecord;
        }).collect(Collectors.toList());

        gpsRecordRepository.saveAll(records);

        Map<String, Object> data = new HashMap<>();
        data.put("records_saved", records.size());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", records.size() + " GPS positions registered successfully");

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterRastroGPS(Long executionId, LocalDateTime startTime, LocalDateTime endTime) {
        // Verificar se execution existe
        RouteExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found"));

        List<GPSRecord> records;
        if (startTime != null && endTime != null) {
            records = gpsRecordRepository.findByExecutionIdAndTimestampBetween(executionId, startTime, endTime);
        } else {
            records = gpsRecordRepository.findByExecutionIdOrderByTimestamp(executionId);
        }

        List<GPSRecordDTO> dtos = records.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        // Calcular estatísticas
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total_points", dtos.size());
        
        if (!dtos.isEmpty()) {
            statistics.put("first_timestamp", dtos.get(0).getGpsTimestamp());
            statistics.put("last_timestamp", dtos.get(dtos.size() - 1).getGpsTimestamp());
            
            // Calcular distância total aproximada (simplificada)
            double totalDistance = 0;
            for (int i = 1; i < dtos.size(); i++) {
                totalDistance += calculateDistance(
                    dtos.get(i-1).getLatitude().doubleValue(),
                    dtos.get(i-1).getLongitude().doubleValue(),
                    dtos.get(i).getLatitude().doubleValue(),
                    dtos.get(i).getLongitude().doubleValue()
                );
            }
            statistics.put("total_distance_km", Math.round(totalDistance * 100.0) / 100.0);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("execution_id", executionId);
        data.put("gps_track", dtos);
        data.put("statistics", statistics);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    // Helper methods
    private GPSRecordDTO toDTO(GPSRecord record) {
        GPSRecordDTO dto = new GPSRecordDTO();
        dto.setId(record.getId());
        dto.setExecutionId(record.getExecution().getId());
        dto.setGpsTimestamp(record.getGpsTimestamp());
        dto.setLatitude(record.getLatitude());
        dto.setLongitude(record.getLongitude());
        dto.setSpeedKmh(record.getSpeedKmh());
        dto.setHeadingDegrees(record.getHeadingDegrees());
        dto.setAccuracyMeters(record.getAccuracyMeters());
        dto.setEventType(record.getEventType());
        dto.setIsAutomatic(record.getIsAutomatic());
        dto.setIsOffline(record.getIsOffline());
        dto.setSyncDelaySeconds(record.getSyncDelaySeconds());
        dto.setDescription(record.getDescription());
        dto.setPhotoUrl(record.getPhotoUrl());
        dto.setPointId(record.getPointId());
        dto.setCollectedWeightKg(record.getCollectedWeightKg());
        dto.setPointCondition(record.getPointCondition());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }

    private BigDecimal getBigDecimalFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        return null;
    }

    private Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        return null;
    }

    /**
     * Registra múltiplos pontos GPS de uma vez (batch/lote)
     * Usado para sincronização offline
     */
    public Map<String, Object> registrarGPSBatch(Long executionId, List<Map<String, Object>> records) {
        int successCount = 0;
        int errorCount = 0;
        List<Map<String, Object>> errors = new java.util.ArrayList<>();
        List<GPSRecordDTO> savedRecords = new java.util.ArrayList<>();
        
        for (int i = 0; i < records.size(); i++) {
            try {
                Map<String, Object> record = records.get(i);
                // Batch não suporta upload de fotos (fotos devem ser enviadas individualmente)
                Map<String, Object> result = registrarPosicaoGPS(executionId, record, null);
                
                if (result.get("success") == Boolean.TRUE && result.containsKey("data")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) result.get("data");
                    if (data.containsKey("gps_record")) {
                        savedRecords.add((GPSRecordDTO) data.get("gps_record"));
                    }
                }
                
                successCount++;
            } catch (Exception e) {
                errorCount++;
                errors.add(Map.of(
                    "index", i,
                    "error", e.getMessage()
                ));
            }
        }
        
        return Map.of(
            "success", errorCount == 0,
            "data", Map.of(
                "total_records", records.size(),
                "success_count", successCount,
                "error_count", errorCount,
                "errors", errors,
                "saved_records", savedRecords
            )
        );
    }
    
    // Cálculo de distância usando fórmula de Haversine (aproximada)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raio da Terra em km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}


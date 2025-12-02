package utfpr.OD46S.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public Map<String, Object> registrarPosicaoGPS(Long executionId, Map<String, Object> request) {
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
        if (request.containsKey("gps_timestamp") && request.get("gps_timestamp") != null) {
            gpsRecord.setGpsTimestamp(LocalDateTime.parse((String) request.get("gps_timestamp")));
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

        if (request.containsKey("description")) {
            String description = (String) request.get("description");
            if (description != null && !description.trim().isEmpty()) {
                gpsRecord.setDescription(description);
            }
        }

        if (request.containsKey("photo_url")) {
            String photoUrl = (String) request.get("photo_url");
            if (photoUrl != null && !photoUrl.trim().isEmpty()) {
                gpsRecord.setPhotoUrl(photoUrl);
            }
        }

        gpsRecordRepository.save(gpsRecord);

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

            if (request.containsKey("gps_timestamp") && request.get("gps_timestamp") != null) {
                gpsRecord.setGpsTimestamp(LocalDateTime.parse((String) request.get("gps_timestamp")));
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
        dto.setDescription(record.getDescription());
        dto.setPhotoUrl(record.getPhotoUrl());
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


package utfpr.OD46S.backend.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GPSRecordDTO {

    private Long id;
    private Long executionId;
    private LocalDateTime gpsTimestamp;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal speedKmh;
    private Integer headingDegrees;
    private BigDecimal accuracyMeters;
    private String eventType;
    private Boolean isAutomatic;  // true = GPS automático, false = evento manual
    private Boolean isOffline;    // true = registrado offline e sincronizado, false = tempo real
    private String description;
    private String photoUrl;
    private Long syncDelaySeconds; // Atraso de sincronização em segundos (calculado)
    
    // Campos opcionais para eventos de coleta
    private Long pointId;
    private BigDecimal collectedWeightKg;
    private String pointCondition;
    
    private LocalDateTime createdAt;

    // Constructors
    public GPSRecordDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public void setExecutionId(Long executionId) {
        this.executionId = executionId;
    }

    public LocalDateTime getGpsTimestamp() {
        return gpsTimestamp;
    }

    public void setGpsTimestamp(LocalDateTime gpsTimestamp) {
        this.gpsTimestamp = gpsTimestamp;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getSpeedKmh() {
        return speedKmh;
    }

    public void setSpeedKmh(BigDecimal speedKmh) {
        this.speedKmh = speedKmh;
    }

    public Integer getHeadingDegrees() {
        return headingDegrees;
    }

    public void setHeadingDegrees(Integer headingDegrees) {
        this.headingDegrees = headingDegrees;
    }

    public BigDecimal getAccuracyMeters() {
        return accuracyMeters;
    }

    public void setAccuracyMeters(BigDecimal accuracyMeters) {
        this.accuracyMeters = accuracyMeters;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Boolean getIsAutomatic() {
        return isAutomatic;
    }

    public void setIsAutomatic(Boolean isAutomatic) {
        this.isAutomatic = isAutomatic;
    }

    public Boolean getIsOffline() {
        return isOffline;
    }

    public void setIsOffline(Boolean isOffline) {
        this.isOffline = isOffline;
    }

    public Long getSyncDelaySeconds() {
        return syncDelaySeconds;
    }

    public void setSyncDelaySeconds(Long syncDelaySeconds) {
        this.syncDelaySeconds = syncDelaySeconds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Long getPointId() {
        return pointId;
    }

    public void setPointId(Long pointId) {
        this.pointId = pointId;
    }

    public BigDecimal getCollectedWeightKg() {
        return collectedWeightKg;
    }

    public void setCollectedWeightKg(BigDecimal collectedWeightKg) {
        this.collectedWeightKg = collectedWeightKg;
    }

    public String getPointCondition() {
        return pointCondition;
    }

    public void setPointCondition(String pointCondition) {
        this.pointCondition = pointCondition;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


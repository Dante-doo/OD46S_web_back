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
    private String description;
    private String photoUrl;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


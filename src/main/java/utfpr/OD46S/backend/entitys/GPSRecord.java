package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "gps_records")
public class GPSRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    private RouteExecution execution;

    @Column(name = "gps_timestamp", nullable = false)
    private LocalDateTime gpsTimestamp;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "speed_kmh", precision = 5, scale = 2)
    private BigDecimal speedKmh;

    @Column(name = "heading_degrees")
    private Integer headingDegrees;

    @Column(name = "accuracy_meters", precision = 5, scale = 2)
    private BigDecimal accuracyMeters;

    @Column(name = "event_type", length = 20)
    private String eventType = "NORMAL";
    
    // Indica se é GPS automático (true) ou evento manual do motorista (false)
    @Column(name = "is_automatic")
    private Boolean isAutomatic = true;  // Default: GPS automático
    
    // Indica se foi registrado offline e sincronizado depois (true) ou em tempo real (false)
    @Column(name = "is_offline")
    private Boolean isOffline = false;  // Default: online/tempo real

    // Descrição adicional para eventos especiais (paradas, problemas, etc)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // URL da foto (opcional) - armazenada no MinIO
    @Column(name = "photo_url", length = 500)
    private String photoUrl;
    
    // Campos opcionais para eventos de COLETA em pontos
    @Column(name = "point_id")
    private Long pointId;  // ID do ponto de coleta visitado (se aplicável)
    
    @Column(name = "collected_weight_kg", precision = 8, scale = 2)
    private BigDecimal collectedWeightKg;  // Peso coletado (se aplicável)
    
    @Column(name = "point_condition", length = 30)
    private String pointCondition;  // NORMAL, SATURATED, DAMAGED, INACCESSIBLE

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (gpsTimestamp == null) {
            gpsTimestamp = LocalDateTime.now();
        }
    }

    // Constructors
    public GPSRecord() {
    }

    public GPSRecord(RouteExecution execution, BigDecimal latitude, BigDecimal longitude) {
        this.execution = execution;
        this.latitude = latitude;
        this.longitude = longitude;
        this.gpsTimestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RouteExecution getExecution() {
        return execution;
    }

    public void setExecution(RouteExecution execution) {
        this.execution = execution;
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
        if (headingDegrees != null && (headingDegrees < 0 || headingDegrees > 359)) {
            throw new IllegalArgumentException("Heading degrees must be between 0 and 359");
        }
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
    
    // Método auxiliar: calcula atraso de sincronização em segundos
    public Long getSyncDelaySeconds() {
        if (gpsTimestamp == null || createdAt == null) {
            return 0L;
        }
        return ChronoUnit.SECONDS.between(gpsTimestamp, createdAt);
    }
}


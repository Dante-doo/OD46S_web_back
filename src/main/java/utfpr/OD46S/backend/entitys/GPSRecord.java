package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    // Descrição adicional para eventos especiais (paradas, problemas, etc)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // URL da foto (opcional) - armazenada no MinIO
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

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


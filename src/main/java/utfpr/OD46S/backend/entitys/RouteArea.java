package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_areas")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RouteArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "external_name", nullable = false, length = 255)
    private String externalName;

    @Column(name = "waste_type", nullable = false, length = 50)
    private String wasteType;

    @Column(name = "geometry_geojson", nullable = false, columnDefinition = "JSONB")
    private String geometryGeojson; // Stored as JSON string, will be parsed when needed

    @Column(name = "stroke_color", length = 9)
    private String strokeColor = "#000000";

    @Column(name = "fill_color", length = 9)
    private String fillColor = "#000000";

    @Column(name = "fill_opacity", nullable = false, precision = 3, scale = 2)
    private BigDecimal fillOpacity = new BigDecimal("0.40");

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


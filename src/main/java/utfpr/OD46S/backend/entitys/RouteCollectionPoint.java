package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utfpr.OD46S.backend.enums.CollectionFrequency;
import utfpr.OD46S.backend.enums.WasteType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_collection_points", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"route_id", "sequence_order"})
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RouteCollectionPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "waste_type", nullable = false, length = 50)
    private WasteType wasteType;

    @Column(name = "estimated_capacity_kg", precision = 8, scale = 2)
    private BigDecimal estimatedCapacityKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_frequency", length = 20)
    private CollectionFrequency collectionFrequency;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}


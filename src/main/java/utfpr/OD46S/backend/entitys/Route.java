package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utfpr.OD46S.backend.enums.CollectionType;
import utfpr.OD46S.backend.enums.Priority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_type", nullable = false, length = 20)
    private CollectionType collectionType;

    @Column(name = "periodicity", nullable = false, length = 50)
    private String periodicity;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "estimated_time_minutes")
    private Integer estimatedTimeMinutes;

    @Column(name = "distance_km", precision = 8, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC")
    private List<RouteCollectionPoint> collectionPoints = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addCollectionPoint(RouteCollectionPoint point) {
        collectionPoints.add(point);
        point.setRoute(this);
    }

    public void removeCollectionPoint(RouteCollectionPoint point) {
        collectionPoints.remove(point);
        point.setRoute(null);
    }
}


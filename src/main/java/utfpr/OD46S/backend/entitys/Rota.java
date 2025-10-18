package utfpr.OD46S.backend.entitys;


import jakarta.persistence.*;
import lombok.*;
import utfpr.OD46S.backend.enums.RotaStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Rota {

    @Id
    @Column(name = "id")
    private Long id;


    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RotaStatus status;

    @Column(nullable = false)
    private Integer estimatedDuration;

    @Column(nullable = false)
    private Double estimatedDistance;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sequence ASC")
    private List<RotaPonto> collectionPoints = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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

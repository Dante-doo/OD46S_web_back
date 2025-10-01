package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utfpr.OD46S.backend.enums.StatusVeiculo;

@Entity
@Table(name = "vehicles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "license_plate", nullable = false, unique = true, length = 7)
    private String licensePlate;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "capacity_kg", nullable = false)
    private java.math.BigDecimal capacityKg;

    @Column(name = "fuel_type", nullable = false, length = 20)
    private String fuelType;

    @Column(name = "average_consumption")
    private java.math.BigDecimal averageConsumption;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusVeiculo status = StatusVeiculo.AVAILABLE;

    @Column(name = "current_km")
    private Integer currentKm = 0;

    @Column(name = "acquisition_date")
    private java.time.LocalDate acquisitionDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", updatable = false, insertable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { }

    @PreUpdate
    protected void onUpdate() { }
}



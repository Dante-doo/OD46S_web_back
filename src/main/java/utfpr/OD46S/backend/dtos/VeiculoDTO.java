package utfpr.OD46S.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import utfpr.OD46S.backend.enums.StatusVeiculo;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoDTO {
    private Long id;
    private String licensePlate;
    private String model;
    private String brand;
    private Integer year;
    private BigDecimal capacityKg;
    private String fuelType;
    private BigDecimal averageConsumption;
    private StatusVeiculo status;
    private Integer currentKm;
    private LocalDate acquisitionDate;
    private String notes;
    private Boolean active;
}



package utfpr.OD46S.backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import utfpr.OD46S.backend.entitys.Rota;
import utfpr.OD46S.backend.enums.RotaStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RotaDTO {

    @JsonProperty("id")
    private Long id;

    private String name;
    private String description;
    private RotaStatus status;
    private Integer estimatedDuration;
    private Double estimatedDistance;
    private List<RotaPontoDTO> collectionPoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private RotaDTO fromEntity(Rota rota) {
        return RotaDTO.builder()
                .id(rota.getId())
                .status(rota.getStatus())
                .build();
    }

    public Rota toEntity() {
        return Rota.builder()
                .id(this.id)
                .build();
    }
}

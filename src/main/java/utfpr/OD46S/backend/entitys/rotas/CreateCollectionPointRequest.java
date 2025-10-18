package utfpr.OD46S.backend.entitys.rotas;

import lombok.*;
import utfpr.OD46S.backend.entitys.RotaPonto;
import utfpr.OD46S.backend.enums.PontoStatus;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCollectionPointRequest {
    private String address;
    private Double latitude;
    private Double longitude;
    private String neighborhood;
    private String observations;

    public RotaPonto toEntity(Integer sequence) {
        return RotaPonto.builder()
                .address(this.address)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .sequence(sequence)
                .neighborhood(this.neighborhood)
                .observations(this.observations)
                .status(PontoStatus.PENDING)
                .build();
    }
}

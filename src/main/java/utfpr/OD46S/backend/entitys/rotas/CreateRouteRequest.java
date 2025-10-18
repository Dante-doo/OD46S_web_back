package utfpr.OD46S.backend.entitys.rotas;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import utfpr.OD46S.backend.entitys.Rota;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRouteRequest {

    private String name;
    private String description;
    private Integer estimatedDuration;
    private Double estimatedDistance;

    public Rota toEntity() {
        return Rota.builder()
                .name(this.name)
                .description(this.description)
                .estimatedDuration(this.estimatedDuration)
                .estimatedDistance(this.estimatedDistance)
                .build();
    }
}

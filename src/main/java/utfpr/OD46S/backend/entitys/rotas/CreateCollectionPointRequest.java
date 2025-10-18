package utfpr.OD46S.backend.entitys.rotas;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCollectionPointRequest {
    private String address;
    private Double latitude;
    private Double longitude;
    private String neighborhood;
    private String observations;
}

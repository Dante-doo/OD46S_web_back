package utfpr.OD46S.backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import utfpr.OD46S.backend.entitys.Motorista;
import utfpr.OD46S.backend.enums.CategoriaCNH;
import utfpr.OD46S.backend.enums.StatusMotorista;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MotoristaDTO {

    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String senha;

    @JsonProperty("cnh")
    private String cnh;

    @JsonProperty("categoria_cnh")
    private CategoriaCNH categoriaCnh;

    @JsonProperty("status")
    private StatusMotorista status;

    public Motorista toEntity() {
        Motorista motorista = new Motorista();
        motorista.setId(this.id);
        motorista.setLicenseNumber(this.cnh);
        motorista.setLicenseCategory(this.categoriaCnh);
        return motorista;
    }

    public static MotoristaDTO fromEntity(Motorista entity) {
        MotoristaDTO dto = new MotoristaDTO();
        dto.setId(entity.getId());
        dto.setCnh(entity.getLicenseNumber());
        dto.setCategoriaCnh(entity.getLicenseCategory());
        return dto;
    }
}
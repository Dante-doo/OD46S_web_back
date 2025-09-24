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
@SuperBuilder
public class MotoristaDTO extends UsuarioDTO {

    @JsonProperty("cnh")
    private String cnh;

    @JsonProperty("categoria_cnh")
    private CategoriaCNH categoriaCnh;

    @JsonProperty("status")
    private StatusMotorista status;

    public Motorista toEntity() {
        return Motorista.builder()
                .id(super.getId())
                .nome(super.getNome())
                .senha(super.getSenha())
                .cnh(this.cnh)
                .categoriaCnh(this.categoriaCnh)
                .status(this.status)
                .build();
    }

    public static MotoristaDTO fromEntity(Motorista entity) {
        return MotoristaDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .senha(entity.getSenha())
                .cnh(entity.getCnh())
                .categoriaCnh(entity.getCategoriaCnh())
                .status(entity.getStatus())
                .build();
    }
}
package utfpr.OD46S.backend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import utfpr.OD46S.backend.entitys.Administrator;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
public class AdministradorDTO extends UsuarioDTO {

    @JsonProperty("nivel_acesso")
    private String nivelAcesso;

    public Administrator toEntity() {
        return Administrator.builder()
                .id(super.getId())
                .nome(super.getNome())
                .senha(super.getSenha())
                .cpf(super.getCpf())
                .email(super.getEmail())
                .nivelAcesso(this.nivelAcesso)
                .build();
    }

    public static AdministradorDTO fromEntity(Administrator entity) {
        return AdministradorDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .senha(entity.getSenha())
                .cpf(entity.getCpf())
                .nivelAcesso(entity.getNivelAcesso())
                .build();
    }
}
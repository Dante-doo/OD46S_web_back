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
public class AdministradorDTO {

    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String senha;

    @JsonProperty("nivel_acesso")
    private String nivelAcesso;

    public Administrator toEntity() {
        Administrator admin = new Administrator();
        admin.setId(this.id);
        admin.setAccessLevel(this.nivelAcesso);
        return admin;
    }

    public static AdministradorDTO fromEntity(Administrator entity) {
        AdministradorDTO dto = new AdministradorDTO();
        dto.setId(entity.getId());
        dto.setNivelAcesso(entity.getNivelAcesso());
        return dto;
    }
}
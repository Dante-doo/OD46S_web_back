package utfpr.OD46S.backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import utfpr.OD46S.backend.entitys.Usuario;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    @JsonProperty("id")

    private Long id;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("senha")
    private String senha;

    public Usuario toEntity() {
        return Usuario.builder()
                .nome(this.nome)
                .senha(this.senha)
                .build();
    }

    public static UsuarioDTO fromEntity(Usuario entity) {
        return UsuarioDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .senha(entity.getSenha())
                .build();
    }



}

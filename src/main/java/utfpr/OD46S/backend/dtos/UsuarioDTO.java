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

    @JsonProperty("email")
    private String email;

    @JsonProperty("cpf")
    private String cpf;

    public Usuario toEntity() {
        return Usuario.builder()
                .name(this.nome)
                .password(this.senha)
                .email(this.email)
                .cpf(this.cpf)
                .build();
    }

    public static UsuarioDTO fromEntity(Usuario entity) {
        return UsuarioDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .senha(entity.getSenha())
                .email(entity.getEmail())
                .cpf(entity.getCpf())
                .build();
    }



}

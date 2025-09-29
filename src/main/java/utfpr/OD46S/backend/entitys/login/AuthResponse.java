package utfpr.OD46S.backend.entitys.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Resposta de autenticação com token JWT e informações do usuário")
@AllArgsConstructor
@Getter
@Setter
public class AuthResponse {

    @Schema(description = "Token de autenticação JWT", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;
    
    @Schema(description = "Endereço de email do usuário", example = "admin@od46s.com")
    private String email;
    
    @Schema(description = "Nome completo do usuário", example = "Administrador do Sistema")
    private String name;
    
    @Schema(description = "Tipo/papel do usuário", example = "ADMIN", allowableValues = {"USER", "ADMIN", "DRIVER"})
    private String type;

    // Constructor legacy (3 parâmetros)
    public AuthResponse(String token, String email, String name) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.type = "USER";
    }
}

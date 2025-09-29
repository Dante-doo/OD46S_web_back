package utfpr.OD46S.backend.entitys.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Dados de requisição de login")
@Getter
@Setter
public class LoginRequest {

    @Schema(description = "Endereço de email do usuário", example = "admin@od46s.com")
    private String email;
    
    @Schema(description = "CPF do usuário (documento brasileiro)", example = "11111111111")
    private String cpf;
    
    @Schema(description = "Senha do usuário", example = "admin123")
    private String password;
    
    // Getter e setter para compatibilidade
    public String getSenha() { return password; }
    public void setSenha(String senha) { this.password = senha; }
}

package utfpr.OD46S.backend.entitys.login;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    private String email;
    private String cpf;
    private String password;
    
    // Getter e setter para compatibilidade
    public String getSenha() { return password; }
    public void setSenha(String senha) { this.password = senha; }
}

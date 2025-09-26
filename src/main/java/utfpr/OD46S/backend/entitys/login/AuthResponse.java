package utfpr.OD46S.backend.entitys.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AuthResponse {

    private String token;
    private String email;
    private String name;
    private String type;

    // Constructor legacy (3 parâmetros)
    public AuthResponse(String token, String email, String name) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.type = "USER";
    }
}

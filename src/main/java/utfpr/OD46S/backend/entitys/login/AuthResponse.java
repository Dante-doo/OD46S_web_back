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


}

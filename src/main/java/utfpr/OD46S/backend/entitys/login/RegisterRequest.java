package utfpr.OD46S.backend.entitys.login;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String email;
    private String password;
    private String name;
}

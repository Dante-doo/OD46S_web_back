package utfpr.OD46S.backend.entitys.login;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String name;
    private String email;
    private String cpf;
    private String password;
    private String type;

    // Campos específicos para DRIVER
    private String licenseNumber;
    private String licenseCategory;
    private String licenseExpiry;
    private String phone;

    // Campos específicos para ADMIN
    private String accessLevel;
    private String department;
    private String corporatePhone;
}

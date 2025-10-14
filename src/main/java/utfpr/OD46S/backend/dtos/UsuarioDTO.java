package utfpr.OD46S.backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import utfpr.OD46S.backend.entitys.Usuario;

import java.time.LocalDateTime;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    private String name;

    @JsonProperty("email")
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String email;

    @JsonProperty("cpf")
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos")
    private String cpf;

    @JsonProperty("password")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String password;

    @JsonProperty("type")
    private String type; // ADMIN | DRIVER

    @JsonProperty("active")
    @Builder.Default
    private Boolean active = true;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // Campos específicos para ADMIN
    @JsonProperty("access_level")
    private String accessLevel;

    @JsonProperty("department")
    private String department;

    @JsonProperty("corporate_phone")
    private String corporatePhone;

    // Campos específicos para DRIVER
    @JsonProperty("license_number")
    private String licenseNumber;

    @JsonProperty("license_category")
    private String licenseCategory;

    @JsonProperty("license_expiry")
    private String licenseExpiry;

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("phone")
    private String phone;

    public Usuario toEntity() {
        return Usuario.builder()
                .name(this.name)
                .password(this.password)
                .email(this.email)
                .cpf(this.cpf)
                .active(this.active)
                .build();
    }

    public static UsuarioDTO fromEntity(Usuario entity) {
        return UsuarioDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .cpf(maskCpf(entity.getCpf()))
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }
}

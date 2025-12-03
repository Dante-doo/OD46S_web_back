package utfpr.OD46S.backend.entitys.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Resposta de autenticação com token JWT e informações do usuário")
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
    
    @Schema(description = "ID do usuário", example = "1")
    private Long userId;
    
    @Schema(description = "ID do motorista (se for DRIVER)", example = "1")
    private Long driverId;
    
    @Schema(description = "ID do administrador (se for ADMIN)", example = "1")
    private Long adminId;

    // Constructor legacy (3 parâmetros) - mantido para compatibilidade
    public AuthResponse(String token, String email, String name) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.type = "USER";
        this.userId = null;
        this.driverId = null;
        this.adminId = null;
    }
    
    // Constructor completo
    public AuthResponse(String token, String email, String name, String type, Long userId, Long driverId, Long adminId) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.type = type;
        this.userId = userId;
        this.driverId = driverId;
        this.adminId = adminId;
    }
}

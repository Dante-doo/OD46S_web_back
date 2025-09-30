package utfpr.OD46S.backend.entitys.login;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequest {
    
    private String token;
    
    public RefreshRequest() {}
    
    public RefreshRequest(String token) {
        this.token = token;
    }
}

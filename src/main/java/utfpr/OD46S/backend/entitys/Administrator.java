package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administrators")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Administrator {

    @Id
    @Column(name = "id")
    private Long id;

    // Relacionamento removido temporariamente para resolver erro de transação

    @Column(name = "access_level", nullable = false)
    private String accessLevel = "ADMIN";
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "corporate_phone")
    private String corporatePhone;
    
    // Getter e setter para compatibilidade
    public String getNivelAcesso() { return accessLevel; }
    public void setNivelAcesso(String nivelAcesso) { this.accessLevel = nivelAcesso; }
}
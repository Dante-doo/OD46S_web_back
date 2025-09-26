package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utfpr.OD46S.backend.enums.CategoriaCNH;
import utfpr.OD46S.backend.enums.StatusMotorista;

@Entity
@Table(name = "drivers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Motorista {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_category", nullable = false)
    private CategoriaCNH licenseCategory;
    
    @Column(name = "license_expiry", nullable = false)
    private java.time.LocalDate licenseExpiry;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "phone")
    private String phone;

    // Getters e setters para compatibilidade
    public String getCnh() { return licenseNumber; }
    public void setCnh(String cnh) { this.licenseNumber = cnh; }
    
    public CategoriaCNH getCategoriaCnh() { return licenseCategory; }
    public void setCategoriaCnh(CategoriaCNH categoriaCnh) { this.licenseCategory = categoriaCnh; }
}
package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import utfpr.OD46S.backend.enums.CategoriaCNH;
import utfpr.OD46S.backend.enums.StatusMotorista;

@Entity
@Table(name = "drivers")
public class Motorista {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_category")
    private CategoriaCNH licenseCategory;
    
    @Column(name = "license_expiry")
    private java.time.LocalDate licenseExpiry;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "phone")
    private String phone;

    // Constructors
    public Motorista() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public CategoriaCNH getLicenseCategory() {
        return licenseCategory;
    }

    public void setLicenseCategory(CategoriaCNH licenseCategory) {
        this.licenseCategory = licenseCategory;
    }

    public java.time.LocalDate getLicenseExpiry() {
        return licenseExpiry;
    }

    public void setLicenseExpiry(java.time.LocalDate licenseExpiry) {
        this.licenseExpiry = licenseExpiry;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
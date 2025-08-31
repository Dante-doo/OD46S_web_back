package utfpr.OD46S.backend.entitys;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import utfpr.OD46S.backend.enums.CategoriaCNH;

@Entity
@Table(name = "motoristas")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Motorista extends Usuario {

    @Column(name = "cnh", nullable = false, unique = true)
    private String cnh;

    @Column(name = "categoria_cnh", nullable = false)
    private CategoriaCNH categoriaCnh;
}
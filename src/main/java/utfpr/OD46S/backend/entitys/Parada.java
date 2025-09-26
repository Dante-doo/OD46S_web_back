package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import utfpr.OD46S.backend.enums.StatusParada;

import java.time.LocalDateTime;

@Entity
@Table(name = "paradas")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Parada {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Rota route;

    @Column(name = "sequencia")
    private Integer sequencia;

    @Column(name = "endereco")
    private String endereco;

    @Column(name = "latitude")
    private Double lat;

    @Column(name = "longitude")
    private Double lng;


    private LocalDateTime arrivalWindowStart;

    private LocalDateTime arrivalWindowEnd;

    private Integer serviceTimeSeconds;

    @Column(name = "hora_chegada_estimada")
    private LocalDateTime horaChegadaEstimada;

    @Column(name = "horaChegada")
    private LocalDateTime horaChegada;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StatusParada status;

    @Column(name = "notas")
    private String notas;
}

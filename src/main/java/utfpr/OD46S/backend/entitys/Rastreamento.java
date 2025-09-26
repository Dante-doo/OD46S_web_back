package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import utfpr.OD46S.backend.enums.RastreamentoStatus;

import java.time.LocalDateTime;

public class Rastreamento {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rota_id")
    private Rota route;

    @ManyToOne
    @JoinColumn(name = "motorista_id")
    private Motorista driver;

    private LocalDateTime assignedAt;

    private LocalDateTime acceptedAt;

    private Boolean accepted;

    @Column(name = "inicio_em")
    private LocalDateTime inicioEm;

    @Column(name = "termino_em")
    private LocalDateTime terminoEm;

    @Enumerated(EnumType.STRING)
    private RastreamentoStatus status;

    private String telemetryChannel;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name= "atualizado_em")
    private LocalDateTime atualizadoEm;
}

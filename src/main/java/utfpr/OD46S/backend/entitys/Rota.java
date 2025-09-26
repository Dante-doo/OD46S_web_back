package utfpr.OD46S.backend.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import utfpr.OD46S.backend.enums.StatusRota;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "rotas")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Rota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "criado_em")
    private Date criado_em;

    @Column(name = "agendada_para")
    private LocalDateTime agendada_para;

    @Column(name = "status")
    private StatusRota statusRota;

    @Column(name = "distancia_total_M")
    private Integer distanciaTotalM;

    @Column(name = "duracao_estimada_s")
    private Integer duracaoEstimadaS;

    @Column(length = 2000)
    private String polyline;

    @Column(columnDefinition = "jsonb")
    private String limites;


    private Boolean optimizedSequence;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    private LocalDateTime atualizadaEm;

    @OneToMany(mappedBy = "rota", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<Parada> paradas;

}

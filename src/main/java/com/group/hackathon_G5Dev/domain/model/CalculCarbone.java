package com.group.hackathon_G5Dev.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "calcul_carbone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculCarbone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "date_calcul", nullable = false)
    private OffsetDateTime dateCalcul;

    @Column(name = "co2_construction", nullable = false)
    private Double co2Construction;

    @Column(name = "co2_exploitation", nullable = false)
    private Double co2Exploitation;

    @Column(name = "co2_total", nullable = false)
    private Double co2Total;

    @Column(name = "co2_par_m2")
    private Double co2ParM2;

    @Column(name = "co2_par_employe")
    private Double co2ParEmploye;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detail_par_categorie", columnDefinition = "jsonb")
    private Map<String, Double> detailParCategorie;

    @Column(name = "annee_reference")
    private Integer anneeReference;

    @PrePersist
    protected void onCreate() {
        if (dateCalcul == null) {
            dateCalcul = OffsetDateTime.now();
        }
    }
}

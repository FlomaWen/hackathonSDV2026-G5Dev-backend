package com.group.hackathon_G5Dev.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(length = 500)
    private String adresse;

    @Column(length = 100)
    private String ville;

    @Column(name = "surface_totale", nullable = false)
    private Double surfaceTotale;

    @Column(name = "parking_sous_dalle")
    private Integer parkingSousDalle;

    @Column(name = "parking_sous_sol")
    private Integer parkingSousSol;

    @Column(name = "parking_aerien")
    private Integer parkingAerien;

    @Column(name = "consommation_energetique_mwh", nullable = false)
    private Double consommationEnergetiqueMwh;

    @Column(name = "nombre_employes", nullable = false)
    private Integer nombreEmployes;

    @Column(name = "nombre_postes_travail")
    private Integer nombrePostesTravail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SiteMateriau> siteMateriaux = new ArrayList<>();

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

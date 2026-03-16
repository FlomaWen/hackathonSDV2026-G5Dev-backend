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

    @Column(name = "nb_places")
    @Builder.Default
    private Integer nbPlaces = 0;

    // Batiment
    @Enumerated(EnumType.STRING)
    @Column(name = "type_batiment", length = 20)
    @Builder.Default
    private TypeBatiment typeBatiment = TypeBatiment.BUREAU;

    @Column(name = "duree_vie")
    @Builder.Default
    private Integer dureeVie = 50;

    // Energie detaillee (kWh/an)
    @Column(name = "e_elec")
    private Double eElec;

    @Column(name = "e_gaz")
    private Double eGaz;

    @Column(name = "e_fioul")
    private Double eFioul;

    @Column(name = "e_chaleur")
    private Double eChaleur;

    // Parking params
    @Column(name = "part_thermique")
    @Builder.Default
    private Double partThermique = 0.80;

    @Column(name = "part_electrique_parking")
    @Builder.Default
    private Double partElectriqueParking = 0.20;

    @Column(name = "taux_occupation")
    @Builder.Default
    private Double tauxOccupation = 0.70;

    @Column(name = "dist_moyenne_parking")
    @Builder.Default
    private Double distMoyenneParking = 5.0;

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

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EmployeGroupe> employeGroupes = new ArrayList<>();

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

    /**
     * Total des places de parking (rétro-compatibilité avec ancien modèle).
     */
    public int getTotalPlaces() {
        return nbPlaces != null ? nbPlaces : 0;
    }
}

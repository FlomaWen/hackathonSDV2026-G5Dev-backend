package com.group.hackathon_G5Dev.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "materiaux")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Materiau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(nullable = false, length = 50)
    private String categorie;

    @Column(name = "facteur_emission", nullable = false)
    private Double facteurEmission;

    @Column(nullable = false, length = 50)
    private String unite;

    @Column(length = 100)
    private String source;

    @Column(length = 500)
    private String description;
}

package com.group.hackathon_G5Dev.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "site_materiaux", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"site_id", "materiau_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteMateriau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "materiau_id", nullable = false)
    private Materiau materiau;

    @Column(nullable = false)
    private Double quantite;

    @Column(name = "unite_quantite", length = 30)
    private String uniteQuantite;
}

package com.group.hackathon_G5Dev.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employe_groupes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeGroupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(nullable = false)
    private Integer nb;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_travail", nullable = false, length = 20)
    private ModeTravail modeTravail;

    @Column(name = "jours_site")
    private Integer joursSite;

    @OneToMany(mappedBy = "employeGroupe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransportEmploye> transports = new ArrayList<>();
}

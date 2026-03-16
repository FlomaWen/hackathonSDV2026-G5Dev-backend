package com.group.hackathon_G5Dev.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transport_employes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportEmploye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_groupe_id", nullable = false)
    private EmployeGroupe employeGroupe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModeTransport mode;

    @Column(nullable = false)
    @Builder.Default
    private Double distance = 12.0;
}

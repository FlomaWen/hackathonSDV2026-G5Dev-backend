package com.group.hackathon_G5Dev.api.dto.response;

import com.group.hackathon_G5Dev.domain.model.ClasseCarbone;

import java.time.OffsetDateTime;

public record CalculResponse(
        Long id,
        Long siteId,
        OffsetDateTime dateCalcul,
        Integer anneeReference,

        // Resultats principaux
        Double ecConstruction,
        Double ecExploitation,
        Double ecTotal,
        Double ecParM2,
        Double ecParEmploye,

        // Detail construction
        DetailConstruction detailConstruction,

        // Detail exploitation
        DetailExploitation detailExploitation,

        // Benchmark
        Benchmark benchmark
) {
    public record DetailConstruction(
            Double ecBeton,
            Double ecAcier,
            Double ecVerre,
            Double ecBois,
            Double ecAutres,
            Integer dureeVie
    ) {}

    public record DetailExploitation(
            Double ecNrj,
            Double ecMob,
            Double ecPark,
            Double ecDech
    ) {}

    public record Benchmark(
            ClasseCarbone classeCarbone,
            String classeLabel,
            Double ecartConstructionPct,
            Double ecartExploitationPct,
            Double ecartTotalPct,
            Double moyConstruction,
            Double moyExploitation,
            Double moyTotal
    ) {}
}

package com.group.hackathon_G5Dev.api.dto.response;

import com.group.hackathon_G5Dev.domain.model.ClasseCarbone;

import java.time.OffsetDateTime;

public record KpiResponse(
        Long siteId,
        String siteNom,
        Double ecTotal,
        Double ecParM2,
        Double ecParEmploye,
        Double ecConstruction,
        Double ecExploitation,
        ClasseCarbone classeCarbone,
        OffsetDateTime dateCalcul
) {}

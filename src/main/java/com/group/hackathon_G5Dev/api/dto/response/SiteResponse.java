package com.group.hackathon_G5Dev.api.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record SiteResponse(
        Long id,
        String nom,
        String adresse,
        String ville,
        Double surfaceTotale,
        Integer parkingSousDalle,
        Integer parkingSousSol,
        Integer parkingAerien,
        Double consommationEnergetiqueMwh,
        Integer nombreEmployes,
        Integer nombrePostesTravail,
        List<MateriauDetailResponse> materiaux,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

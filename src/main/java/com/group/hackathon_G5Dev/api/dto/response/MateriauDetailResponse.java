package com.group.hackathon_G5Dev.api.dto.response;

public record MateriauDetailResponse(
        Long materiauId,
        String nom,
        String categorie,
        Double facteurEmission,
        String unite,
        Double quantite,
        String uniteQuantite
) {}

package com.group.hackathon_G5Dev.api.dto.response;

import com.group.hackathon_G5Dev.domain.model.TypeBatiment;

import java.time.OffsetDateTime;
import java.util.List;

public record SiteResponse(
        Long id,
        String nom,
        String adresse,
        String ville,
        Double surfaceTotale,
        Integer nbPlaces,
        TypeBatiment typeBatiment,
        Integer dureeVie,
        Double eElec,
        Double eGaz,
        Double eFioul,
        Double eChaleur,
        Double partThermique,
        Double partElectriqueParking,
        Double tauxOccupation,
        Double distMoyenneParking,
        Integer nombreEmployes,
        Integer nombrePostesTravail,
        List<MateriauDetailResponse> materiaux,
        List<EmployeGroupeResponse> employes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

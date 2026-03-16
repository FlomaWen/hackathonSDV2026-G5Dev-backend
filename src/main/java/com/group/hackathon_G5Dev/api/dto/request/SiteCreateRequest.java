package com.group.hackathon_G5Dev.api.dto.request;

import com.group.hackathon_G5Dev.domain.model.TypeBatiment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record SiteCreateRequest(
        @NotBlank(message = "Le nom du site est obligatoire")
        String nom,

        String adresse,

        String ville,

        @NotNull(message = "La surface totale est obligatoire")
        @Positive(message = "La surface totale doit être positive")
        Double surfaceTotale,

        @PositiveOrZero
        Integer nbPlaces,

        // Batiment (optionnel)
        TypeBatiment typeBatiment,
        Integer dureeVie,

        // Materiaux (optionnel, estimés depuis superficie si absent)
        @Valid
        List<MateriauQuantiteRequest> materiaux,

        // Energie detaillee en kWh/an (optionnel, estimée depuis superficie si absent)
        Double eElec,
        Double eGaz,
        Double eFioul,
        Double eChaleur,

        // Employes (liste de groupes)
        @Valid
        List<EmployeGroupeRequest> employes,

        // Parking (optionnel)
        Double partThermique,
        Double partElectriqueParking,
        Double tauxOccupation,
        Double distMoyenneParking,

        @NotNull(message = "Le nombre d'employés est obligatoire")
        @Positive(message = "Le nombre d'employés doit être positif")
        Integer nombreEmployes,

        @PositiveOrZero
        Integer nombrePostesTravail
) {}

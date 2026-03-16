package com.group.hackathon_G5Dev.api.dto.request;

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

        @PositiveOrZero(message = "Le nombre de parkings sous dalle doit être positif ou nul")
        Integer parkingSousDalle,

        @PositiveOrZero(message = "Le nombre de parkings sous sol doit être positif ou nul")
        Integer parkingSousSol,

        @PositiveOrZero(message = "Le nombre de parkings aériens doit être positif ou nul")
        Integer parkingAerien,

        @NotNull(message = "La consommation énergétique est obligatoire")
        @Positive(message = "La consommation énergétique doit être positive")
        Double consommationEnergetiqueMwh,

        @NotNull(message = "Le nombre d'employés est obligatoire")
        @Positive(message = "Le nombre d'employés doit être positif")
        Integer nombreEmployes,

        @PositiveOrZero(message = "Le nombre de postes de travail doit être positif ou nul")
        Integer nombrePostesTravail,

        @Valid
        List<MateriauQuantiteRequest> materiaux
) {}

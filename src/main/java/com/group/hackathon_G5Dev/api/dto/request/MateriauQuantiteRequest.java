package com.group.hackathon_G5Dev.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MateriauQuantiteRequest(
        @NotNull(message = "L'id du matériau est obligatoire")
        Long materiauId,

        @NotNull(message = "La quantité est obligatoire")
        @Positive(message = "La quantité doit être positive")
        Double quantite
) {}

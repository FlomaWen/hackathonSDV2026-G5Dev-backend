package com.group.hackathon_G5Dev.api.dto.request;

import com.group.hackathon_G5Dev.domain.model.ModeTravail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record EmployeGroupeRequest(
        @NotNull @Positive
        Integer nb,

        @NotNull
        ModeTravail modeTravail,

        Integer joursSite,

        @Valid
        List<TransportRequest> transports
) {}

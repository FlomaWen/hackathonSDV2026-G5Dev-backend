package com.group.hackathon_G5Dev.api.dto.request;

import com.group.hackathon_G5Dev.domain.model.ModeTransport;
import jakarta.validation.constraints.NotNull;

public record TransportRequest(
        @NotNull
        ModeTransport mode,

        Double distance
) {}

package com.group.hackathon_G5Dev.api.dto.response;

import com.group.hackathon_G5Dev.domain.model.ModeTransport;

public record TransportResponse(
        ModeTransport mode,
        Double distance
) {}

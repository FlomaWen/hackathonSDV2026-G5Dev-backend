package com.group.hackathon_G5Dev.api.dto.response;

import com.group.hackathon_G5Dev.domain.model.ModeTravail;

import java.util.List;

public record EmployeGroupeResponse(
        Integer nb,
        ModeTravail modeTravail,
        Integer joursSite,
        List<TransportResponse> transports
) {}

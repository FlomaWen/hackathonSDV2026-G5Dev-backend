package com.group.hackathon_G5Dev.api.dto.response;

import java.util.List;

public record CompareResponse(
        List<KpiResponse> sites,
        Double co2TotalMoyen,
        Double co2ParM2Moyen,
        Double co2ParEmployeMoyen
) {}

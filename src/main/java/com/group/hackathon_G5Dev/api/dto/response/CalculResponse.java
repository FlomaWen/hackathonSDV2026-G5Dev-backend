package com.group.hackathon_G5Dev.api.dto.response;

import java.time.OffsetDateTime;
import java.util.Map;

public record CalculResponse(
        Long id,
        Long siteId,
        OffsetDateTime dateCalcul,
        Double co2Construction,
        Double co2Exploitation,
        Double co2Total,
        Double co2ParM2,
        Double co2ParEmploye,
        Map<String, Double> detailParCategorie,
        Integer anneeReference
) {}

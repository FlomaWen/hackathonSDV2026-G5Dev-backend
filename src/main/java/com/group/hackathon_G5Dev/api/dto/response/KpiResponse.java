package com.group.hackathon_G5Dev.api.dto.response;

import java.time.OffsetDateTime;
import java.util.Map;

public record KpiResponse(
        Long siteId,
        String siteNom,
        Double co2Total,
        Double co2ParM2,
        Double co2ParEmploye,
        Double co2Construction,
        Double co2Exploitation,
        Map<String, Double> detailParCategorie,
        OffsetDateTime dateCalcul
) {}

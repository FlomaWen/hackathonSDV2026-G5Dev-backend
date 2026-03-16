package com.group.hackathon_G5Dev.api.controller;

import com.group.hackathon_G5Dev.api.dto.response.CompareResponse;
import com.group.hackathon_G5Dev.api.dto.response.KpiResponse;
import com.group.hackathon_G5Dev.api.mapper.CalculMapper;
import com.group.hackathon_G5Dev.domain.model.CalculCarbone;
import com.group.hackathon_G5Dev.domain.model.User;
import com.group.hackathon_G5Dev.domain.service.CarboneCalculService;
import com.group.hackathon_G5Dev.domain.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/api/comparaison")
@RequiredArgsConstructor
public class ComparaisonController {

    private final CarboneCalculService carboneCalculService;
    private final SiteService siteService;
    private final CalculMapper calculMapper;

    @GetMapping
    public ResponseEntity<CompareResponse> comparer(
            @RequestParam("siteIds") List<Long> siteIds,
            @AuthenticationPrincipal User user
    ) {
        List<KpiResponse> kpis = siteIds.stream()
                .map(siteId -> {
                    siteService.findByIdAndUser(siteId, user);
                    CalculCarbone calcul = carboneCalculService.getDernierCalcul(siteId);
                    return calculMapper.toKpiResponse(calcul);
                })
                .toList();

        double co2TotalMoyen = arrondir(kpis.stream()
                .mapToDouble(KpiResponse::ecTotal)
                .average()
                .orElse(0));

        double co2ParM2Moyen = arrondir(kpis.stream()
                .mapToDouble(KpiResponse::ecParM2)
                .average()
                .orElse(0));

        double co2ParEmployeMoyen = arrondir(kpis.stream()
                .mapToDouble(KpiResponse::ecParEmploye)
                .average()
                .orElse(0));

        return ResponseEntity.ok(new CompareResponse(kpis, co2TotalMoyen, co2ParM2Moyen, co2ParEmployeMoyen));
    }

    private double arrondir(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}

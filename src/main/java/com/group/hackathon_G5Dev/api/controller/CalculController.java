package com.group.hackathon_G5Dev.api.controller;

import com.group.hackathon_G5Dev.api.dto.response.CalculResponse;
import com.group.hackathon_G5Dev.api.dto.response.KpiResponse;
import com.group.hackathon_G5Dev.api.mapper.CalculMapper;
import com.group.hackathon_G5Dev.domain.model.CalculCarbone;
import com.group.hackathon_G5Dev.domain.model.Site;
import com.group.hackathon_G5Dev.domain.model.User;
import com.group.hackathon_G5Dev.domain.service.CarboneCalculService;
import com.group.hackathon_G5Dev.domain.service.RapportPdfService;
import com.group.hackathon_G5Dev.domain.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller pour le calcul carbone, l'historique, les KPIs et l'export PDF d'un site.
 */
@RestController
@RequestMapping("/api/sites/{siteId}")
@RequiredArgsConstructor
public class CalculController {

    private final CarboneCalculService carboneCalculService;
    private final SiteService siteService;
    private final CalculMapper calculMapper;
    private final RapportPdfService rapportPdfService;

    @PostMapping("/calcul")
    public ResponseEntity<CalculResponse> calculer(
            @PathVariable("siteId") Long siteId,
            @AuthenticationPrincipal User user
    ) {
        Site site = siteService.findByIdAndUser(siteId, user);
        CalculCarbone calcul = carboneCalculService.calculerEmpreinte(site);
        return ResponseEntity.ok(calculMapper.toResponse(calcul));
    }

    @GetMapping("/historique")
    public ResponseEntity<List<CalculResponse>> historique(
            @PathVariable("siteId") Long siteId,
            @AuthenticationPrincipal User user
    ) {
        siteService.findByIdAndUser(siteId, user);
        List<CalculResponse> historique = carboneCalculService.getHistorique(siteId).stream()
                .map(calculMapper::toResponse)
                .toList();
        return ResponseEntity.ok(historique);
    }

    @GetMapping("/kpis")
    public ResponseEntity<KpiResponse> kpis(
            @PathVariable("siteId") Long siteId,
            @AuthenticationPrincipal User user
    ) {
        siteService.findByIdAndUser(siteId, user);
        CalculCarbone calcul = carboneCalculService.getDernierCalcul(siteId);
        return ResponseEntity.ok(calculMapper.toKpiResponse(calcul));
    }

    @GetMapping("/rapport")
    public ResponseEntity<byte[]> rapport(
            @PathVariable("siteId") Long siteId,
            @AuthenticationPrincipal User user
    ) {
        siteService.findByIdAndUser(siteId, user);
        CalculCarbone calcul = carboneCalculService.getDernierCalcul(siteId);
        byte[] pdf = rapportPdfService.genererRapport(calcul);

        String filename = "rapport-carbone-site-" + siteId + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}

package com.group.hackathon_G5Dev.api.mapper;

import com.group.hackathon_G5Dev.api.dto.request.SiteCreateRequest;
import com.group.hackathon_G5Dev.api.dto.response.MateriauDetailResponse;
import com.group.hackathon_G5Dev.api.dto.response.SiteResponse;
import com.group.hackathon_G5Dev.domain.model.Site;
import com.group.hackathon_G5Dev.domain.model.SiteMateriau;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SiteMapper {

    public SiteResponse toResponse(Site site) {
        List<MateriauDetailResponse> materiaux = site.getSiteMateriaux().stream()
                .map(this::toMateriauDetail)
                .toList();

        return new SiteResponse(
                site.getId(),
                site.getNom(),
                site.getAdresse(),
                site.getVille(),
                site.getSurfaceTotale(),
                site.getParkingSousDalle(),
                site.getParkingSousSol(),
                site.getParkingAerien(),
                site.getConsommationEnergetiqueMwh(),
                site.getNombreEmployes(),
                site.getNombrePostesTravail(),
                materiaux,
                site.getCreatedAt(),
                site.getUpdatedAt()
        );
    }

    public Site toEntity(SiteCreateRequest request) {
        return Site.builder()
                .nom(request.nom())
                .adresse(request.adresse())
                .ville(request.ville())
                .surfaceTotale(request.surfaceTotale())
                .parkingSousDalle(request.parkingSousDalle())
                .parkingSousSol(request.parkingSousSol())
                .parkingAerien(request.parkingAerien())
                .consommationEnergetiqueMwh(request.consommationEnergetiqueMwh())
                .nombreEmployes(request.nombreEmployes())
                .nombrePostesTravail(request.nombrePostesTravail())
                .build();
    }

    public List<Map<String, Object>> toMateriauMaps(SiteCreateRequest request) {
        if (request.materiaux() == null) return null;

        return request.materiaux().stream()
                .map(m -> Map.<String, Object>of(
                        "materiauId", m.materiauId(),
                        "quantite", m.quantite()
                ))
                .toList();
    }

    private MateriauDetailResponse toMateriauDetail(SiteMateriau sm) {
        return new MateriauDetailResponse(
                sm.getMateriau().getId(),
                sm.getMateriau().getNom(),
                sm.getMateriau().getCategorie(),
                sm.getMateriau().getFacteurEmission(),
                sm.getMateriau().getUnite(),
                sm.getQuantite(),
                sm.getUniteQuantite()
        );
    }
}

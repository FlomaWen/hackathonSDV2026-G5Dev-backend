package com.group.hackathon_G5Dev.api.mapper;

import com.group.hackathon_G5Dev.api.dto.request.EmployeGroupeRequest;
import com.group.hackathon_G5Dev.api.dto.request.SiteCreateRequest;
import com.group.hackathon_G5Dev.api.dto.response.*;
import com.group.hackathon_G5Dev.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SiteMapper {

    public SiteResponse toResponse(Site site) {
        List<MateriauDetailResponse> materiaux = site.getSiteMateriaux().stream()
                .map(this::toMateriauDetail)
                .toList();

        List<EmployeGroupeResponse> employes = site.getEmployeGroupes() != null
                ? site.getEmployeGroupes().stream().map(this::toEmployeGroupeResponse).toList()
                : List.of();

        return new SiteResponse(
                site.getId(),
                site.getNom(),
                site.getAdresse(),
                site.getVille(),
                site.getSurfaceTotale(),
                site.getNbPlaces(),
                site.getTypeBatiment(),
                site.getDureeVie(),
                site.getEElec(),
                site.getEGaz(),
                site.getEFioul(),
                site.getEChaleur(),
                site.getPartThermique(),
                site.getPartElectriqueParking(),
                site.getTauxOccupation(),
                site.getDistMoyenneParking(),
                site.getNombreEmployes(),
                site.getNombrePostesTravail(),
                materiaux,
                employes,
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
                .nbPlaces(request.nbPlaces() != null ? request.nbPlaces() : 0)
                .typeBatiment(request.typeBatiment() != null ? request.typeBatiment() : TypeBatiment.BUREAU)
                .dureeVie(request.dureeVie() != null ? request.dureeVie() : 50)
                .eElec(request.eElec())
                .eGaz(request.eGaz())
                .eFioul(request.eFioul())
                .eChaleur(request.eChaleur())
                .partThermique(request.partThermique() != null ? request.partThermique() : 0.80)
                .partElectriqueParking(request.partElectriqueParking() != null ? request.partElectriqueParking() : 0.20)
                .tauxOccupation(request.tauxOccupation() != null ? request.tauxOccupation() : 0.70)
                .distMoyenneParking(request.distMoyenneParking() != null ? request.distMoyenneParking() : 5.0)
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

    public List<EmployeGroupeRequest> getEmployeRequests(SiteCreateRequest request) {
        return request.employes();
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

    private EmployeGroupeResponse toEmployeGroupeResponse(EmployeGroupe eg) {
        List<TransportResponse> transports = eg.getTransports() != null
                ? eg.getTransports().stream()
                    .map(t -> new TransportResponse(t.getMode(), t.getDistance()))
                    .toList()
                : List.of();

        return new EmployeGroupeResponse(
                eg.getNb(),
                eg.getModeTravail(),
                eg.getJoursSite(),
                transports
        );
    }
}

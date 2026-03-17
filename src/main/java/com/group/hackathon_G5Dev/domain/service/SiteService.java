package com.group.hackathon_G5Dev.domain.service;

import com.group.hackathon_G5Dev.api.dto.request.EmployeGroupeRequest;
import com.group.hackathon_G5Dev.domain.exception.ResourceNotFoundException;
import com.group.hackathon_G5Dev.domain.exception.UnauthorizedException;
import com.group.hackathon_G5Dev.domain.model.*;
import com.group.hackathon_G5Dev.persistence.repository.MateriauRepository;
import com.group.hackathon_G5Dev.persistence.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service CRUD pour les sites physiques.
 * Gère la création, modification et suppression des sites avec leurs
 * matériaux de construction et groupes d'employés associés.
 */
@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final MateriauRepository materiauRepository;

    public List<Site> findByUser(User user) {
        return siteRepository.findByUserId(user.getId());
    }

    public Site findByIdAndUser(Long siteId, User user) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site non trouvé avec l'id : " + siteId));
        if (!site.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Vous n'avez pas accès à ce site");
        }
        return site;
    }

    @Transactional
    public Site create(Site site, List<Map<String, Object>> materiaux, List<EmployeGroupeRequest> employes, User user) {
        site.setUser(user);
        addMateriaux(site, materiaux);
        addEmployeGroupes(site, employes);
        return siteRepository.save(site);
    }

    @Transactional
    public Site update(Long siteId, Site updated, List<Map<String, Object>> materiaux, List<EmployeGroupeRequest> employes, User user) {
        Site site = findByIdAndUser(siteId, user);

        site.setNom(updated.getNom());
        site.setAdresse(updated.getAdresse());
        site.setVille(updated.getVille());
        site.setSurfaceTotale(updated.getSurfaceTotale());
        site.setNbPlaces(updated.getNbPlaces());
        site.setTypeBatiment(updated.getTypeBatiment());
        site.setDureeVie(updated.getDureeVie());
        site.setEElec(updated.getEElec());
        site.setEGaz(updated.getEGaz());
        site.setEFioul(updated.getEFioul());
        site.setEChaleur(updated.getEChaleur());
        site.setPartThermique(updated.getPartThermique());
        site.setPartElectriqueParking(updated.getPartElectriqueParking());
        site.setTauxOccupation(updated.getTauxOccupation());
        site.setDistMoyenneParking(updated.getDistMoyenneParking());
        site.setNombreEmployes(updated.getNombreEmployes());
        site.setNombrePostesTravail(updated.getNombrePostesTravail());

        site.getSiteMateriaux().clear();
        site.getEmployeGroupes().clear();
        siteRepository.saveAndFlush(site);

        addMateriaux(site, materiaux);
        addEmployeGroupes(site, employes);

        return siteRepository.save(site);
    }

    @Transactional
    public void delete(Long siteId, User user) {
        Site site = findByIdAndUser(siteId, user);
        siteRepository.delete(site);
    }

    private void addMateriaux(Site site, List<Map<String, Object>> materiaux) {
        if (materiaux == null) return;

        for (Map<String, Object> mat : materiaux) {
            Long materiauId = ((Number) mat.get("materiauId")).longValue();
            Double quantite = ((Number) mat.get("quantite")).doubleValue();

            Materiau materiau = materiauRepository.findById(materiauId)
                    .orElseThrow(() -> new ResourceNotFoundException("Matériau non trouvé avec l'id : " + materiauId));

            SiteMateriau siteMateriau = SiteMateriau.builder()
                    .site(site)
                    .materiau(materiau)
                    .quantite(quantite)
                    .uniteQuantite("tonnes")
                    .build();

            site.getSiteMateriaux().add(siteMateriau);
        }
    }

    private void addEmployeGroupes(Site site, List<EmployeGroupeRequest> employes) {
        if (employes == null) return;

        for (EmployeGroupeRequest req : employes) {
            EmployeGroupe groupe = EmployeGroupe.builder()
                    .site(site)
                    .nb(req.nb())
                    .modeTravail(req.modeTravail())
                    .joursSite(req.joursSite())
                    .build();

            if (req.transports() != null) {
                for (var tr : req.transports()) {
                    TransportEmploye transport = TransportEmploye.builder()
                            .employeGroupe(groupe)
                            .mode(tr.mode())
                            .distance(tr.distance() != null ? tr.distance() : 12.0)
                            .build();
                    groupe.getTransports().add(transport);
                }
            }

            site.getEmployeGroupes().add(groupe);
        }
    }
}

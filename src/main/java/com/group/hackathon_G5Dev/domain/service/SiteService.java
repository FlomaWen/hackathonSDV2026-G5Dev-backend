package com.group.hackathon_G5Dev.domain.service;

import com.group.hackathon_G5Dev.domain.exception.ResourceNotFoundException;
import com.group.hackathon_G5Dev.domain.exception.UnauthorizedException;
import com.group.hackathon_G5Dev.domain.model.Materiau;
import com.group.hackathon_G5Dev.domain.model.Site;
import com.group.hackathon_G5Dev.domain.model.SiteMateriau;
import com.group.hackathon_G5Dev.domain.model.User;
import com.group.hackathon_G5Dev.persistence.repository.MateriauRepository;
import com.group.hackathon_G5Dev.persistence.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
    public Site create(Site site, List<Map<String, Object>> materiaux, User user) {
        site.setUser(user);
        addMateriaux(site, materiaux);
        return siteRepository.save(site);
    }

    @Transactional
    public Site update(Long siteId, Site updated, List<Map<String, Object>> materiaux, User user) {
        Site site = findByIdAndUser(siteId, user);

        site.setNom(updated.getNom());
        site.setAdresse(updated.getAdresse());
        site.setVille(updated.getVille());
        site.setSurfaceTotale(updated.getSurfaceTotale());
        site.setParkingSousDalle(updated.getParkingSousDalle());
        site.setParkingSousSol(updated.getParkingSousSol());
        site.setParkingAerien(updated.getParkingAerien());
        site.setConsommationEnergetiqueMwh(updated.getConsommationEnergetiqueMwh());
        site.setNombreEmployes(updated.getNombreEmployes());
        site.setNombrePostesTravail(updated.getNombrePostesTravail());

        site.getSiteMateriaux().clear();
        siteRepository.saveAndFlush(site);
        addMateriaux(site, materiaux);

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
}

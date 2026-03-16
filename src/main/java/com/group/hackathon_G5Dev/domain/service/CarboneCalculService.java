package com.group.hackathon_G5Dev.domain.service;

import com.group.hackathon_G5Dev.domain.exception.ResourceNotFoundException;
import com.group.hackathon_G5Dev.domain.model.CalculCarbone;
import com.group.hackathon_G5Dev.domain.model.Site;
import com.group.hackathon_G5Dev.domain.model.SiteMateriau;
import com.group.hackathon_G5Dev.persistence.repository.CalculCarboneRepository;
import com.group.hackathon_G5Dev.persistence.repository.MateriauRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CarboneCalculService {

    private static final String ELECTRICITE_FRANCE = "Électricité France";

    private final CalculCarboneRepository calculCarboneRepository;
    private final MateriauRepository materiauRepository;

    @Transactional
    public CalculCarbone calculerEmpreinte(Site site) {
        Map<String, Double> detail = new LinkedHashMap<>();

        double co2Construction = 0;
        for (SiteMateriau sm : site.getSiteMateriaux()) {
            double emission = sm.getQuantite() * sm.getMateriau().getFacteurEmission();
            emission = arrondir(emission);
            detail.put(sm.getMateriau().getNom(), emission);
            co2Construction += emission;
        }
        co2Construction = arrondir(co2Construction);

        double facteurElectricite = materiauRepository.findByNom(ELECTRICITE_FRANCE)
                .orElseThrow(() -> new ResourceNotFoundException("Facteur d'émission Électricité France non trouvé"))
                .getFacteurEmission();

        double co2Exploitation = arrondir(site.getConsommationEnergetiqueMwh() * facteurElectricite);
        detail.put(ELECTRICITE_FRANCE, co2Exploitation);

        double co2Total = arrondir(co2Construction + co2Exploitation);
        double co2ParM2 = arrondir(co2Total / site.getSurfaceTotale());
        double co2ParEmploye = arrondir(co2Total / site.getNombreEmployes());

        CalculCarbone calcul = CalculCarbone.builder()
                .site(site)
                .dateCalcul(OffsetDateTime.now())
                .co2Construction(co2Construction)
                .co2Exploitation(co2Exploitation)
                .co2Total(co2Total)
                .co2ParM2(co2ParM2)
                .co2ParEmploye(co2ParEmploye)
                .detailParCategorie(detail)
                .anneeReference(OffsetDateTime.now().getYear())
                .build();

        return calculCarboneRepository.save(calcul);
    }

    public List<CalculCarbone> getHistorique(Long siteId) {
        return calculCarboneRepository.findBySiteIdOrderByDateCalculDesc(siteId);
    }

    public CalculCarbone getDernierCalcul(Long siteId) {
        return calculCarboneRepository.findFirstBySiteIdOrderByDateCalculDesc(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun calcul trouvé pour le site " + siteId));
    }

    private double arrondir(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}

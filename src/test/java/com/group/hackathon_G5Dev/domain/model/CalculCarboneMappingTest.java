package com.group.hackathon_G5Dev.domain.model;

import com.group.hackathon_G5Dev.persistence.repository.CalculCarboneRepository;
import com.group.hackathon_G5Dev.persistence.repository.SiteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CalculCarboneMappingTest {

    @Autowired
    private CalculCarboneRepository calculCarboneRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Test
    @Transactional
    void testSaveCalculCarboneWithJson() {
        Site site = Site.builder()
                .nom("Test Site")
                .adresse("123 Test St")
                .surfaceTotale(100.0)
                .eElec(50000.0)
                .nombreEmployes(10)
                .user(User.builder().id(1L).build())
                .build();
        site = siteRepository.save(site);

        Map<String, Double> detail = new HashMap<>();
        detail.put("Béton", 10.5);
        detail.put("Acier", 5.2);

        CalculCarbone calcul = CalculCarbone.builder()
                .site(site)
                .dateCalcul(OffsetDateTime.now())
                .co2Construction(15.7)
                .co2Exploitation(10.0)
                .co2Total(25.7)
                .detailParCategorie(detail)
                .build();

        CalculCarbone saved = calculCarboneRepository.save(calcul);
        assertNotNull(saved.getId());
    }
}

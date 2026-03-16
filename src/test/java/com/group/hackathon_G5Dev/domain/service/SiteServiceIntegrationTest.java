package com.group.hackathon_G5Dev.domain.service;

import com.group.hackathon_G5Dev.domain.model.Materiau;
import com.group.hackathon_G5Dev.domain.model.Site;
import com.group.hackathon_G5Dev.domain.model.User;
import com.group.hackathon_G5Dev.persistence.repository.MateriauRepository;
import com.group.hackathon_G5Dev.persistence.repository.SiteRepository;
import com.group.hackathon_G5Dev.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class SiteServiceIntegrationTest {

    @Autowired
    private SiteService siteService;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MateriauRepository materiauRepository;

    private User testUser;
    private Materiau mat1;
    private Materiau mat2;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .email("test@example.com")
                .password("password")
                .role("USER")
                .build());

        mat1 = materiauRepository.save(Materiau.builder()
                .nom("Beton")
                .categorie("Construction")
                .facteurEmission(0.2)
                .unite("tonnes")
                .build());

        mat2 = materiauRepository.save(Materiau.builder()
                .nom("Acier")
                .categorie("Construction")
                .facteurEmission(1.5)
                .unite("tonnes")
                .build());
    }

    @Test
    void shouldUpdateSiteWithSameMaterialsWithoutUniqueConstraintViolation() {
        // Given: A site with mat1
        Site site = Site.builder()
                .nom("Initial Site")
                .surfaceTotale(100.0)
                .consommationEnergetiqueMwh(10.0)
                .nombreEmployes(10)
                .build();

        List<Map<String, Object>> initialMaterials = new ArrayList<>();
        initialMaterials.add(Map.of("materiauId", mat1.getId(), "quantite", 100.0));
        
        Site savedSite = siteService.create(site, initialMaterials, testUser);
        Long siteId = savedSite.getId();

        // When: Updating the site with the same material (mat1) but different quantity
        Site updateData = Site.builder()
                .nom("Updated Site")
                .surfaceTotale(100.0)
                .consommationEnergetiqueMwh(10.0)
                .nombreEmployes(10)
                .build();

        List<Map<String, Object>> updateMaterials = new ArrayList<>();
        updateMaterials.add(Map.of("materiauId", mat1.getId(), "quantite", 200.0));

        // This should not throw DataIntegrityViolationException
        Site updatedSite = siteService.update(siteId, updateData, updateMaterials, testUser);

        // Then: Verification
        assertThat(updatedSite.getNom()).isEqualTo("Updated Site");
        assertThat(updatedSite.getSiteMateriaux()).hasSize(1);
        assertThat(updatedSite.getSiteMateriaux().get(0).getMateriau().getId()).isEqualTo(mat1.getId());
        assertThat(updatedSite.getSiteMateriaux().get(0).getQuantite()).isEqualTo(200.0);
    }
}

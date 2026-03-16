package com.group.hackathon_G5Dev.persistence.repository;

import com.group.hackathon_G5Dev.domain.model.SiteMateriau;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteMateriauRepository extends JpaRepository<SiteMateriau, Long> {

    List<SiteMateriau> findBySiteId(Long siteId);

    void deleteBySiteId(Long siteId);
}

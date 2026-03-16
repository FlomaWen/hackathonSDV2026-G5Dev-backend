package com.group.hackathon_G5Dev.persistence.repository;

import com.group.hackathon_G5Dev.domain.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Long> {

    List<Site> findByUserId(Long userId);
}

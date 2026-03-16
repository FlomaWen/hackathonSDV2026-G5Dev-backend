package com.group.hackathon_G5Dev.persistence.repository;

import com.group.hackathon_G5Dev.domain.model.CalculCarbone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalculCarboneRepository extends JpaRepository<CalculCarbone, Long> {

    List<CalculCarbone> findBySiteIdOrderByDateCalculDesc(Long siteId);

    Optional<CalculCarbone> findFirstBySiteIdOrderByDateCalculDesc(Long siteId);
}

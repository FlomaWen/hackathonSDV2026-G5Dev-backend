package com.group.hackathon_G5Dev.persistence.repository;

import com.group.hackathon_G5Dev.domain.model.Materiau;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MateriauRepository extends JpaRepository<Materiau, Long> {

    List<Materiau> findByCategorie(String categorie);

    Optional<Materiau> findByNom(String nom);
}

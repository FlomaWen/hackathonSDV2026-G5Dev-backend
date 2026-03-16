package com.group.hackathon_G5Dev.api.controller;

import com.group.hackathon_G5Dev.domain.model.Materiau;
import com.group.hackathon_G5Dev.persistence.repository.MateriauRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/materiaux")
@RequiredArgsConstructor
public class MateriauController {

    private final MateriauRepository materiauRepository;

    @GetMapping
    public ResponseEntity<List<Materiau>> getAll() {
        return ResponseEntity.ok(materiauRepository.findAll());
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<Materiau>> getByCategorie(@PathVariable String categorie) {
        return ResponseEntity.ok(materiauRepository.findByCategorie(categorie));
    }
}

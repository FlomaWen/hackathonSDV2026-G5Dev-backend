package com.group.hackathon_G5Dev.api.controller;

import com.group.hackathon_G5Dev.api.dto.request.SiteCreateRequest;
import com.group.hackathon_G5Dev.api.dto.response.SiteResponse;
import com.group.hackathon_G5Dev.api.mapper.SiteMapper;
import com.group.hackathon_G5Dev.domain.model.Site;
import com.group.hackathon_G5Dev.domain.model.User;
import com.group.hackathon_G5Dev.domain.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;
    private final SiteMapper siteMapper;

    @PostMapping
    public ResponseEntity<SiteResponse> create(
            @Valid @RequestBody SiteCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        Site site = siteService.create(
                siteMapper.toEntity(request),
                siteMapper.toMateriauMaps(request),
                user
        );
        return ResponseEntity.ok(siteMapper.toResponse(site));
    }

    @GetMapping
    public ResponseEntity<List<SiteResponse>> getAll(@AuthenticationPrincipal User user) {
        List<SiteResponse> sites = siteService.findByUser(user).stream()
                .map(siteMapper::toResponse)
                .toList();
        return ResponseEntity.ok(sites);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        Site site = siteService.findByIdAndUser(id, user);
        return ResponseEntity.ok(siteMapper.toResponse(site));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SiteResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SiteCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        Site site = siteService.update(
                id,
                siteMapper.toEntity(request),
                siteMapper.toMateriauMaps(request),
                user
        );
        return ResponseEntity.ok(siteMapper.toResponse(site));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        siteService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}

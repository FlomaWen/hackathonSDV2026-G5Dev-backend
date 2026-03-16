package com.group.hackathon_G5Dev.api.mapper;

import com.group.hackathon_G5Dev.domain.model.Materiau;
import org.springframework.stereotype.Component;

@Component
public class MateriauMapper {

    public Materiau toEntity(Materiau materiau) {
        return materiau;
    }
}

package com.group.hackathon_G5Dev.api.mapper;

import com.group.hackathon_G5Dev.api.dto.response.CalculResponse;
import com.group.hackathon_G5Dev.api.dto.response.KpiResponse;
import com.group.hackathon_G5Dev.domain.model.CalculCarbone;
import org.springframework.stereotype.Component;

@Component
public class CalculMapper {

    public CalculResponse toResponse(CalculCarbone calcul) {
        return new CalculResponse(
                calcul.getId(),
                calcul.getSite().getId(),
                calcul.getDateCalcul(),
                calcul.getCo2Construction(),
                calcul.getCo2Exploitation(),
                calcul.getCo2Total(),
                calcul.getCo2ParM2(),
                calcul.getCo2ParEmploye(),
                calcul.getDetailParCategorie(),
                calcul.getAnneeReference()
        );
    }

    public KpiResponse toKpiResponse(CalculCarbone calcul) {
        return new KpiResponse(
                calcul.getSite().getId(),
                calcul.getSite().getNom(),
                calcul.getCo2Total(),
                calcul.getCo2ParM2(),
                calcul.getCo2ParEmploye(),
                calcul.getCo2Construction(),
                calcul.getCo2Exploitation(),
                calcul.getDetailParCategorie(),
                calcul.getDateCalcul()
        );
    }
}

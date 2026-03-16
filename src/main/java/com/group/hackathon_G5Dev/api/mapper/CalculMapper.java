package com.group.hackathon_G5Dev.api.mapper;

import com.group.hackathon_G5Dev.api.dto.response.CalculResponse;
import com.group.hackathon_G5Dev.api.dto.response.KpiResponse;
import com.group.hackathon_G5Dev.domain.model.CalculCarbone;
import com.group.hackathon_G5Dev.domain.model.ClasseCarbone;
import com.group.hackathon_G5Dev.domain.model.TypeBatiment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CalculMapper {

    public CalculResponse toResponse(CalculCarbone calcul) {
        Map<String, Double> detail = calcul.getDetailParCategorie();

        CalculResponse.DetailConstruction detailConstruction = new CalculResponse.DetailConstruction(
                detail.getOrDefault("EC_beton", 0.0),
                detail.getOrDefault("EC_acier", 0.0),
                detail.getOrDefault("EC_verre", 0.0),
                detail.getOrDefault("EC_bois", 0.0),
                detail.getOrDefault("EC_autres", 0.0),
                calcul.getSite().getDureeVie() != null ? calcul.getSite().getDureeVie() : 50
        );

        CalculResponse.DetailExploitation detailExploitation = new CalculResponse.DetailExploitation(
                detail.getOrDefault("EC_nrj", 0.0),
                detail.getOrDefault("EC_mob", 0.0),
                detail.getOrDefault("EC_park", 0.0),
                detail.getOrDefault("EC_dech", 0.0)
        );

        double ecParM2 = calcul.getCo2ParM2() != null ? calcul.getCo2ParM2() : 0;
        ClasseCarbone classe = ClasseCarbone.fromCo2ParM2(ecParM2);

        CalculResponse.Benchmark benchmark = new CalculResponse.Benchmark(
                classe,
                classe.getLabel(),
                detail.getOrDefault("ecart_construction_pct", 0.0),
                detail.getOrDefault("ecart_exploitation_pct", 0.0),
                detail.getOrDefault("ecart_total_pct", 0.0),
                detail.getOrDefault("moy_construction", 0.0),
                detail.getOrDefault("moy_exploitation", 0.0),
                detail.getOrDefault("moy_total", 0.0)
        );

        return new CalculResponse(
                calcul.getId(),
                calcul.getSite().getId(),
                calcul.getDateCalcul(),
                calcul.getAnneeReference(),
                calcul.getCo2Construction(),
                calcul.getCo2Exploitation(),
                calcul.getCo2Total(),
                calcul.getCo2ParM2(),
                calcul.getCo2ParEmploye(),
                detailConstruction,
                detailExploitation,
                benchmark
        );
    }

    public KpiResponse toKpiResponse(CalculCarbone calcul) {
        double ecParM2 = calcul.getCo2ParM2() != null ? calcul.getCo2ParM2() : 0;
        ClasseCarbone classe = ClasseCarbone.fromCo2ParM2(ecParM2);

        return new KpiResponse(
                calcul.getSite().getId(),
                calcul.getSite().getNom(),
                calcul.getCo2Total(),
                calcul.getCo2ParM2(),
                calcul.getCo2ParEmploye(),
                calcul.getCo2Construction(),
                calcul.getCo2Exploitation(),
                classe,
                calcul.getDateCalcul()
        );
    }
}

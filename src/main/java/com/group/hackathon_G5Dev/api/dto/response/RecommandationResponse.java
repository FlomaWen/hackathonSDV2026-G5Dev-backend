package com.group.hackathon_G5Dev.api.dto.response;

import java.util.List;
import java.util.Map;

public record RecommandationResponse(
        List<Recommandation> recommandations,
        ScoreEcoMaturite scoreEcoMaturite
) {
    public record Recommandation(
            String titre,
            String description,
            double gainEstimeKgCo2eAn,
            String difficulte,
            String poste
    ) {}

    public record ScoreEcoMaturite(
            int score,
            String niveau,
            Map<String, Integer> detailScores
    ) {}
}

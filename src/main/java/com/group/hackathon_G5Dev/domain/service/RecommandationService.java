package com.group.hackathon_G5Dev.domain.service;

import com.group.hackathon_G5Dev.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Service de génération de recommandations priorisées et du score d'éco-maturité.
 * Analyse le dernier calcul carbone et génère des conseils actionnables
 * selon le poste le plus émetteur.
 */
@Service
@RequiredArgsConstructor
public class RecommandationService {

    private final CarboneCalculService carboneCalculService;

    /**
     * Représente une recommandation d'action carbone.
     */
    public record Recommandation(
            String titre,
            String description,
            double gainEstimeKgCo2eAn,
            String difficulte, // facile, moyen, difficile
            String poste       // mobilite, energie, materiaux, dechets, parking
    ) {}

    /**
     * Représente le score d'éco-maturité sur 100.
     */
    public record ScoreEcoMaturite(
            int score,
            String niveau, // Exemplaire, Avancé, Intermédiaire, Débutant, Critique
            Map<String, Integer> detailScores
    ) {}

    /**
     * Résultat complet : recommandations + score.
     */
    public record ResultatRecommandations(
            List<Recommandation> recommandations,
            ScoreEcoMaturite scoreEcoMaturite
    ) {}

    public ResultatRecommandations generer(Long siteId) {
        CalculCarbone calcul = carboneCalculService.getDernierCalcul(siteId);
        Site site = calcul.getSite();
        Map<String, Double> detail = calcul.getDetailParCategorie();

        List<Recommandation> recommandations = genererRecommandations(calcul, site, detail);
        ScoreEcoMaturite score = calculerScoreEcoMaturite(calcul, site, detail);

        return new ResultatRecommandations(recommandations, score);
    }

    // ══════════════════════════════════════════════════════════════
    // RECOMMANDATIONS
    // ══════════════════════════════════════════════════════════════

    private List<Recommandation> genererRecommandations(CalculCarbone calcul, Site site, Map<String, Double> detail) {
        double total = calcul.getCo2Total() != null ? calcul.getCo2Total() : 1;
        double ecNrj = detail.getOrDefault("EC_nrj", 0.0);
        double ecMob = detail.getOrDefault("EC_mob", 0.0);
        double ecPark = detail.getOrDefault("EC_park", 0.0);
        double ecDech = detail.getOrDefault("EC_dech", 0.0);
        double ecConstruction = calcul.getCo2Construction() != null ? calcul.getCo2Construction() : 0;

        double pctNrj = ecNrj / total * 100;
        double pctMob = (ecMob + ecPark) / total * 100;
        double pctMat = ecConstruction / total * 100;
        double pctDech = ecDech / total * 100;

        List<Recommandation> recommandations = new ArrayList<>();

        // --- Recommandations mobilité ---
        if (pctMob > 15) {
            recommandations.add(new Recommandation(
                    "Encourager le télétravail",
                    "Mettre en place une politique de télétravail 2-3 jours/semaine pour les postes éligibles. "
                            + "Réduction estimée de 40-60% des émissions de mobilité domicile-travail.",
                    arrondir(ecMob * 0.45),
                    "facile",
                    "mobilite"
            ));
            recommandations.add(new Recommandation(
                    "Subventionner les transports en commun",
                    "Prendre en charge 75-100% des abonnements de transport en commun. "
                            + "Un employé passant de la voiture thermique au métro réduit ses émissions de 98%.",
                    arrondir(ecMob * 0.30),
                    "facile",
                    "mobilite"
            ));
        }
        if (pctMob > 25) {
            recommandations.add(new Recommandation(
                    "Installer des bornes de recharge électrique",
                    "Équiper le parking de bornes de recharge pour véhicules électriques. "
                            + "Un véhicule électrique émet 91% de moins qu'un véhicule thermique par km.",
                    arrondir(ecPark * 0.60 + ecMob * 0.10),
                    "moyen",
                    "mobilite"
            ));
            recommandations.add(new Recommandation(
                    "Plan de mobilité douce (vélo)",
                    "Proposer un forfait mobilité durable, installer des abris vélos sécurisés "
                            + "et des douches. Idéal pour les trajets < 10 km.",
                    arrondir(ecMob * 0.15),
                    "moyen",
                    "mobilite"
            ));
        }

        // --- Recommandations énergie ---
        if (pctNrj > 15) {
            recommandations.add(new Recommandation(
                    "Passer à l'éclairage LED",
                    "Remplacer l'ensemble de l'éclairage par des LED basse consommation. "
                            + "Réduction de 50-70% de la consommation d'éclairage (environ 15% de l'électricité totale).",
                    arrondir(ecNrj * 0.10),
                    "facile",
                    "energie"
            ));
            recommandations.add(new Recommandation(
                    "Optimiser le chauffage et la climatisation",
                    "Installer des thermostats programmables, réduire la consigne de 1°C en hiver "
                            + "et l'augmenter de 1°C en été. Chaque degré représente 7% d'économie.",
                    arrondir(ecNrj * 0.14),
                    "facile",
                    "energie"
            ));
        }
        if (pctNrj > 30) {
            recommandations.add(new Recommandation(
                    "Améliorer l'isolation thermique",
                    "Réaliser un audit thermique et isoler les points faibles (toiture, fenêtres, murs). "
                            + "Peut réduire les besoins en chauffage de 30-50%.",
                    arrondir(ecNrj * 0.25),
                    "difficile",
                    "energie"
            ));
            recommandations.add(new Recommandation(
                    "Souscrire un contrat d'énergie verte",
                    "Passer à un fournisseur d'électricité 100% renouvelable certifiée. "
                            + "Réduction du facteur d'émission de l'électricité.",
                    arrondir(ecNrj * 0.20),
                    "facile",
                    "energie"
            ));
        }
        if (pctNrj > 40) {
            recommandations.add(new Recommandation(
                    "Installer des panneaux solaires",
                    "Équiper la toiture de panneaux photovoltaïques pour l'autoconsommation. "
                            + "Retour sur investissement en 8-12 ans avec réduction significative de la facture.",
                    arrondir(ecNrj * 0.30),
                    "difficile",
                    "energie"
            ));
        }

        // --- Recommandations matériaux ---
        if (pctMat > 20) {
            double ecBois = detail.getOrDefault("EC_bois", 0.0);
            double ecBeton = detail.getOrDefault("EC_beton", 0.0);

            recommandations.add(new Recommandation(
                    "Privilégier le bois CLT pour les rénovations",
                    "Le bois lamellé-croisé (CLT) est un puits carbone (-750 kgCO2e/t) "
                            + "contre +200 kgCO2e/t pour le béton. À envisager pour les prochaines extensions ou rénovations.",
                    arrondir(ecBeton * 0.20),
                    "difficile",
                    "materiaux"
            ));
            recommandations.add(new Recommandation(
                    "Utiliser du béton bas carbone",
                    "Pour les futurs travaux, spécifier du béton bas carbone (CEM III ou géopolymère) "
                            + "qui réduit l'empreinte du béton de 30-50%.",
                    arrondir(ecBeton * 0.35),
                    "moyen",
                    "materiaux"
            ));
        }

        // --- Recommandations déchets ---
        if (pctDech > 5) {
            recommandations.add(new Recommandation(
                    "Renforcer le tri et le recyclage",
                    "Mettre en place le tri 5 flux (papier, plastique, verre, métal, bois) "
                            + "et sensibiliser les employés. Le recyclage émet 85% de moins que l'enfouissement.",
                    arrondir(ecDech * 0.40),
                    "facile",
                    "dechets"
            ));
        }
        if (pctDech > 10) {
            recommandations.add(new Recommandation(
                    "Réduire les déchets à la source",
                    "Supprimer les gobelets jetables, passer au zéro papier, "
                            + "installer des fontaines à eau. Objectif : -30% de volume de déchets.",
                    arrondir(ecDech * 0.30),
                    "moyen",
                    "dechets"
            ));
        }

        // Trier par gain décroissant et limiter à 5
        recommandations.sort(Comparator.comparingDouble(Recommandation::gainEstimeKgCo2eAn).reversed());
        if (recommandations.size() > 5) {
            recommandations = new ArrayList<>(recommandations.subList(0, 5));
        }

        return recommandations;
    }

    // ══════════════════════════════════════════════════════════════
    // SCORE D'ÉCO-MATURITÉ (0-100)
    // ══════════════════════════════════════════════════════════════

    ScoreEcoMaturite calculerScoreEcoMaturite(CalculCarbone calcul, Site site, Map<String, Double> detail) {
        Map<String, Integer> detailScores = new LinkedHashMap<>();

        // 1. Score classe carbone (0-30 points)
        // A=30, B=25, C=20, D=15, E=10, F=5, G=0
        double ecParM2 = calcul.getCo2ParM2() != null ? calcul.getCo2ParM2() : 0;
        ClasseCarbone classe = ClasseCarbone.fromCo2ParM2(ecParM2);
        int scoreClasse = switch (classe) {
            case A -> 30;
            case B -> 25;
            case C -> 20;
            case D -> 15;
            case E -> 10;
            case F -> 5;
            case G -> 0;
        };
        detailScores.put("classeCarbone", scoreClasse);

        // 2. Score mobilité durable (0-25 points)
        int scoreMobilite = evaluerMobilite(site);
        detailScores.put("mobilite", scoreMobilite);

        // 3. Score mix énergétique (0-20 points)
        int scoreEnergie = evaluerEnergie(site, detail);
        detailScores.put("energie", scoreEnergie);

        // 4. Score matériaux (0-15 points)
        int scoreMateriaux = evaluerMateriaux(site, detail);
        detailScores.put("materiaux", scoreMateriaux);

        // 5. Score benchmark vs secteur (0-10 points)
        int scoreBenchmark = evaluerBenchmark(detail);
        detailScores.put("benchmark", scoreBenchmark);

        int scoreTotal = scoreClasse + scoreMobilite + scoreEnergie + scoreMateriaux + scoreBenchmark;
        scoreTotal = Math.min(100, Math.max(0, scoreTotal));

        String niveau;
        if (scoreTotal >= 80) niveau = "Exemplaire";
        else if (scoreTotal >= 60) niveau = "Avance";
        else if (scoreTotal >= 40) niveau = "Intermediaire";
        else if (scoreTotal >= 20) niveau = "Debutant";
        else niveau = "Critique";

        return new ScoreEcoMaturite(scoreTotal, niveau, detailScores);
    }

    /**
     * Évalue les pratiques de mobilité (0-25 pts).
     * - Part de télétravail/hybride : 0-10 pts
     * - Part de transports doux (vélo, marche, TC) : 0-10 pts
     * - Part de véhicules électriques parking : 0-5 pts
     */
    private int evaluerMobilite(Site site) {
        int score = 0;

        if (site.getEmployeGroupes() != null && !site.getEmployeGroupes().isEmpty()) {
            int totalEmployes = site.getEmployeGroupes().stream()
                    .mapToInt(EmployeGroupe::getNb).sum();

            if (totalEmployes > 0) {
                // Part de télétravail/hybride
                int remote = site.getEmployeGroupes().stream()
                        .filter(g -> g.getModeTravail() == ModeTravail.REMOTE || g.getModeTravail() == ModeTravail.HYBRIDE)
                        .mapToInt(EmployeGroupe::getNb).sum();
                double partTeletravail = (double) remote / totalEmployes;
                score += (int) Math.min(10, partTeletravail * 20); // 50%+ = 10 pts

                // Part de transports doux
                int doux = 0, totalTransport = 0;
                for (EmployeGroupe g : site.getEmployeGroupes()) {
                    if (g.getTransports() != null) {
                        for (TransportEmploye t : g.getTransports()) {
                            totalTransport += g.getNb();
                            if (t.getMode() == ModeTransport.VELO
                                    || t.getMode() == ModeTransport.MARCHE
                                    || t.getMode() == ModeTransport.METRO
                                    || t.getMode() == ModeTransport.TRAIN
                                    || t.getMode() == ModeTransport.BUS) {
                                doux += g.getNb();
                            }
                        }
                    }
                }
                if (totalTransport > 0) {
                    double partDoux = (double) doux / totalTransport;
                    score += (int) Math.min(10, partDoux * 15); // 67%+ = 10 pts
                }
            }
        }

        // Part de véhicules électriques sur le parking
        double partElec = site.getPartElectriqueParking() != null ? site.getPartElectriqueParking() : 0.20;
        score += (int) Math.min(5, partElec * 10); // 50%+ = 5 pts

        return Math.min(25, score);
    }

    /**
     * Évalue le mix énergétique (0-20 pts).
     * - Faible intensité carbone/m² : 0-10 pts
     * - Absence de fioul : 5 pts
     * - Présence de chaleur réseau : 5 pts
     */
    private int evaluerEnergie(Site site, Map<String, Double> detail) {
        int score = 0;
        double ecNrj = detail.getOrDefault("EC_nrj", 0.0);
        double superficie = site.getSurfaceTotale();

        // Intensité carbone énergie/m²
        if (superficie > 0) {
            double nrjParM2 = ecNrj / superficie;
            // Excellent < 5, bon < 10, moyen < 20, mauvais >= 20
            if (nrjParM2 < 5) score += 10;
            else if (nrjParM2 < 10) score += 7;
            else if (nrjParM2 < 20) score += 4;
            else score += 1;
        }

        // Pas de fioul = bonus
        double fioul = site.getEFioul() != null ? site.getEFioul() : 0;
        if (fioul == 0) score += 5;

        // Chaleur réseau = bonus (souvent moins carboné)
        double chaleur = site.getEChaleur() != null ? site.getEChaleur() : 0;
        if (chaleur > 0) score += 5;

        return Math.min(20, score);
    }

    /**
     * Évalue les matériaux (0-15 pts).
     * - Présence de bois dans les matériaux : 0-10 pts
     * - Faible part de béton : 0-5 pts
     */
    private int evaluerMateriaux(Site site, Map<String, Double> detail) {
        int score = 0;
        double ecBois = Math.abs(detail.getOrDefault("EC_bois", 0.0));
        double ecBeton = detail.getOrDefault("EC_beton", 0.0);
        double ecConstruction = Math.abs(ecBois) + ecBeton
                + detail.getOrDefault("EC_acier", 0.0)
                + detail.getOrDefault("EC_verre", 0.0)
                + detail.getOrDefault("EC_autres", 0.0);

        if (ecConstruction > 0) {
            // Part du bois (puits carbone)
            double partBois = ecBois / ecConstruction;
            score += (int) Math.min(10, partBois * 30); // 33%+ = 10 pts

            // Part béton faible = bonus
            double partBeton = ecBeton / ecConstruction;
            if (partBeton < 0.3) score += 5;
            else if (partBeton < 0.5) score += 3;
            else if (partBeton < 0.7) score += 1;
        }

        return Math.min(15, score);
    }

    /**
     * Évalue la performance vs benchmark sectoriel (0-10 pts).
     */
    private int evaluerBenchmark(Map<String, Double> detail) {
        double ecartTotal = detail.getOrDefault("ecart_total_pct", 0.0);
        // < -30% = 10, < -15% = 7, < 0% = 5, < +15% = 2, >= +15% = 0
        if (ecartTotal < -30) return 10;
        if (ecartTotal < -15) return 7;
        if (ecartTotal < 0) return 5;
        if (ecartTotal < 15) return 2;
        return 0;
    }

    private double arrondir(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}

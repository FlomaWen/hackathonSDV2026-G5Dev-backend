package com.group.hackathon_G5Dev.domain.service;

import com.group.hackathon_G5Dev.domain.exception.ResourceNotFoundException;
import com.group.hackathon_G5Dev.domain.model.*;
import com.group.hackathon_G5Dev.persistence.repository.CalculCarboneRepository;
import com.group.hackathon_G5Dev.persistence.repository.MateriauRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CarboneCalculService {

    // Facteurs d'émission transport (kgCO2e/km/personne) - ADEME Base Carbone
    private static final Map<ModeTransport, Double> FACTEUR_TRANSPORT = Map.of(
            ModeTransport.VOITURE_THERMIQUE, 0.193,
            ModeTransport.VOITURE_ELECTRIQUE, 0.103,
            ModeTransport.BUS, 0.113,
            ModeTransport.METRO, 0.003,
            ModeTransport.TRAIN, 0.006,
            ModeTransport.VELO, 0.0,
            ModeTransport.MARCHE, 0.0
    );

    // Ratios estimation materiaux par defaut (tonnes/m2) pour bureau
    private static final double RATIO_BETON = 0.80;   // t/m2
    private static final double RATIO_ACIER = 0.05;
    private static final double RATIO_VERRE = 0.03;
    private static final double RATIO_BOIS = 0.02;

    // Ratios estimation energie par defaut (kWh/m2/an)
    private static final Map<TypeBatiment, Double> RATIO_ELEC = Map.of(
            TypeBatiment.BUREAU, 110.0,
            TypeBatiment.ENTREPOT, 50.0,
            TypeBatiment.COMMERCE, 150.0,
            TypeBatiment.RESIDENTIEL, 80.0
    );
    private static final Map<TypeBatiment, Double> RATIO_GAZ = Map.of(
            TypeBatiment.BUREAU, 80.0,
            TypeBatiment.ENTREPOT, 40.0,
            TypeBatiment.COMMERCE, 60.0,
            TypeBatiment.RESIDENTIEL, 100.0
    );

    // Facteur dechets : 128.5 kg dechets/employe/an * 0.5 kgCO2e/kg
    private static final double DECHETS_KG_PAR_EMPLOYE_AN = 128.5;
    private static final double FACTEUR_DECHETS = 0.5; // kgCO2e/kg

    // Jours travailles par an par defaut
    private static final int JOURS_TRAVAILLES_AN = 228;

    // Facteur parking : émission par voiture par km (aller-retour)
    private static final double FACTEUR_VOITURE_TH_KM = 0.193;
    private static final double FACTEUR_VOITURE_ELEC_KM = 0.103;

    // Facteur reseau de chaleur (kgCO2e/MWh)
    private static final double FACTEUR_CHALEUR = 125.0;

    // Moyennes benchmark par type de batiment (kgCO2e/m2/an)
    private static final Map<TypeBatiment, double[]> BENCHMARK = Map.of(
            // [moy_construction, moy_exploitation, moy_total]
            TypeBatiment.BUREAU, new double[]{5.3, 28.0, 33.3},
            TypeBatiment.ENTREPOT, new double[]{3.5, 15.0, 18.5},
            TypeBatiment.COMMERCE, new double[]{6.0, 35.0, 41.0},
            TypeBatiment.RESIDENTIEL, new double[]{4.5, 22.0, 26.5}
    );

    private final CalculCarboneRepository calculCarboneRepository;
    private final MateriauRepository materiauRepository;

    @Transactional
    public CalculCarbone calculerEmpreinte(Site site) {
        Map<String, Double> detail = new LinkedHashMap<>();

        // === 1. CONSTRUCTION (amortie sur duree_vie) ===
        int dureeVie = site.getDureeVie() != null ? site.getDureeVie() : 50;
        double superficie = site.getSurfaceTotale();

        double ecBeton = 0, ecAcier = 0, ecVerre = 0, ecBois = 0, ecAutres = 0;

        if (site.getSiteMateriaux() != null && !site.getSiteMateriaux().isEmpty()) {
            for (SiteMateriau sm : site.getSiteMateriaux()) {
                String nom = sm.getMateriau().getNom().toLowerCase();
                double emissionTotale = sm.getQuantite() * sm.getMateriau().getFacteurEmission();
                double emissionAnnuelle = emissionTotale / dureeVie;

                if (nom.contains("béton") || nom.contains("beton")) {
                    ecBeton += emissionAnnuelle;
                } else if (nom.contains("acier")) {
                    ecAcier += emissionAnnuelle;
                } else if (nom.contains("verre")) {
                    ecVerre += emissionAnnuelle;
                } else if (nom.contains("bois")) {
                    // Bois = puits carbone : facteur positif (émission fabrication) - séquestration
                    // Convention : facteur_emission stocke l'émission brute, on soustrait la séquestration
                    double sequestration = sm.getQuantite() * 1500.0 / dureeVie; // 1500 kgCO2/t séquestré
                    ecBois += (emissionAnnuelle - sequestration);
                } else {
                    ecAutres += emissionAnnuelle;
                }
            }
        } else {
            // Estimation depuis superficie
            ecBeton = estimerMateriauConstruction("Béton", superficie * RATIO_BETON, dureeVie);
            ecAcier = estimerMateriauConstruction("Acier", superficie * RATIO_ACIER, dureeVie);
            ecVerre = estimerMateriauConstruction("Verre", superficie * RATIO_VERRE, dureeVie);
            double quantiteBois = superficie * RATIO_BOIS;
            double facteurBois = getFacteurEmission("Bois");
            ecBois = (quantiteBois * facteurBois - quantiteBois * 1500.0) / dureeVie;
        }

        ecBeton = arrondir(ecBeton);
        ecAcier = arrondir(ecAcier);
        ecVerre = arrondir(ecVerre);
        ecBois = arrondir(ecBois);
        ecAutres = arrondir(ecAutres);
        double ecConstruction = arrondir(ecBeton + ecAcier + ecVerre + ecBois + ecAutres);

        detail.put("EC_beton", ecBeton);
        detail.put("EC_acier", ecAcier);
        detail.put("EC_verre", ecVerre);
        detail.put("EC_bois", ecBois);
        if (ecAutres != 0) detail.put("EC_autres", ecAutres);

        // === 2. EXPLOITATION ===

        // 2a. Energie (EC_nrj)
        TypeBatiment typeBat = site.getTypeBatiment() != null ? site.getTypeBatiment() : TypeBatiment.BUREAU;

        double elecKwh = site.getEElec() != null ? site.getEElec() : superficie * RATIO_ELEC.get(typeBat);
        double gazKwh = site.getEGaz() != null ? site.getEGaz() : superficie * RATIO_GAZ.get(typeBat);
        double fioulKwh = site.getEFioul() != null ? site.getEFioul() : 0;
        double chaleurKwh = site.getEChaleur() != null ? site.getEChaleur() : 0;

        double facteurElec = getFacteurEmission("Électricité France");
        double facteurGaz = getFacteurEmission("Gaz naturel");
        double facteurFioul = getFacteurEmission("Fioul domestique");

        // Facteurs en kgCO2e/MWh, energie en kWh -> conversion /1000
        double ecNrj = arrondir(
                (elecKwh / 1000.0) * facteurElec +
                (gazKwh / 1000.0) * facteurGaz +
                (fioulKwh / 1000.0) * facteurFioul +
                (chaleurKwh / 1000.0) * FACTEUR_CHALEUR
        );

        // 2b. Mobilite employes (EC_mob)
        double ecMob = calculerMobilite(site);
        ecMob = arrondir(ecMob);

        // 2c. Parking (EC_park)
        double ecPark = calculerParking(site);
        ecPark = arrondir(ecPark);

        // 2d. Dechets (EC_dech)
        double ecDech = arrondir(site.getNombreEmployes() * DECHETS_KG_PAR_EMPLOYE_AN * FACTEUR_DECHETS);

        double ecExploitation = arrondir(ecNrj + ecMob + ecPark + ecDech);

        detail.put("EC_nrj", ecNrj);
        detail.put("EC_mob", ecMob);
        detail.put("EC_park", ecPark);
        detail.put("EC_dech", ecDech);

        // === 3. TOTAUX ===
        double ecTotal = arrondir(ecConstruction + ecExploitation);
        double ecParM2 = arrondir(ecTotal / superficie);
        double ecParEmploye = arrondir(ecTotal / site.getNombreEmployes());

        // === 4. BENCHMARK ===
        ClasseCarbone classe = ClasseCarbone.fromCo2ParM2(ecParM2);
        double[] bench = BENCHMARK.getOrDefault(typeBat, BENCHMARK.get(TypeBatiment.BUREAU));
        double constructionParM2 = arrondir(ecConstruction / superficie);
        double exploitationParM2 = arrondir(ecExploitation / superficie);

        detail.put("classe_carbone", (double) classe.ordinal());
        detail.put("moy_construction", bench[0]);
        detail.put("moy_exploitation", bench[1]);
        detail.put("moy_total", bench[2]);
        detail.put("ecart_construction_pct", bench[0] > 0 ? arrondir((constructionParM2 - bench[0]) / bench[0] * 100) : 0);
        detail.put("ecart_exploitation_pct", bench[1] > 0 ? arrondir((exploitationParM2 - bench[1]) / bench[1] * 100) : 0);
        detail.put("ecart_total_pct", bench[2] > 0 ? arrondir((ecParM2 - bench[2]) / bench[2] * 100) : 0);

        CalculCarbone calcul = CalculCarbone.builder()
                .site(site)
                .dateCalcul(OffsetDateTime.now())
                .co2Construction(ecConstruction)
                .co2Exploitation(ecExploitation)
                .co2Total(ecTotal)
                .co2ParM2(ecParM2)
                .co2ParEmploye(ecParEmploye)
                .detailParCategorie(detail)
                .anneeReference(OffsetDateTime.now().getYear())
                .build();

        return calculCarboneRepository.save(calcul);
    }

    private double calculerMobilite(Site site) {
        double ecMob = 0;

        if (site.getEmployeGroupes() != null && !site.getEmployeGroupes().isEmpty()) {
            for (EmployeGroupe groupe : site.getEmployeGroupes()) {
                if (groupe.getModeTravail() == ModeTravail.REMOTE) {
                    continue;
                }

                int joursSite;
                if (groupe.getModeTravail() == ModeTravail.HYBRIDE) {
                    joursSite = groupe.getJoursSite() != null ? groupe.getJoursSite() : JOURS_TRAVAILLES_AN / 2;
                } else {
                    joursSite = JOURS_TRAVAILLES_AN;
                }

                if (groupe.getTransports() != null) {
                    for (TransportEmploye transport : groupe.getTransports()) {
                        double distance = transport.getDistance() != null ? transport.getDistance() : 12.0;
                        double facteur = FACTEUR_TRANSPORT.getOrDefault(transport.getMode(), 0.0);
                        // distance aller * 2 (aller-retour) * jours * nb employes
                        ecMob += groupe.getNb() * distance * 2 * joursSite * facteur;
                    }
                } else {
                    // Par defaut : voiture thermique, 12 km
                    ecMob += groupe.getNb() * 12.0 * 2 * joursSite * FACTEUR_TRANSPORT.get(ModeTransport.VOITURE_THERMIQUE);
                }
            }
        } else {
            // Pas de groupes definis : estimation par defaut
            // Tous les employes sur site, voiture thermique, 12 km
            ecMob = site.getNombreEmployes() * 12.0 * 2 * JOURS_TRAVAILLES_AN
                    * FACTEUR_TRANSPORT.get(ModeTransport.VOITURE_THERMIQUE);
        }

        return ecMob;
    }

    private double calculerParking(Site site) {
        int nbPlaces = site.getTotalPlaces();
        if (nbPlaces <= 0) return 0;

        double partTh = site.getPartThermique() != null ? site.getPartThermique() : 0.80;
        double partElec = site.getPartElectriqueParking() != null ? site.getPartElectriqueParking() : 0.20;
        double tauxOcc = site.getTauxOccupation() != null ? site.getTauxOccupation() : 0.70;
        double distMoy = site.getDistMoyenneParking() != null ? site.getDistMoyenneParking() : 5.0;

        double voituresJour = nbPlaces * tauxOcc;
        // distance aller-retour * jours travailles
        double kmTotalAn = voituresJour * distMoy * 2 * JOURS_TRAVAILLES_AN;

        return kmTotalAn * partTh * FACTEUR_VOITURE_TH_KM
                + kmTotalAn * partElec * FACTEUR_VOITURE_ELEC_KM;
    }

    private double estimerMateriauConstruction(String nomMateriau, double quantiteTonnes, int dureeVie) {
        double facteur = getFacteurEmission(nomMateriau);
        return (quantiteTonnes * facteur) / dureeVie;
    }

    private double getFacteurEmission(String nom) {
        return materiauRepository.findByNom(nom)
                .orElseThrow(() -> new ResourceNotFoundException("Facteur d'émission non trouvé : " + nom))
                .getFacteurEmission();
    }

    public List<CalculCarbone> getHistorique(Long siteId) {
        return calculCarboneRepository.findBySiteIdOrderByDateCalculDesc(siteId);
    }

    public CalculCarbone getDernierCalcul(Long siteId) {
        return calculCarboneRepository.findFirstBySiteIdOrderByDateCalculDesc(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun calcul trouvé pour le site " + siteId));
    }

    private double arrondir(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}

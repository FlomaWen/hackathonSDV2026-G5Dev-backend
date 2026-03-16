package com.group.hackathon_G5Dev.domain.model;

public enum ClasseCarbone {
    A("Excellent", 0, 10),
    B("Très bon", 10, 20),
    C("Bon", 20, 35),
    D("Moyen", 35, 55),
    E("Insuffisant", 55, 80),
    F("Mauvais", 80, 110),
    G("Critique", 110, Double.MAX_VALUE);

    private final String label;
    private final double seuilMin; // kgCO2e/m2/an
    private final double seuilMax;

    ClasseCarbone(String label, double seuilMin, double seuilMax) {
        this.label = label;
        this.seuilMin = seuilMin;
        this.seuilMax = seuilMax;
    }

    public String getLabel() {
        return label;
    }

    public double getSeuilMin() {
        return seuilMin;
    }

    public double getSeuilMax() {
        return seuilMax;
    }

    public static ClasseCarbone fromCo2ParM2(double co2ParM2) {
        for (ClasseCarbone classe : values()) {
            if (co2ParM2 >= classe.seuilMin && co2ParM2 < classe.seuilMax) {
                return classe;
            }
        }
        return G;
    }
}

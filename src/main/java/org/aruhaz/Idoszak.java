package org.aruhaz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Idoszak {
    private final String nev;
    private final Map<Termek, Double> egysegArak = new HashMap<>();
    private final Map<Termek, List<Kedvezmeny>> kedvezmenyek = new HashMap<>();

    public Idoszak(String nev) {
        this.nev = nev;
    }

    public String getNev() {
        return nev;
    }

    public void setEgysegAr(Termek termek, double egysegAr) {
        egysegArak.put(termek, egysegAr);
    }

    public double getEgysegAr(Termek termek) {
        return egysegArak.get(termek);
    }

    public void setKedvezmeny(Termek termek, double hatar, double kedvezmenyMertek) {
        kedvezmenyek
                .computeIfAbsent(termek, key -> new ArrayList<>())
                .add(new Kedvezmeny(hatar, kedvezmenyMertek));
    }

    public double getLegjobbKedvezmeny(Termek termek, double mennyiseg) {
        double legjobb = 0.0;

        for (Kedvezmeny kedvezmeny : kedvezmenyek.getOrDefault(termek, List.of())) {
            if (mennyiseg >= kedvezmeny.getHatar() && kedvezmeny.getMertek() > legjobb) {
                legjobb = kedvezmeny.getMertek();
            }
        }

        return legjobb;
    }

    private static class Kedvezmeny {
        private final double hatar;
        private final double mertek;

        private Kedvezmeny(double hatar, double mertek) {
            this.hatar = hatar;
            this.mertek = mertek;
        }

        private double getHatar() {
            return hatar;
        }

        private double getMertek() {
            return mertek;
        }
    }
}

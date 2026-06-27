package org.aruhaz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Aruhaz {
    private final Map<Termek, Double> arak = new HashMap<>();
    private final Map<Termek, List<Kedvezmeny>> kedvezmenyek = new HashMap<>();
    private final List<Idoszak> idoszakok = new ArrayList<>();

    public Aruhaz() {
    }

    public Aruhaz(Termek termek, double egysegAr) {
        arak.put(termek, egysegAr);
    }

    public Aruhaz(List<TermekAr> termekArak) {
        for (TermekAr termekAr : termekArak) {
            arak.put(termekAr.getTermek(), termekAr.getEgysegAr());
        }
    }

    public void setKedvezmeny(Termek termek, double hatar, double kedvezmenyMertek) {
        kedvezmenyek
                .computeIfAbsent(termek, key -> new ArrayList<>())
                .add(new Kedvezmeny(hatar, kedvezmenyMertek));
    }

    public void addIdoszak(Idoszak idoszak) {
        idoszakok.add(idoszak);
    }

    public double getKosarAr(Kosar kosar) {
        Map<Termek, Double> mennyisegek = osszesitMennyisegekTermekenkent(kosar);
        return osszegSzamitas(mennyisegek);
    }

    public double getKosarAr(Kosar kosar, Idoszak idoszak) {
        Map<Termek, Double> mennyisegek = osszesitMennyisegekTermekenkent(kosar);
        return osszegSzamitas(mennyisegek, idoszak);
    }

    public ArInfo getKosarAr(Kosar kosar, Idoszak idoszak, List<String> kuponok) {
        Map<Termek, Double> mennyisegek = osszesitMennyisegekTermekenkent(kosar);
        KuponAllapot kuponAllapot = kuponokAlkalmazasa(mennyisegek, idoszak, kuponok);
        double osszesen = osszegSzamitas(
                kuponAllapot.getMennyisegek(),
                idoszak,
                kuponAllapot.getSzazalekosKedvezmenyek());

        return new ArInfo(kerekites5re(osszesen), kuponAllapot.getFelNemHasznaltKuponok());
    }

    private double osszegSzamitas(Map<Termek, Double> mennyisegek) {
        double osszesen = 0.0;

        for (Map.Entry<Termek, Double> entry : mennyisegek.entrySet()) {
            Termek termek = entry.getKey();
            double mennyiseg = entry.getValue();
            double egysegAr = arak.get(termek);
            double termekOsszesen = egysegAr * mennyiseg;
            double kedvezmenyMertek = legjobbKedvezmeny(termek, mennyiseg);

            osszesen += termekOsszesen * (1.0 - kedvezmenyMertek);
        }

        return kerekites5re(osszesen);
    }

    private double osszegSzamitas(Map<Termek, Double> mennyisegek, Idoszak idoszak) {
        double osszesen = 0.0;

        for (Map.Entry<Termek, Double> entry : mennyisegek.entrySet()) {
            Termek termek = entry.getKey();
            double mennyiseg = entry.getValue();
            double egysegAr = idoszak.getEgysegAr(termek);
            double termekOsszesen = egysegAr * mennyiseg;
            double kedvezmenyMertek = idoszak.getLegjobbKedvezmeny(termek, mennyiseg);

            osszesen += termekOsszesen * (1.0 - kedvezmenyMertek);
        }

        return kerekites5re(osszesen);
    }

    private double osszegSzamitas(
            Map<Termek, Double> mennyisegek,
            Idoszak idoszak,
            Map<Termek, Double> szazalekosKedvezmenyek) {
        double osszesen = 0.0;

        for (Map.Entry<Termek, Double> entry : mennyisegek.entrySet()) {
            Termek termek = entry.getKey();
            double mennyiseg = entry.getValue();
            double egysegAr = idoszak.getEgysegAr(termek);
            double termekOsszesen = egysegAr * mennyiseg;
            double idoszakKedvezmeny = idoszak.getLegjobbKedvezmeny(termek, mennyiseg);
            double kuponKedvezmeny = szazalekosKedvezmenyek.getOrDefault(termek, 0.0);
            double legjobbKedvezmeny = Math.max(idoszakKedvezmeny, kuponKedvezmeny);

            osszesen += termekOsszesen * (1.0 - legjobbKedvezmeny);
        }

        return osszesen;
    }

    private KuponAllapot kuponokAlkalmazasa(
            Map<Termek, Double> eredetiMennyisegek,
            Idoszak idoszak,
            List<String> kuponok) {
        Map<Termek, Double> mennyisegek = new HashMap<>(eredetiMennyisegek);
        Map<Termek, Double> szazalekosKedvezmenyek = new HashMap<>();
        Map<Termek, Boolean> hasznaltKuponTermekenkent = new HashMap<>();
        List<String> felNemHasznaltKuponok = new ArrayList<>();

        for (String kupon : kuponok) {
            Termek termek = kuponTermeke(kupon);

            if (termek == null
                    || hasznaltKuponTermekenkent.getOrDefault(termek, false)
                    || mennyisegek.getOrDefault(termek, 0.0) <= 0.0) {
                felNemHasznaltKuponok.add(kupon);
                continue;
            }

            if (ingyenKupon(kupon)) {
                double regiMennyiseg = mennyisegek.get(termek);
                mennyisegek.put(termek, Math.max(0.0, regiMennyiseg - 1.0));
                hasznaltKuponTermekenkent.put(termek, true);
                continue;
            }

            double kuponKedvezmeny = kuponKedvezmenye(kupon);
            double idoszakKedvezmeny = idoszak.getLegjobbKedvezmeny(termek, mennyisegek.get(termek));

            if (kuponKedvezmeny > idoszakKedvezmeny) {
                szazalekosKedvezmenyek.put(termek, kuponKedvezmeny);
                hasznaltKuponTermekenkent.put(termek, true);
            } else {
                felNemHasznaltKuponok.add(kupon);
            }
        }

        return new KuponAllapot(mennyisegek, szazalekosKedvezmenyek, felNemHasznaltKuponok);
    }

    private Termek kuponTermeke(String kupon) {
        if (kupon.startsWith("A")) {
            return Termek.ALMA;
        }
        if (kupon.startsWith("B")) {
            return Termek.BANAN;
        }
        return null;
    }

    private boolean ingyenKupon(String kupon) {
        return kupon.equals("A-FREE1") || kupon.equals("B-FREE1");
    }

    private double kuponKedvezmenye(String kupon) {
        if (kupon.equals("A5") || kupon.equals("B5")) {
            return 0.05;
        }
        if (kupon.equals("A10") || kupon.equals("B10")) {
            return 0.10;
        }
        return 0.0;
    }

    private Map<Termek, Double> osszesitMennyisegekTermekenkent(Kosar kosar) {
        Map<Termek, Double> mennyisegek = new HashMap<>();

        for (Tetel tetel : kosar.getTetelek()) {
            double regiMennyiseg = mennyisegek.getOrDefault(tetel.getTermek(), 0.0);
            mennyisegek.put(tetel.getTermek(), regiMennyiseg + tetel.getMennyiseg());
        }

        return mennyisegek;
    }

    private double legjobbKedvezmeny(Termek termek, double mennyiseg) {
        double legjobb = 0.0;

        for (Kedvezmeny kedvezmeny : kedvezmenyek.getOrDefault(termek, List.of())) {
            if (mennyiseg >= kedvezmeny.getHatar() && kedvezmeny.getMertek() > legjobb) {
                legjobb = kedvezmeny.getMertek();
            }
        }

        return legjobb;
    }

    private double kerekites5re(double osszeg) {
        double maradek = osszeg % 10.0;

        if (maradek < 2.5) {
            return osszeg - maradek;
        } else if (maradek < 5.0) {
            return osszeg - maradek + 5.0;
        } else if (maradek < 7.5) {
            return osszeg - maradek + 5.0;
        } else {
            return osszeg - maradek + 10.0;
        }
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

    private static class KuponAllapot {
        private final Map<Termek, Double> mennyisegek;
        private final Map<Termek, Double> szazalekosKedvezmenyek;
        private final List<String> felNemHasznaltKuponok;

        private KuponAllapot(
                Map<Termek, Double> mennyisegek,
                Map<Termek, Double> szazalekosKedvezmenyek,
                List<String> felNemHasznaltKuponok) {
            this.mennyisegek = mennyisegek;
            this.szazalekosKedvezmenyek = szazalekosKedvezmenyek;
            this.felNemHasznaltKuponok = felNemHasznaltKuponok;
        }

        private Map<Termek, Double> getMennyisegek() {
            return mennyisegek;
        }

        private Map<Termek, Double> getSzazalekosKedvezmenyek() {
            return szazalekosKedvezmenyek;
        }

        private List<String> getFelNemHasznaltKuponok() {
            return felNemHasznaltKuponok;
        }
    }
}

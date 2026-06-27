package org.aruhaz;

public class TermekAr {
    private final Termek termek;
    private final double egysegAr;

    public TermekAr(Termek termek, double egysegAr) {
        this.termek = termek;
        this.egysegAr = egysegAr;
    }

    public Termek getTermek() {
        return termek;
    }

    public double getEgysegAr() {
        return egysegAr;
    }
}

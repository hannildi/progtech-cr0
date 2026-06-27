package org.aruhaz;

public class Tetel {
    private final Termek termek;
    private final double mennyiseg;

    public Tetel(Termek termek, double mennyiseg) {
        this.termek = termek;
        this.mennyiseg = mennyiseg;
    }

    public Termek getTermek() {
        return termek;
    }

    public double getMennyiseg() {
        return mennyiseg;
    }
}

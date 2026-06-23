package org.aruhaz;

import java.util.List;

public class Kosar {
    private final List<Tetel> tetelek;

    public Kosar(List<Tetel> tetelek) {
        this.tetelek = tetelek;
    }

    public List<Tetel> getTetelek() {
        return tetelek;
    }
}

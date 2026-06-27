package org.aruhaz;

import java.util.List;

public class ArInfo {
    private final double ar;
    private final List<String> felNemHasznaltKuponok;

    public ArInfo(double ar, List<String> felNemHasznaltKuponok) {
        this.ar = ar;
        this.felNemHasznaltKuponok = felNemHasznaltKuponok;
    }

    public double getAr() {
        return ar;
    }

    public List<String> getFelNemHasznaltKuponok() {
        return felNemHasznaltKuponok;
    }
}

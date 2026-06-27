import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.aruhaz.*;

class AruhazCR2Tesztek {
    static Aruhaz target;
    static Idoszak normal;

    @BeforeAll
    static void init() {
        target = new Aruhaz();
        normal = new Idoszak("Normal");
        normal.setEgysegAr(Termek.ALMA, 500.0);
        normal.setEgysegAr(Termek.BANAN, 450.0);
        normal.setKedvezmeny(Termek.ALMA, 5.0, 0.1);
        normal.setKedvezmeny(Termek.ALMA, 20.0, 0.15);
        normal.setKedvezmeny(Termek.BANAN, 2.0, 0.1);
        target.addIdoszak(normal);
    }

    @Test
    void teszt_cr2_pelda1_b10_nem_alkalmazhato() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.BANAN, 2.0)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("B10"));
        assertEquals(810.0, info.getAr(), 0.001);
        assertEquals(List.of("B10"), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda2_a10_alkalmazhato() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.ALMA, 1.0)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("A10"));
        assertEquals(450.0, info.getAr(), 0.001);
        assertEquals(List.of(), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda3_rossz_sorrend() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.ALMA, 1.0)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("A5", "A10"));
        assertEquals(475.0, info.getAr(), 0.001);
        assertEquals(List.of("A10"), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda4_jo_sorrend() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.ALMA, 1.0)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("A10", "A5"));
        assertEquals(450.0, info.getAr(), 0.001);
        assertEquals(List.of("A5"), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda5_ingyen_alma() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.ALMA, 0.5)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("A-FREE1"));
        assertEquals(0.0, info.getAr(), 0.001);
        assertEquals(List.of(), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda6_free_es_mennyisegi() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.BANAN, 3.0)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("B-FREE1"));
        assertEquals(810.0, info.getAr(), 0.001);
        assertEquals(List.of(), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda7_ket_kulon_kupon() {
        Kosar kosar = new Kosar(List.of(
                new Tetel(Termek.ALMA, 1.0),
                new Tetel(Termek.BANAN, 1.0)
        ));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("A5", "B5"));
        assertEquals(905.0, info.getAr(), 0.001);
        assertEquals(List.of(), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda8_free_kimarad_jo_kupon_marad() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.ALMA, 1.0)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("A10", "A-FREE1"));
        assertEquals(450.0, info.getAr(), 0.001);
        assertEquals(List.of("A-FREE1"), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda9_free_elsokent_nem_enged_kupon_osszevonast() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.ALMA, 1.0)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("A-FREE1", "A10"));
        assertEquals(0.0, info.getAr(), 0.001);
        assertEquals(List.of("A10"), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda10_free_utan_nem_alkalmazhato_szazalekos() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.BANAN, 1.5)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("B-FREE1", "B10"));
        double expected = kerekites5re(0.5 * 450.0);
        assertEquals(expected, info.getAr(), 0.001);
        assertEquals(List.of("B10"), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda11_kupon_nem_elozo_akcionak_jobbat() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.BANAN, 3.0)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("B5"));
        double expected = kerekites5re(3.0 * 450.0 * 0.9);
        assertEquals(expected, info.getAr(), 0.001);
        assertEquals(List.of("B5"), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda12_free_kiesik_akciobol() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.ALMA, 2.2)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("A-FREE1"));
        double expected = kerekites5re(1.2 * 500.0);
        assertEquals(expected, info.getAr(), 0.001);
        assertEquals(List.of(), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda13_ket_kupon_egy_termekre() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.BANAN, 1.0)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("B5", "B-FREE1"));
        double expected = kerekites5re(450.0 * 0.95);
        assertEquals(expected, info.getAr(), 0.001);
        assertEquals(List.of("B-FREE1"), info.getFelNemHasznaltKuponok());
    }

    @Test
    void teszt_cr2_pelda14_rossz_termek_kupon() {
        Kosar kosar = new Kosar(List.of(new Tetel(Termek.ALMA, 1.0)));
        ArInfo info = target.getKosarAr(kosar, normal, List.of("B10"));
        assertEquals(500.0, info.getAr(), 0.001);
        assertEquals(List.of("B10"), info.getFelNemHasznaltKuponok());
    }

    private double kerekites5re(double osszeg) {
        double maradek = osszeg % 10.0;
        if (maradek < 2.5) {
            return osszeg - maradek;
        }
        if (maradek < 5.0) {
            return osszeg - maradek + 5.0;
        }
        if (maradek < 7.5) {
            return osszeg - maradek + 5.0;
        }
        return osszeg - maradek + 10.0;
    }
}

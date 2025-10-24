public class Mensa {
    Baum gaeste;

    public Mensa() {
        gaeste = new Baum();

        einfuegen(new Daten("Hans", "Müller", true, 23.30, 'm'));
        einfuegen(new Daten("Veronika", "Haser", false, 13.30, 'w'));
        einfuegen(new Daten("Gerd", "Maier", true, 27.30, 'm'));
        einfuegen(new Daten("Petra", "Schöne", true, 5.10, 'w'));
        einfuegen(new Daten("Sandra", "Letzte", false, 8.90, 'w'));
        
        datenAusgeben();
    }

    public void einfuegen(Daten inhaltE) {
        gaeste.einfuegen(inhaltE);
    }

    public void datenAusgeben() {
        gaeste.datenAusgeben();
    }

    public void anzahlSchueler() {
        int anzahl = gaeste.anzahlKnoten();
        System.out.println("Es sind " + anzahl + " Schüler in der Mensa.");
    }

    public void suchen(String nameE) {
        Daten schueler = gaeste.suchen(nameE);

        if (schueler != null) {
            System.out.println("Gefundener Schüler:");
            schueler.datenAusgeben();
        } else {
            System.out.println("Schüler mit Nachnamen '" + nameE + "' nicht gefunden!");
        }
    }
}
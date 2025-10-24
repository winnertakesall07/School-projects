public class Baum {
    Baumelement wurzel;

    public Baum() {
        wurzel = new Abschlussbaum();
    }

    public void einfuegen(Daten inhaltE) {
        wurzel = wurzel.einfuegen(inhaltE);
    }

    public void datenAusgeben() {
        System.out.println("--- Mensa-Gäste (sortiert nach Nachname) ---");
        wurzel.datenAusgeben();
        System.out.println("-------------------------------------------");
    }

    public int anzahlKnoten() {
        return wurzel.anzahlKnoten();
    }

    public Daten suchen(String nameE) {
        return wurzel.suchen(nameE);
    }
}
public class Abschlussbaum extends Baumelement {
    public Abschlussbaum() {
    }

    @Override
    Baumelement einfuegen(Daten inhaltE) {
        return new Datenknoten(inhaltE);
    }

    @Override
    void datenAusgeben() {
    }

    @Override
    public int anzahlKnoten() {
        return 0;
    }

    @Override
    public Daten suchen(String nameE) {
        return null;
    }
}
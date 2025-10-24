public class Datenknoten extends Baumelement {
    Daten inhalt;
    Baumelement links;
    Baumelement rechts;

    public Datenknoten(Daten inhaltE) {
        inhalt = inhaltE;
        links = new Abschlussbaum();
        rechts = new Abschlussbaum();
    }

    @Override
    Baumelement einfuegen(Daten inhaltE) {
        int vergleich = inhaltE.compareTo(this.inhalt);

        if (vergleich < 0) {
            links = links.einfuegen(inhaltE);
        } else if (vergleich > 0) {
            rechts = rechts.einfuegen(inhaltE);
        }
        return this;
    }

    @Override
    void datenAusgeben() {
        links.datenAusgeben();
        inhalt.datenAusgeben();
        rechts.datenAusgeben();
    }

    @Override
    public int anzahlKnoten() {
        return 1 + links.anzahlKnoten() + rechts.anzahlKnoten();
    }

    @Override
    public Daten suchen(String nameE) {
        int vergleich = nameE.compareTo(this.inhalt.nameGeben());

        if (vergleich == 0) {
            return inhalt;
        } else if (vergleich < 0) {
            return links.suchen(nameE);
        } else {
            return rechts.suchen(nameE);
        }
    }
}
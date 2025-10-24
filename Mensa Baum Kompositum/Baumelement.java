public abstract class Baumelement {
    abstract Baumelement einfuegen(Daten inhaltE);
    abstract void datenAusgeben();
    abstract int anzahlKnoten();
    abstract Daten suchen(String nameE);
}
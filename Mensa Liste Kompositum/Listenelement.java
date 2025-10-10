public abstract class Listenelement
{
    abstract Listenelement einfuegen(Daten inhaltE);
    abstract void datenAusgeben();
    abstract int anzahlKnoten();
    abstract Daten suchen(String nameE);
    abstract Listenelement nachfolgerGeben();
    abstract Listenelement entfernenName(String nameE);
}

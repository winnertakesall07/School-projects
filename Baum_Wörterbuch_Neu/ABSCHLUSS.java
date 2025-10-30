class ABSCHLUSS extends BAUMELEMENT
{

    public ABSCHLUSS ()
    {

    }

    public BAUMELEMENT einfuegen(DATEN inhaltE)
    {
        return new KNOTEN(inhaltE);
    }

    public void datenAusgeben()
    {

    }

    public int anzahlKnoten()
    {
        return 0;
    }

    public void preorder()
    {

    }

    public void postorder()
    {

    }

    public void suchen(DATEN inhaltE)
    {
        System.out.println("Daten wurden nicht gefunden!");
    }
}

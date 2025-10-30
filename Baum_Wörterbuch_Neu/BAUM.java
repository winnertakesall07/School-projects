public class BAUM
{
private BAUMELEMENT wurzel;

    public BAUM ()
    {
        wurzel=new ABSCHLUSS();
    }
    
public void einfuegen(DATEN inhaltE)
{
    wurzel=wurzel.einfuegen(inhaltE);
}
public void datenAusgeben()
{
    wurzel.datenAusgeben();
}

public int anzahlKnoten()
{
    return wurzel.anzahlKnoten();
}
public void preorder()
{
    wurzel.preorder();
}

public void postorder()
{
    wurzel.postorder();
}

public void suchen(DATEN inhaltE)
{
    wurzel.suchen(inhaltE);
}

    
}

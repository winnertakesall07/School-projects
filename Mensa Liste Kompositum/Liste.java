public class Liste
{

    Listenelement anfang;

    public Liste()
    {
        anfang=new Abschluss();
    }

    void einfuegen(Daten inhaltE)
    {
           anfang=anfang.einfuegen(inhaltE);
    }

    public void einfuegenVorne(Daten inhaltE)
    {
        anfang=new Knoten(inhaltE, anfang);
    }
    
    void datenAusgeben()
    {
            anfang.datenAusgeben();   
    }

    public int anzahlKnoten()
    {
            return anfang.anzahlKnoten();
    }
    
    public Daten suchen(String nameE)
    {
        return anfang.suchen(nameE);
    }
    
    public void entfernen()
    {
        anfang=anfang.nachfolgerGeben();
    }
    
    public void entfernenName(String nameE)
    {
        anfang=anfang.entfernenName(nameE);   
     }
}

public class Knoten extends Listenelement
{
    Daten inhalt;
    Listenelement nachfolger;

    public Knoten(Daten inhaltE, Listenelement nachfolgerE)
    {
        inhalt=inhaltE;
        nachfolger=nachfolgerE;

    }

    Listenelement einfuegen(Daten inhaltE)
    {
        nachfolger=nachfolger.einfuegen(inhaltE);
        return this;
    }

    void datenAusgeben()
    {
        inhalt.datenAusgeben();
        nachfolger.datenAusgeben();
    }

    public int anzahlKnoten()
    {
            return nachfolger.anzahlKnoten()+1;
    }

    public Daten suchen(String nameE)
    {
            if( nameE==inhalt.nameGeben())
            {
                return inhalt;
            }
            else
            {
                return nachfolger.suchen(nameE);
            }
    }
    
    public Listenelement nachfolgerGeben()
    {
        return nachfolger;
    }

    public Listenelement entfernenName(String nameE)
    {
    if( nameE==inhalt.nameGeben())
            {
                return nachfolger;
            }
            else
            {
                nachfolger=nachfolger.entfernenName(nameE);
                return this;
            }
    }

    // weitere Methoden


}

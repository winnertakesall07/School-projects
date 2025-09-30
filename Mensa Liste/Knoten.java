public class Knoten
{
    Daten inhalt;
    Knoten nachfolger;
    
    
    public Knoten(Daten inhaltE, Knoten nachfolgerE)
    {
        inhalt=inhaltE;
        nachfolger=nachfolgerE;
        
    }
    
    void einfuegen(Daten inhaltE)
    {
        if (nachfolger!=null)
        {
            nachfolger.einfuegen(inhaltE);
        }
        else
        {
            nachfolger=new Knoten(inhaltE, null);
        }
    }
    // weitere Methoden




}


public class Abschluss extends Listenelement
{
    public Abschluss()
    {
    }

    Listenelement einfuegen(Daten inhaltE)
    {
      return new Knoten(inhaltE, this);
    }
    
    void datenAusgeben()
    {
    }

    public int anzahlKnoten()
    {
            return 0;
    }
    
    public Daten suchen(String nameE)
    {
              return null;
    }
    public Listenelement nachfolgerGeben()
    {
        return this;
    }
    
    public Listenelement entfernenName(String nameE)
    {
    System.out.println("Name nicht gefunden");
                return this;
    }
    // weitere Methoden




}

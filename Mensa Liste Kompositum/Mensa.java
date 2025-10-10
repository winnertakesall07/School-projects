
public class Mensa
{
    Liste schlange;

    public Mensa()
    {
        schlange = new Liste();
        
        einfuegen(new Daten("Hans", "Müller", true, 23.30, 'm'));
        einfuegen(new Daten("Veronika", "Haser", false, 13.30, 'w'));
        einfuegen(new Daten("Gerd", "Maier", true, 27.30, 'm'));
        einfuegen(new Daten("Petra", "Schöne", true, 5.10, 'w'));
        einfuegenVorne(new Daten("Sandra", "Letzte", false, 8.90, 'w'));
        datenAusgeben();
    }

    public void einfuegen(Daten inhaltE)
    {
        schlange.einfuegen(inhaltE);    
    }
    
    public void einfuegenVorne(Daten inhaltE)
    {
        schlange.einfuegenVorne(inhaltE);    
    }

    
    
    public void datenAusgeben()
    {
        schlange.datenAusgeben();
    }
    
    public void anzahlSchueler()
    {
        int anzahl=schlange.anzahlKnoten();
        System.out.println("Es sind "+anzahl+" Schüler");
    }
    
    public void suchen(String nameE)
    { 
        Daten schueler= schlange.suchen(nameE);
        
        if (schueler!=null)
        {
            System.out.println("Gefundener Schüler:");
            schueler.datenAusgeben();
        }
        else
        {   
            System.out.println("Schüler nicht gefunden!");     
        }
    }
    
    public void entfernen()
    {
        schlange.entfernen();   
        datenAusgeben();
    }
    public void entfernenName(String nameE)
    {
        schlange.entfernenName(nameE);    
        datenAusgeben();   
    }
    

    // weitere Methoden


}

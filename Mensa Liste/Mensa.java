
public class Mensa
{
    Liste schlange;

    public Mensa()
    {
        schlange = new Liste();
        
        schlange.einfuegen(new Daten("Müller"));
        schlange.einfuegen(new Daten("Maier"));
        schlange.einfuegen(new Daten("Huber"));
        schlange.einfuegen(new Daten("Haser"));
        // // datenAusgeben();
    }

    // public void datenAusgeben()
    // {
        // schlange.datenAusgeben();
    // }
    
    // public void anzahlSchueler()
    // {
        // int anzahl=schlange.anzahlSchueler();
        // System.out.println(anzahl);
    // }
    // weitere Methoden




}

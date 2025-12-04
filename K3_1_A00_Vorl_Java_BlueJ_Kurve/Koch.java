
/**
 * Beschreiben Sie hier die Klasse Koch.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public class Koch
{
    // Attribute
    private Turtle t;
    private int laenge;
    int e = 1;
    /**
     * Konstruktor für Objekte der Klasse Koch
     */
    public Koch()
    {   t = new Turtle();
        
        t.StiftHeben();
        t.PositionSetzen(350, 250);
        t.StiftSenken();
    }
    
    public void zeichne (int laengeE){
        laenge = laengeE;
        for (int i = 3; i>0; i--){
        zeichneKoch(laenge);
        t.Drehen(-120);
    }
}
    private void zeichneKoch (int laenge){
          
                t.StiftSenken();
                schrit(laenge);
                t.Drehen(60);
                schrit(laenge);
                t.Drehen(-120);
                schrit(laenge);
                t.Drehen(60);
                schrit(laenge);
                laenge = laenge*3;
            
    }
    private void schrit(int laenge){
        if (laenge > 5){
            laenge = laenge/3;
            zeichneKoch(laenge);
        }
        else{
            t.Gehen(laenge);
        }
    }
    }

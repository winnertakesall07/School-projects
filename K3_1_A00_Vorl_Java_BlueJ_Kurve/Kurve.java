
/**
 * Zeichnet die Levy-C-Kurve
 * 
 * @author Albert Wiedemann 
 * @version 1.0
 */
class Kurve
{
    /** Die Turtle */ 
    private Turtle t;
    private int[] x;
    private int[] y;
    private int zahl;
    private int test;
    private int laenge;
    

    /**
     * Legt die Turtle an
     */
    Kurve ()
    {
        t = new Turtle();
        
        t.StiftHeben();
        t.PositionSetzen(350, 250);
        t.StiftSenken();
    }
    
    /**
     * Zeichnet die Levy-C-Kurve mit der gegebenen Tiefe in der angegebenen Farbe.
     * @param tiefe Rekursionstiefe
     * @param farbe die Linienfarbe
     */
    void KurveZeichnen (int tiefe, String farbe)
    {
        t.Löschen();
        t.FarbeSetzen(farbe);
        t.PositionSetzen(350, 250);
        SchrittAusführen (150, tiefe);
    }
    
    void quadrat (int zahl, String farbe)
    {
        
        t.Löschen();
        t.FarbeSetzen(farbe);
        t.StiftHeben();
        t.PositionSetzen(350, 250);
        t.StiftHeben();
        for (int i = 5; i > 0; i--){
            
        }
        x = new int[zahl];
        y = new int[zahl];
        quadratE(zahl);
    }
    private void quadratE(int zahl){
            if (zahl > 5){
        
            t.StiftSenken();
            t.Gehen(zahl);
            t.Drehen(90);
            t.Gehen(zahl);
            t.Drehen(90);
            t.Gehen(zahl);
            t.Drehen(90);
            t.Gehen(zahl);
            t.Drehen(90);
            zahl = zahl - 3;
            quadratE(zahl);
        
    }
}
    /**
     * Zeichnet die Levy-C-Kurve mit den Tiefen 0 bis 3
     */
    void KurveZeichnen0bis3 ()
    {
        t.Löschen();
        t.FarbeSetzen("schwarz");
        t.PositionSetzen(350, 250);
        SchrittAusführen (150, 0);
        t.FarbeSetzen("rot");
        t.PositionSetzen(350, 250);
        SchrittAusführen (150, 1);
        t.FarbeSetzen("grün");
        t.PositionSetzen(350, 250);
        SchrittAusführen (150, 2);
        t.FarbeSetzen("blau");
        t.PositionSetzen(350, 250);
        SchrittAusführen (150, 3);
    }
    
  
        
      public void laengeListe() {
        laenge = 0;
        for (int i = 0; i < zahl; i++) {
            if (y[i] != 0) laenge++;
        }
        
    }
    /**
     * Zeichnet ein Element der Kurve durch Ausführen des nächsten Rekursionsschrittes
     * @param länge die Linienlänge
     * @param tiefe die (restliche) Rekursionstiefe
     */
    private void SchrittAusführen(double länge, int tiefe)
    {
        if (tiefe > 0)
        {
            t.Drehen(45);
            SchrittAusführen(länge * 0.7071, tiefe - 1);
            t.Drehen(-90);
            SchrittAusführen(länge * 0.7071, tiefe - 1);
            t.Drehen(45);
        }
        else
        {
            t.Gehen(länge);
        }
    }
}

class DATEN 
{
    /** Englisches Wort; Schlüssel dieses Datenelements */    
    private String wort; 
    /**
     * Deutsche Bedeutung; mehrere Bedeutungen werden als eine Zeichenkette zusammengefasst */  
    private String bedeutung;

    /**
     * Konstruktor für Objekte der Klasse Woerterbucheintrag
     * @param wortNeu das englische Wort.
     * @param bedeutungNeu die deutsche(n) Bedeutung(en).
     */
    DATEN(String wortNeu, String bedeutungNeu)
    {
        wort = wortNeu;
        bedeutung = bedeutungNeu;
    }

    /**
     * Gibt Information über das Datenelement zu Kontrollzwecken
     * auf das Terminalfenster aus.
     */
    void datenAusgeben()
    {
        System.out.println(wort + ": " + bedeutung);
    }

    /**
     * Vergleicht zwei Datenelemente auf Gleichheit.
     * @param dvergleich Datenelement mit dem das Objekt verglichen wird.
     * @return true, wenn die beiden Datenelemente gleichen Schlüssel haben.
     */
    boolean istGleich(DATEN dvergleich)
    {
        if(wort.compareTo(dvergleich.WortGeben()) == 0)                                                                                                                                                                                                                                                            
        {
            return true;
        }
        else
        {
            return false;
        }       
    }

    /**
     * Vergleicht zwei Datenelemente bezüglich der Ordnungsrelation.
     * @param vergleichselement Datenelement mit dem das Objekt verglichen wird.
     * @return true, wenn das aktuelle Element einen größeren Schlüssel hat, als das angegebene Vergleichselement.
     */
    boolean istGroesserAls(DATEN dvergleich)
    {
        if(wort.compareTo(dvergleich.WortGeben()) > 0)                                                                                                                                                                                                                                                            
        {
            return true;
        }
        else
        {
            return false;
        }       
    }

    String SchlüsselAlsStringGeben()
    {
        return wort;
    }   

    /**
     * Geben-Methode zum Attribut wort
     * @return das englische Wort
     */
    String WortGeben()
    {
        return wort;
    }

    /**
     * Geben-Methode zum Attribut bedeutung
     * @return bedeutung: die deutsche(n) Bedeutung(en) als eine Zeichenkette
     */
    String BedeutungGeben()
    {
        return bedeutung;
    }

    /**
     * Setzen-Methode zum Attribut bedeutung
     * @param bedeutungNeu neue Bedeutung für das englische Wort
     */
    void BedeutungSetzen(String bedeutungNeu)
    {
        bedeutung = bedeutungNeu;
    }


    
    // public boolean istKleiner(DATEN inhaltE)
    // {
        // //vorhandener Wert ist kleiner als eingegebener Wert inhaltE)
        // return (idNr<inhaltE.idNrGeben());
    // }
//     public boolean istKleiner(DATEN inhaltE)
//     {
//          System.out.println(name.compareTo(inhaltE.nameGeben()));
//         return (name.compareTo(inhaltE.nameGeben())<0);
//     }
}

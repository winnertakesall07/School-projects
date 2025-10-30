class KNOTEN extends BAUMELEMENT
{
    private DATEN inhalt;
    private BAUMELEMENT linkerNachfolger, rechterNachfolger;

    public KNOTEN (DATEN inhaltE)
    {
        inhalt =inhaltE;
        linkerNachfolger=new ABSCHLUSS();
        rechterNachfolger= new ABSCHLUSS();
    }
    
    public BAUMELEMENT einfuegen(DATEN inhaltE)
    {
        if (inhalt.istGroesserAls(inhaltE))
        {
           linkerNachfolger=linkerNachfolger.einfuegen(inhaltE); 
        }
        else
        {
           rechterNachfolger=rechterNachfolger.einfuegen(inhaltE); 
        }
        return this;
    }
    public void datenAusgeben()
{   
    linkerNachfolger.datenAusgeben();
    inhalt.datenAusgeben();
    rechterNachfolger.datenAusgeben();    
}
    public void preorder()
{   
    inhalt.datenAusgeben();
    linkerNachfolger.preorder();
    rechterNachfolger.preorder();    
}
    public void postorder()
{   
    linkerNachfolger.postorder();
    rechterNachfolger.postorder();    
    inhalt.datenAusgeben();
}

public void suchen(DATEN inhaltE)
{
    if (inhalt.istGleich(inhaltE))
    {
        inhalt.datenAusgeben();
    }
    else
    {
    if (inhalt.istGroesserAls(inhaltE))
        {
           linkerNachfolger.suchen(inhaltE); 
        }
        else
        {
          rechterNachfolger.suchen(inhaltE); 
        }
    }    
   
}



public int anzahlKnoten()
{
    return rechterNachfolger.anzahlKnoten()+linkerNachfolger.anzahlKnoten()+1;
}
}

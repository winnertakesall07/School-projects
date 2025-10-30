public class ABLAUF
{
    private BAUM binbaum;    

    ABLAUF ()
    {
       binbaum = new BAUM(); 
        
        einfuegen("clip", "Clip, Spange, Klammer");
        einfuegen("cat", "Katze");
        einfuegen("cup", "Tasse, Becher");
        einfuegen("cut", "Schnitt, Öffnung, kürzen");
        einfuegen("cube", "Würfel");
        einfuegen("cave", "Höhle");
        
        
       
    }

    public void einfuegen(String wortE, String bedeutungE)
    {
        DATEN woerterbucheintrag=new DATEN(wortE, bedeutungE);
        binbaum.einfuegen(woerterbucheintrag);
    }  

    public void datenAusgeben()
    {
        binbaum.datenAusgeben();
    }

    public void preorder()
    {
        binbaum.preorder();
    }

    public void postorder()
    {
        binbaum.postorder();
    }

    public void suchen(String wortE)
    {
        DATEN suchelement=new DATEN(wortE,"");
        binbaum.suchen(suchelement);
    }

    public int anzahlKnoten()
    {
        return binbaum.anzahlKnoten();
    }

}

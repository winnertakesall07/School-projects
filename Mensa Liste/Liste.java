public class Liste
{

    Knoten anfang;
    
    public Liste()
    {
        anfang=null;
    }

    void einfuegen(Daten inhaltE)
    {
        if (anfang!=null)
        {
            anfang.einfuegen(inhaltE);
        }
        else
        {
            anfang=new Knoten(inhaltE, null);
        }
    }




}

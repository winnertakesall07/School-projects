public class Warteschlange
{

    Schueler anfang;
    
    public Warteschlange()
    {
        anfang=null;
    }

    public void einfuegen(String nameE)
    {
        if (anfang==null)
        {
            anfang=new Schueler(nameE,null);
        }
        else 
        {
            anfang.einfuegen(nameE);
        }
        
    }
    public void datenAusgeben()
    {
        if (anfang!=null)
        {
        anfang.datenAusgeben();
        }
    }
    public int anzahlSchueler()
    {
        if (anfang!=null)
        {
            return anfang.anzahlSchueler()+1;
        }
        else
        {
            return 0;
        }
    }



}

public class Schueler
{
    // Attribute
    
    String name;
    Schueler nachfolger;
    
    public Schueler(String nameE, Schueler nachfolgerE)
    {
        name=nameE;
        nachfolger=nachfolgerE;
    }

        public void einfuegen(String nameE)
    {
        if (nachfolger==null)
        {
            nachfolger=new Schueler(nameE,null);
        }
        else 
        {
            nachfolger.einfuegen(nameE);
        }
        
    }

    public void datenAusgeben()
    {
        System.out.println(name);
        
        if (nachfolger!=null)
        {
            nachfolger.datenAusgeben();
        }
    }
    public int anzahlSchueler()
    {
        if (nachfolger!=null)
        {
            return nachfolger.anzahlSchueler()+1;
        }
        else
        {
            return 0;
        }
    }


}

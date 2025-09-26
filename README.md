public class Mensa
{
    int laenge;
    String[] namen;
    int position;
    String suche;
    boolean[] vegetarisch;

    public Mensa()
    {
        namen = new String[20];
        vegetarisch = new boolean[20];
        namen[position++] = ("Mueller");
        namen[position++] = ("Maier");
        namen[position++] = ("Huber");
        namen[position++] = ("Haser");
        position = 0;
        datenAusgeben();
        System.out.println("--------");
    }
    public void datenAusgeben()
    {
    if (null != namen[position]){
        System.out.println(namen[position]);
        position++;
        datenAusgeben();
    }
    else {
        position = 0;
    }
    }
    public void laengeliste(){
    
    if (null != namen[position]){
        position++;
        laengeliste();
    }
    else {
        System.out.println(position);
        position = 0;
        System.out.println("--------");
    }
    }
    public void schuelersuchen(String sucheE) {
        suche = sucheE;
    if (suche != namen[position]){
        position++;
        if (20 != position){
        schuelersuchen(sucheE);
        }
        else {
            System.out.println("Nicht gefunden");
            position = 0; 
            System.out.println("--------");
        }
    }
    else {
        position++;
        System.out.println(position);
        position = 0;  
        System.out.println("--------");
    }
    }
    

    // weitere Methoden




}

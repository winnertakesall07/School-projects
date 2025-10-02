public class Mensa { 
    int laenge; 
    String[] namen;
    int position;
    String suche;
    boolean[] vegetarisch;

public Mensa(int laengelisteE)
{
    System.out.println("####################");
    namen = new String[laengelisteE];
    vegetarisch = new boolean[laengelisteE];
    namen[position++] = ("Mueller");
    vegetarisch[position] = true;
    namen[position++] = ("Maier");
    vegetarisch[position] = false;
    namen[position++] = ("Huber");
    vegetarisch[position] = true;
    namen[position++] = ("Haser");
    vegetarisch[position] = false;
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
public void suchen(String sucheE) {
    suche = sucheE;
if (suche != namen[position]){
    position++;
    if (20 != position){
    suchen(sucheE);
    }
    else {
        System.out.println("Nicht gefunden");
        position = 0; 
        System.out.println("--------");
    }
}
else {
    position++;
    System.out.println(position-1);
    position = 0;  
    System.out.println("--------");
}
}
public void schuelervegetarisch(){
if (null != namen[position]){
    if (false == vegetarisch[position]){
        System.out.println(namen[position]);
        position++;
        schuelervegetarisch();
    }
    else{
        position++;
        schuelervegetarisch();
    }
}
else {
    position = 0;
    System.out.println("--------");
}
}
public void hinzufuegen(String NameE, boolean vegetarischE, int positionE){
    namen[positionE] = (NameE);
    vegetarisch[positionE] = !vegetarischE;
}
public void loeschen(int positionE){
    if (positionE != position){
    position++;
     if (20 != position){
         
     }
    else {
        System.out.println("Nicht gefunden");
        position = 0; 
        System.out.println("--------");
    }
}
else {
    
}
}
}

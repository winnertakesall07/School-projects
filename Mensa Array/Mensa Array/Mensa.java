public class Mensa { 
    String[] namen;
    int position;
    String suche;
    boolean[] vegetarisch;
    int[] geld;

public Mensa()
{
    System.out.println("####################");
    namen = new String[20];
    vegetarisch = new boolean[20];
    geld = new int[20];
    namen[position++] = ("Mueller");
    vegetarisch[position] = true;
    geld[position] = 20;
    namen[position++] = ("Maier");
    vegetarisch[position] = false;
    geld[position] = 2;
    namen[position++] = ("Huber");
    vegetarisch[position] = true;
    geld[position] = 3;
    namen[position++] = ("Haser");
    vegetarisch[position] = false;
    geld[position] = 4;
    position = 0;
    datenAusgeben();
    System.out.println("--------");
}
public void datenAusgeben()
{
if (null != namen[position]){
    System.out.println("Name:");
    System.out.println(namen[position]);
    System.out.println("Vegetaria:");
    System.out.println(vegetarisch[position]);
    System.out.println("Vermoegen:");
    System.out.println(geld[position]);
    System.out.println("()()()()()()");
    
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
public void ersetzen(String NameE, boolean vegetarischE, int positionE, int geldE){
     if (positionE < 0 || positionE >= 20) {
        System.out.println("Inkorrekte Position");
        return;
    }
    namen[positionE] = (NameE);
    vegetarisch[positionE] = !vegetarischE;
    geld[positionE] = geldE;
}
public void loeschen(int positionE){
      if (positionE < 0 || positionE >= 20) {
        System.out.println("Inkorrekte Position");
        return;
    }
    if (namen[positionE] == null) {
        System.out.println("Nichts zu loeschen bei " + positionE);
        return;
    }
    
    namen[positionE] = null;
    vegetarisch[positionE] = false;
    geld[positionE] = 0;
    
    for (int i = positionE; i < 19; i++){
        namen[i] = namen[i+1];
        vegetarisch[i] = vegetarisch[i+1];
    }
    
    namen[19] = null;
    vegetarisch[19] = false;
    geld[19] = 0;
    System.out.println("Element bei -" + positionE + "- geloescht und Liste angepasst");
    System.out.println("--------");

}
public void hinzufuegen(String NameE, boolean vegetarischE, int positionE, int geldE){
    
     if (positionE < 0 || positionE >= 20) {
        System.out.println("Inkorrekte Position");
        return;
    }
    for (int i = 19; i > positionE; i--){
        namen[i] = namen[i-1];
        vegetarisch[i] = vegetarisch[i-1];
        
    }
    namen[positionE] = (NameE);
    vegetarisch[positionE] = !vegetarischE;
    geld[positionE] = geldE;
    System.out.println("Element bei -" + positionE + "- hinzugefuegt und Liste angepasst");
    System.out.println("--------");
}
}


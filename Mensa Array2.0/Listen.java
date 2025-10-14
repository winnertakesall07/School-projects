
public abstract class Listen { 
    protected String[] namen;
    protected int position;
    protected String suche;
    protected boolean[] booleanwert;
    protected int[] zahl;
    protected String nameZahl;
    protected String nameboolean;
    public static final int MAX_SIZE = 20;


    public Listen() {
        namen = new String[MAX_SIZE];
        booleanwert = new boolean[MAX_SIZE];
        zahl = new int[MAX_SIZE];
        position = 0;

    
    }

    public void datenAusgeben() {
        for (int i = 0; i < MAX_SIZE; i++) {
            if (namen[i] != null) {
                System.out.println("Namen:");
                System.out.println(namen[i]);
                System.out.println(nameboolean + ":");
                System.out.println(booleanwert[i]);
                System.out.println(nameZahl + ":");
                System.out.println(zahl[i]);
                System.out.println("()()()()()()");
            }
        }
        System.out.println("----------");
    }


    public void laengeListe() {
        int laenge = 0;
        for (int i = 0; i < MAX_SIZE; i++) {
            if (namen[i] != null) laenge++;
        }
        System.out.println(laenge);
        System.out.println("--------");
    }

    public void suchen(String sucheE) {
        boolean gefunden = false;
        for (int i = 0; i < MAX_SIZE; i++) {
            if (namen[i] != null && namen[i].equals(sucheE)) {
                System.out.println(i);
                gefunden = true;
                break;
            }
        }
        if (!gefunden) {
            System.out.println("Nicht gefunden");
        }
        System.out.println("--------");
    }

    public void ersetzen(String nameE, boolean booleanWertE, int positionE, int zahlE) {
        if (positionE < 0 || positionE >= MAX_SIZE) {
            System.out.println("Inkorrekte Position");
            return;
        }
        namen[positionE] = nameE;
        booleanwert[positionE] = booleanWertE;
        zahl[positionE] = zahlE;
    }

    public void loeschen(int positionE) {
        if (positionE < 0 || positionE >= MAX_SIZE) {
            System.out.println("Inkorrekte Position");
            return;
        }
        if (namen[positionE] == null) {
            System.out.println("Nichts zu loeschen bei " + positionE);
            return;
        }
        for (int i = positionE; i < MAX_SIZE - 1; i++) {
            namen[i] = namen[i + 1];
            booleanwert[i] = booleanwert[i + 1];
            zahl[i] = zahl[i + 1];
        }
        namen[MAX_SIZE - 1] = null;
        booleanwert[MAX_SIZE - 1] = false;
        zahl[MAX_SIZE - 1] = 0;
        System.out.println("Element bei -" + positionE + "- geloescht und Liste angepasst");
        System.out.println("--------");
    }

    public void hinzufuegen(String nameE, boolean booleanWertE, int positionE, int zahlE) {
        if (positionE < 0 || positionE >= MAX_SIZE) {
            System.out.println("Inkorrekte Position");
            return;
        }
        for (int i = MAX_SIZE - 1; i > positionE; i--) {
            namen[i] = namen[i - 1];
            booleanwert[i] = booleanwert[i - 1];
            zahl[i] = zahl[i - 1];
        }
        namen[positionE] = nameE;
        booleanwert[positionE] = booleanWertE;
        zahl[positionE] = zahlE;
        System.out.println("Element bei -" + positionE + "- hinzugefuegt und Liste angepasst");
        System.out.println("--------");
    }
    public void summe(){
        int summe = 0;
        for (int i = MAX_SIZE -1; i >= 0; i--){
            summe = summe + zahl[i];
        }
        System.out.println("Summe aller Zahlen ist:" + summe);
    }
    public void sortierennachZahl(){
        for (int i = 0; i < MAX_SIZE-1; i++){
            for (int e = 0; e < MAX_SIZE-1; e++){
                if (zahl[e] < zahl[e+1]){
                    String Ename = namen[e];
                    boolean Eboolean = booleanwert[e];
                    int Ezahl = zahl[e];
                    namen[e] = namen[e+1];
                    booleanwert[e] = booleanwert[e+1];
                    zahl[e] = zahl[e+1];
                    namen[e+1] = Ename;
                    booleanwert[e+1] = Eboolean;
                    zahl[e+1] = Ezahl;
                }
        }
    }
}}
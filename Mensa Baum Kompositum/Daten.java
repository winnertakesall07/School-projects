public class Daten implements Comparable<Daten> {
    String vorname, nachname;
    int alter;
    boolean vegetarisch;
    double guthaben;
    char geschlecht;

    public Daten(String vornameE, String nachnameE, boolean vegetarischE, double guthabenE, char geschlechtE) {
        vorname = vornameE;
        nachname = nachnameE;
        vegetarisch = vegetarischE;
        guthaben = guthabenE;
        geschlecht = geschlechtE;
    }

    public void datenAusgeben() {
        System.out.println(vorname + " " + nachname + " " + guthaben);
    }

    public String nameGeben() {
        return nachname;
    }

    @Override
    public int compareTo(Daten andereDaten) {
        return this.nachname.compareTo(andereDaten.nameGeben());
    }
}
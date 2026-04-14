public class Pattern {
    private String name;
    private int[][] coordinates;

    public Pattern(String name, int[][] coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public String getName() { return name; }
    public int[][] getCoordinates() { return coordinates; }

    public static Pattern glider() {
        return new Pattern("Glider", new int[][]{
            {1,0},{2,1},{0,2},{1,2},{2,2}
        });
    }

    public static Pattern gosperGliderGun() {
        return new Pattern("Gosper Glider Gun", new int[][]{
            {24,0},
            {22,1},{24,1},
            {12,2},{13,2},{20,2},{21,2},{34,2},{35,2},
            {11,3},{15,3},{20,3},{21,3},{34,3},{35,3},
            {0,4},{1,4},{10,4},{16,4},{20,4},{21,4},
            {0,5},{1,5},{10,5},{14,5},{16,5},{17,5},{22,5},{24,5},
            {10,6},{16,6},{24,6},
            {11,7},{15,7},
            {12,8},{13,8}
        });
    }

    public static Pattern pulsar() {
        return new Pattern("Pulsar", new int[][]{
            {2,0},{3,0},{4,0},{8,0},{9,0},{10,0},
            {0,2},{5,2},{7,2},{12,2},
            {0,3},{5,3},{7,3},{12,3},
            {0,4},{5,4},{7,4},{12,4},
            {2,5},{3,5},{4,5},{8,5},{9,5},{10,5},
            {2,7},{3,7},{4,7},{8,7},{9,7},{10,7},
            {0,8},{5,8},{7,8},{12,8},
            {0,9},{5,9},{7,9},{12,9},
            {0,10},{5,10},{7,10},{12,10},
            {2,12},{3,12},{4,12},{8,12},{9,12},{10,12}
        });
    }

    public static Pattern blinker() {
        return new Pattern("Blinker", new int[][]{
            {0,1},{1,1},{2,1}
        });
    }

    public static Pattern beacon() {
        return new Pattern("Beacon", new int[][]{
            {0,0},{1,0},{0,1},
            {3,2},{2,3},{3,3}
        });
    }

    @Override
    public String toString() { return name; }
}

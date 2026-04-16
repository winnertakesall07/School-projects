/**
 * A named set of (x, y) cell coordinates that can be stamped onto the board.
 * Coordinates are relative to the pattern's own top-left corner (0, 0).
 *
 * Factory methods are provided for a wide variety of well-known Game-of-Life
 * structures grouped by category:
 *   - Still lifes
 *   - Oscillators (P2, P3, P8, P15)
 *   - Spaceships
 *   - Guns
 *   - Methuselahs (long-lived seeds)
 */
public class Pattern {
    private final String name;
    private final int[][] coordinates;

    public Pattern(String name, int[][] coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public String getName() { return name; }

    public int[][] getCoordinates() { return coordinates; }

    /**
     * Returns {width, height} of the bounding box that contains all cells,
     * measured from (0, 0) to (maxX+1, maxY+1).
     */
    public int[] getBounds() {
        int maxX = 0, maxY = 0;
        for (int[] c : coordinates) {
            if (c[0] > maxX) maxX = c[0];
            if (c[1] > maxY) maxY = c[1];
        }
        return new int[]{maxX + 1, maxY + 1};
    }

    @Override
    public String toString() { return name; }

    // ─────────────────────────────────────────────────────────────────────────
    // STILL LIFES — patterns that never change
    // ─────────────────────────────────────────────────────────────────────────

    public static Pattern block() {
        return new Pattern("Block (still)", new int[][]{
            {0,0},{1,0},
            {0,1},{1,1}
        });
    }

    public static Pattern beehive() {
        return new Pattern("Beehive (still)", new int[][]{
            {1,0},{2,0},
            {0,1},{3,1},
            {1,2},{2,2}
        });
    }

    public static Pattern loaf() {
        return new Pattern("Loaf (still)", new int[][]{
            {1,0},{2,0},
            {0,1},{3,1},
            {1,2},{3,2},
            {2,3}
        });
    }

    public static Pattern boat() {
        return new Pattern("Boat (still)", new int[][]{
            {0,0},{1,0},
            {0,1},{2,1},
            {1,2}
        });
    }

    public static Pattern tub() {
        return new Pattern("Tub (still)", new int[][]{
            {1,0},
            {0,1},{2,1},
            {1,2}
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OSCILLATORS
    // ─────────────────────────────────────────────────────────────────────────

    /** Blinker — period 2, the simplest oscillator. */
    public static Pattern blinker() {
        return new Pattern("Blinker (P2)", new int[][]{
            {0,1},{1,1},{2,1}
        });
    }

    /**
     * Toad — period 2.
     * .###
     * ###.
     */
    public static Pattern toad() {
        return new Pattern("Toad (P2)", new int[][]{
            {1,0},{2,0},{3,0},
            {0,1},{1,1},{2,1}
        });
    }

    /**
     * Clock — period 2.
     * .#..
     * ..##
     * ##..
     * ..#.
     */
    public static Pattern clock() {
        return new Pattern("Clock (P2)", new int[][]{
            {1,0},
            {2,1},{3,1},
            {0,2},{1,2},
            {2,3}
        });
    }

    /**
     * Beacon — period 2.
     * ##..
     * #...
     * ...#
     * ..##
     */
    public static Pattern beacon() {
        return new Pattern("Beacon (P2)", new int[][]{
            {0,0},{1,0},
            {0,1},
            {3,2},
            {2,3},{3,3}
        });
    }

    /**
     * Pulsar — period 3, the most well-known large oscillator.
     * (13 × 13 bounding box)
     */
    public static Pattern pulsar() {
        return new Pattern("Pulsar (P3)", new int[][]{
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

    /**
     * Figure Eight — period 8.
     * Two overlapping 3×3 blocks that interact.
     */
    public static Pattern figureEight() {
        return new Pattern("Figure Eight (P8)", new int[][]{
            {0,0},{1,0},{2,0},
            {0,1},{1,1},{2,1},
            {0,2},{1,2},{2,2},
            {3,3},{4,3},{5,3},
            {3,4},{4,4},{5,4},
            {3,5},{4,5},{5,5}
        });
    }

    /**
     * Pentadecathlon — period 15.
     * ..#....#..
     * ##.####.##
     * ..#....#..
     */
    public static Pattern pentadecathlon() {
        return new Pattern("Pentadecathlon (P15)", new int[][]{
            {2,0},{7,0},
            {0,1},{1,1},{3,1},{4,1},{5,1},{6,1},{8,1},{9,1},
            {2,2},{7,2}
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SPACESHIPS — patterns that translate across the board
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Glider — moves diagonally, period 4, displacement (1,1).
     */
    public static Pattern glider() {
        return new Pattern("Glider", new int[][]{
            {1,0},
            {2,1},
            {0,2},{1,2},{2,2}
        });
    }

    /**
     * LWSS (Light-weight Spaceship) — period 4, displacement (2,0).
     * .#..#
     * #....
     * #...#
     * .####
     */
    public static Pattern lwss() {
        return new Pattern("LWSS (spaceship)", new int[][]{
            {1,0},{4,0},
            {0,1},
            {0,2},{4,2},
            {1,3},{2,3},{3,3},{4,3}
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GUNS — patterns that emit gliders or spaceships indefinitely
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gosper Glider Gun — period 30, the first gun ever discovered.
     * Emits one glider every 30 generations.
     */
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

    // ─────────────────────────────────────────────────────────────────────────
    // METHUSELAHS — small seeds that take a very long time to stabilise
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * R-pentomino — 5 cells, lives for 1,103 generations before stabilising.
     * .##
     * ##.
     * .#.
     */
    public static Pattern rPentomino() {
        return new Pattern("R-pentomino (meta)", new int[][]{
            {1,0},{2,0},
            {0,1},{1,1},
            {1,2}
        });
    }

    /**
     * Diehard — 7 cells, dies completely after exactly 130 generations.
     * ......#.
     * ##......
     * .#...###
     */
    public static Pattern diehard() {
        return new Pattern("Diehard (meta)", new int[][]{
            {6,0},
            {0,1},{1,1},
            {1,2},{5,2},{6,2},{7,2}
        });
    }

    /**
     * Acorn — 7 cells, lives for 5,206 generations before stabilising.
     * .#.....
     * ...#...
     * ##..###
     */
    public static Pattern acorn() {
        return new Pattern("Acorn (meta)", new int[][]{
            {1,0},
            {3,1},
            {0,2},{1,2},{4,2},{5,2},{6,2}
        });
    }

    /**
     * TicTacToe Computer — the wall structure of a 63×63 tic-tac-toe board
     * built from GoL cells.  Placing this pattern activates the TicTacToe mode
     * in GameOfLifeGUI v3.
     *
     * The board is divided into 9 squares by double-line walls at rows/cols
     * 0, 20, 21, 41, 42, 62.  All cells on those rows or columns are alive
     * (wall cells).
     */
    public static Pattern ticTacToeComputer() {
        java.util.List<int[]> coords = new java.util.ArrayList<>();
        int[] wallRows = {0, 20, 21, 41, 42, 62};
        int[] wallCols = {0, 20, 21, 41, 42, 62};

        // Collect all (col, row) pairs where the cell is on a wall row or wall col
        java.util.Set<Integer> wallRowSet = new java.util.HashSet<>();
        java.util.Set<Integer> wallColSet = new java.util.HashSet<>();
        for (int r : wallRows) wallRowSet.add(r);
        for (int c : wallCols) wallColSet.add(c);

        for (int r = 0; r < 63; r++) {
            for (int c = 0; c < 63; c++) {
                if (wallRowSet.contains(r) || wallColSet.contains(c)) {
                    coords.add(new int[]{c, r}); // Pattern uses {x, y} = {col, row}
                }
            }
        }
        return new Pattern("TicTacToe Computer (GoL)", coords.toArray(new int[0][]));
    }
}

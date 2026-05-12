import java.io.*;
import java.util.*;

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
 *   - Turing Machines (computer simulations)
 */
public class Pattern {
    private final String name;
    private final int[][] coordinates;

    /**
     * Optional named regions within this pattern (e.g. input/output areas).
     * Each entry is {x, y, width, height} in pattern-relative cell coordinates.
     * May be null if the pattern has no annotated regions.
     */
    private final String[] regionLabels;
    private final int[][] regionBoxes;

    public Pattern(String name, int[][] coordinates) {
        this(name, coordinates, null, null);
    }

    public Pattern(String name, int[][] coordinates,
                   String[] regionLabels, int[][] regionBoxes) {
        this.name         = name;
        this.coordinates  = coordinates;
        this.regionLabels = regionLabels;
        this.regionBoxes  = regionBoxes;
    }

    public String getName() { return name; }

    public int[][] getCoordinates() { return coordinates; }

    /** Returns the region labels, or {@code null} if none are defined. */
    public String[] getRegionLabels() { return regionLabels; }

    /** Returns the region bounding boxes {x,y,w,h}, or {@code null} if none. */
    public int[][] getRegionBoxes() { return regionBoxes; }

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
    // COLLISION COMPUTING STORYBOARD (v10)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Step-by-step storyboard showing two signal streams before collision.
     */
    public static Pattern collisionComputingStep1() {
        return collisionStoryboard(
            "Collision Computing 1/4 — Signals armed",
            6, 50, true, true,
            "STEP 1: Place both inputs",
            "Signals are far apart."
        );
    }

    /**
     * Step-by-step storyboard showing signal streams approaching each other.
     */
    public static Pattern collisionComputingStep2() {
        return collisionStoryboard(
            "Collision Computing 2/4 — Approach",
            16, 40, true, true,
            "STEP 2: Inputs approach",
            "Pulses move toward the gate."
        );
    }

    /**
     * Step-by-step storyboard showing the collision moment.
     */
    public static Pattern collisionComputingStep3() {
        return collisionStoryboard(
            "Collision Computing 3/4 — Impact at gate",
            24, 32, true, true,
            "STEP 3: Collision at gate",
            "Discuss annihilation/debris."
        );
    }

    /**
     * Step-by-step storyboard showing one surviving signal lane.
     */
    public static Pattern collisionComputingStep4() {
        return collisionStoryboard(
            "Collision Computing 4/4 — Output readout",
            30, 0, true, false,
            "STEP 4: Read output lane",
            "Only one signal remains."
        );
    }

    /**
     * Variant for truth-table explanations where only input A is active.
     */
    public static Pattern collisionGateInputA() {
        return collisionStoryboard(
            "Collision Gate — Input A=1, B=0",
            14, 44, true, false,
            "CASE: A=1, B=0",
            "Single stream enters gate."
        );
    }

    /**
     * Variant for truth-table explanations where only input B is active.
     */
    public static Pattern collisionGateInputB() {
        return collisionStoryboard(
            "Collision Gate — Input A=0, B=1",
            14, 44, false, true,
            "CASE: A=0, B=1",
            "Single stream enters gate."
        );
    }

    /**
     * Variant for truth-table explanations where both inputs are active.
     */
    public static Pattern collisionGateInputAB() {
        return collisionStoryboard(
            "Collision Gate — Input A=1, B=1",
            14, 44, true, true,
            "CASE: A=1, B=1",
            "Two streams collide in gate."
        );
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

    // ─────────────────────────────────────────────────────────────────────────
    // TURING MACHINE — a pattern that simulates a universal computer
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Turing Machine (v1) loaded from "turingmaschine.txt".
     */
    public static Pattern turingMachineV1() {
        return loadTuringMachine("Turing Machine v1 (computer)", new String[]{
            "turingmaschine.txt",
            "ConwaysGameOfLife_v8/turingmaschine.txt",
            "ConwaysGameOfLife_v7/turingmaschine.txt",
            "ConwaysGameOfLife_v6/turingmaschine.txt",
            "ConwaysGameOfLife_v5/turingmaschine.txt",
            "ConwaysGameOfLife_v4/turingmaschine.txt",
            "../turingmaschine.txt"
        });
    }

    /**
     * Turing Machine (v2) loaded from "turingmaschineV2.txt".
     */
    public static Pattern turingMachineV2() {
        return loadTuringMachine("Turing Machine v2 (computer)", true, new String[]{
            "turingmaschineV2.txt",
            "ConwaysGameOfLife_v8/turingmaschineV2.txt",
            "ConwaysGameOfLife_v7/turingmaschineV2.txt",
            "ConwaysGameOfLife_v6/turingmaschineV2.txt",
            "../turingmaschineV2.txt"
        });
    }

    /**
     * Turing Machine (v3) loaded from "turingmaschineV3.txt".
     */
    public static Pattern turingMachineV3() {
        return loadTuringMachine("Turing Machine v3 (computer)", new String[]{
            "turingmaschineV3.txt",
            "ConwaysGameOfLife_v8/turingmaschineV3.txt",
            "ConwaysGameOfLife_v7/turingmaschineV3.txt",
            "ConwaysGameOfLife_v6/turingmaschineV3.txt",
            "../turingmaschineV3.txt"
        });
    }

    /**
     * Large V1 clock computer pattern loaded from "clockV1.txt".
     */
    public static Pattern clockV1() {
        String[] candidates = {
            "clockV1.txt",
            "ConwaysGameOfLife_v9/clockV1.txt",
            "ConwaysGameOfLife_v8/clockV1.txt",
            "../clockV1.txt"
        };
        for (String path : candidates) {
            File f = new File(path);
            if (!f.exists()) continue;
            try {
                return fromRleFile("Clock v1 (computer)", f);
            } catch (IOException e) {
                // try next candidate
            }
        }
        return null;
    }

    /**
     * Backward-compatible alias that returns the v2 machine.
     */
    public static Pattern turingMachine() {
        return turingMachineV2();
    }

    private static Pattern loadTuringMachine(String displayName, String[] candidates) {
        return loadTuringMachine(displayName, false, candidates);
    }

    private static Pattern collisionStoryboard(
            String name,
            int leftSignalX,
            int rightSignalX,
            boolean includeLeftSignal,
            boolean includeRightSignal,
            String stepLabel,
            String noteLabel) {
        List<int[]> coords = new ArrayList<>();
        int[][] lwss = lwss().getCoordinates();

        if (includeLeftSignal) {
            addCells(coords, lwss, leftSignalX, 8, false);
        }
        if (includeRightSignal) {
            addCells(coords, lwss, rightSignalX, 8, true);
        }

        String[] labels = new String[]{
            stepLabel,
            "INPUT A lane",
            "INPUT B lane",
            "COLLISION GATE",
            noteLabel
        };
        int[][] boxes = new int[][]{
            { 2,  1, 58, 4},
            { 2,  7, 16, 8},
            {44,  7, 16, 8},
            {25,  7, 12, 9},
            {18, 17, 26, 4}
        };

        return new Pattern(name, coords.toArray(new int[0][]), labels, boxes);
    }

    private static Pattern loadTuringMachine(String displayName, boolean detailedV2Blueprint, String[] candidates) {
        for (String path : candidates) {
            File f = new File(path);
            if (!f.exists()) continue;
            try {
                Pattern base = fromRleFile(displayName, f);
                int[] bounds = base.getBounds();
                int W = bounds[0], H = bounds[1];
                String[] labels;
                int[][] boxes;
                if (detailedV2Blueprint) {
                    labels = new String[]{
                        "INPUT / INITIAL TAPE",
                        "BOOTSTRAP",
                        "CLOCK BACKBONE",
                        "SIGNAL BUS",
                        "CONTROL MATRIX",
                        "READ LOGIC",
                        "WRITE LOGIC",
                        "STATE MEMORY",
                        "HEAD MOVER X",
                        "HEAD MOVER Y",
                        "OUTPUT TAPE",
                        "CLEANUP / EATERS"
                    };
                    boxes = new int[][]{
                        { W * 2 / 100,  H * 2  / 100, W * 16 / 100, H * 12 / 100 },
                        { W * 16 / 100, H * 10 / 100, W * 14 / 100, H * 14 / 100 },
                        { W * 24 / 100, H * 4  / 100, W * 44 / 100, H * 10 / 100 },
                        { W * 28 / 100, H * 15 / 100, W * 46 / 100, H * 13 / 100 },
                        { W * 46 / 100, H * 28 / 100, W * 24 / 100, H * 18 / 100 },
                        { W * 32 / 100, H * 31 / 100, W * 14 / 100, H * 13 / 100 },
                        { W * 57 / 100, H * 32 / 100, W * 14 / 100, H * 13 / 100 },
                        { W * 40 / 100, H * 48 / 100, W * 25 / 100, H * 12 / 100 },
                        { W * 61 / 100, H * 52 / 100, W * 16 / 100, H * 12 / 100 },
                        { W * 56 / 100, H * 66 / 100, W * 17 / 100, H * 15 / 100 },
                        { W * 74 / 100, H * 78 / 100, W * 22 / 100, H * 18 / 100 },
                        { W * 80 / 100, H * 56 / 100, W * 16 / 100, H * 14 / 100 }
                    };
                } else {
                    labels = new String[]{ "INPUT", "OUTPUT" };
                    boxes   = new int[][]{
                        { 0,          0,          W / 5,     H / 5 },
                        { W * 4 / 5,  H * 4 / 5, W / 5,     H / 5 }
                    };
                }
                return new Pattern(base.getName(), base.getCoordinates(), labels, boxes);
            } catch (IOException e) {
                // try next candidate
            }
        }
        return null;
    }

    private static void addCells(List<int[]> target, int[][] src, int offX, int offY, boolean mirrorX) {
        int maxX = 0;
        for (int[] c : src) {
            if (c[0] > maxX) maxX = c[0];
        }
        for (int[] c : src) {
            int x = mirrorX ? (maxX - c[0]) : c[0];
            target.add(new int[]{x + offX, c[1] + offY});
        }
    }

    /**
     * Load a pattern from an RLE-formatted file.
     * Supports standard RLE and the LifeHistory multi-state extension:
     * any uppercase letter is treated as an alive cell.
     *
     * @param name display name for the pattern
     * @param file the RLE file to read
     * @return the parsed Pattern
     * @throws IOException if the file cannot be read
     */
    public static Pattern fromRleFile(String name, File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean inData = false;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) continue;
                if (!inData && line.startsWith("x")) { inData = true; continue; }
                if (inData) sb.append(line.trim());
            }
        }
        return fromRle(name, sb.toString());
    }

    /**
     * Parse an RLE-encoded cell string (without header) into a Pattern.
     * Supports classic RLE ('b'/'B' dead, 'o'/'O' alive), dot-dead variants ('.'),
     * and LifeHistory-style letters where any letter except 'b'/'B' is treated as
     * alive. '$' ends a row; '!' ends the pattern; digits are run-length
     * prefixes.
     */
    public static Pattern fromRle(String name, String rle) {
        List<int[]> coords = new ArrayList<>();
        int x = 0, y = 0;
        int runLen = 0;
        for (int i = 0; i < rle.length(); i++) {
            char ch = rle.charAt(i);
            if (ch >= '0' && ch <= '9') {
                runLen = runLen * 10 + (ch - '0');
            } else if (ch == '.' || ch == 'b' || ch == 'B') {
                x += (runLen == 0 ? 1 : runLen);
                runLen = 0;
            } else if (ch == 'o' || ch == 'O'
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= 'a' && ch <= 'z')) {
                int count = (runLen == 0 ? 1 : runLen);
                for (int k = 0; k < count; k++) {
                    coords.add(new int[]{x, y});
                    x++;
                }
                runLen = 0;
            } else if (ch == '$') {
                int count = (runLen == 0 ? 1 : runLen);
                y += count;
                x = 0;
                runLen = 0;
            } else if (ch == '!') {
                break;
            }
        }
        return new Pattern(name, coords.toArray(new int[0][]));
    }
}

import java.util.*;

/**
 * Sparse Conway's Game of Life board using a HashSet of encoded cell coordinates.
 * The board is effectively infinite: cells can exist at any integer (x, y) coordinate.
 * Only alive cells are stored, making large empty regions free.
 */
public class GameOfLifeBoard {
    private Set<Long> cells;
    private int generation;

    public GameOfLifeBoard() {
        cells = new HashSet<>();
        generation = 0;
    }

    /** Encode (x, y) as a single long. Works for any int x, y. */
    public static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    /** Decode x-coordinate from a key. */
    public static int keyX(long k) {
        return (int)(k >> 32);
    }

    /** Decode y-coordinate from a key. */
    public static int keyY(long k) {
        return (int)(k);
    }

    public int getGeneration() { return generation; }

    public boolean getCell(int x, int y) {
        return cells.contains(key(x, y));
    }

    public void setCell(int x, int y, boolean alive) {
        if (alive) cells.add(key(x, y));
        else cells.remove(key(x, y));
    }

    /** Returns a read-only view of all alive cell keys. */
    public Set<Long> getAliveCells() {
        return Collections.unmodifiableSet(cells);
    }

    /** Number of alive cells. */
    public int countAlive() { return cells.size(); }

    /** Clear all cells and reset the generation counter. */
    public void clear() {
        cells.clear();
        generation = 0;
    }

    /**
     * Randomize cells in a region of the given size, centered on (0, 0).
     * Approximately 30 % of cells in the region will start alive.
     */
    public void randomize(int width, int height) {
        cells.clear();
        Random rng = new Random();
        int ox = -width / 2;
        int oy = -height / 2;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                if (rng.nextDouble() < 0.3)
                    cells.add(key(ox + x, oy + y));
        generation = 0;
    }

    /**
     * Advance the board by one generation using the standard GoL rules:
     *   - A live cell with 2 or 3 neighbours survives.
     *   - A dead cell with exactly 3 neighbours becomes alive.
     *   - All other cells die or stay dead.
     *
     * The algorithm is O(alive cells): for each live cell we increment a
     * neighbour-count for all eight surrounding positions, then apply the rules.
     */
    public void nextGeneration() {
        Map<Long, Integer> counts = new HashMap<>();
        for (long ck : cells) {
            int cx = keyX(ck);
            int cy = keyY(ck);
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    long nk = key(cx + dx, cy + dy);
                    counts.merge(nk, 1, Integer::sum);
                }
            }
        }
        Set<Long> next = new HashSet<>();
        for (Map.Entry<Long, Integer> e : counts.entrySet()) {
            int n = e.getValue();
            long k = e.getKey();
            boolean alive = cells.contains(k);
            if (alive ? (n == 2 || n == 3) : (n == 3))
                next.add(k);
        }
        cells = next;
        generation++;
    }

    /** Place a pattern with its top-left corner at (offsetX, offsetY). */
    public void setCellPattern(Pattern p, int offsetX, int offsetY) {
        for (int[] coord : p.getCoordinates())
            setCell(coord[0] + offsetX, coord[1] + offsetY, true);
    }
}

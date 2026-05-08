import java.util.*;

/**
 * Sparse Conway's Game of Life board using a HashSet of encoded cell coordinates.
 * The board is effectively infinite: cells can exist at any integer (x, y) coordinate.
 * Only alive cells are stored, making large empty regions free.
 *
 * New in v5:
 *  - nextGeneration() automatically switches to a parallel implementation
 *    (Java parallel streams) when the alive-cell count exceeds PARALLEL_THRESHOLD.
 *    Both the neighbour-counting phase and the rule-application phase are
 *    parallelised, giving roughly a linear speedup with core count on large boards.
 *  - Better initial HashMap capacity to reduce resize overhead.
 *  - generation is declared volatile so the display thread can read it cheaply
 *    without holding the board lock.
 */
public class GameOfLifeBoard {

    /** Switch to parallel computation above this many alive cells. */
    private static final int PARALLEL_THRESHOLD = 5_000;

    private Set<Long> cells;
    /** volatile so the display / measurement thread can read without locking. */
    private volatile int generation;

    public GameOfLifeBoard() {
        cells = new HashSet<>();
        generation = 0;
    }

    // ── Coordinate encoding ───────────────────────────────────────────────────

    /** Encode (x, y) as a single long. Works for any int x, y. */
    public static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    /** Decode x-coordinate from a key. */
    public static int keyX(long k) { return (int)(k >> 32); }

    /** Decode y-coordinate from a key. */
    public static int keyY(long k) { return (int)(k); }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public int getGeneration() { return generation; }

    public boolean getCell(int x, int y) { return cells.contains(key(x, y)); }

    public void setCell(int x, int y, boolean alive) {
        if (alive) cells.add(key(x, y));
        else       cells.remove(key(x, y));
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
     * Randomize cells in a region of the given size, centred on (0, 0).
     * Approximately 30 % of cells in the region will start alive.
     */
    public void randomize(int width, int height) {
        cells.clear();
        Random rng = new Random();
        int ox = -width / 2, oy = -height / 2;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                if (rng.nextDouble() < 0.3)
                    cells.add(key(ox + x, oy + y));
        generation = 0;
    }

    // ── Simulation ────────────────────────────────────────────────────────────

    /**
     * Advance the board by one generation using the standard GoL rules:
     *   - A live cell with 2 or 3 neighbours survives.
     *   - A dead cell with exactly 3 neighbours becomes alive.
     *   - All other cells die or stay dead.
     *
     * For small boards (< PARALLEL_THRESHOLD alive cells) a tight sequential loop
     * is used.  For larger boards both the neighbour-counting phase and the
     * rule-application phase run in parallel via Java parallel streams, giving
     * roughly a linear speedup with the number of CPU cores.
     */
    public void nextGeneration() {
        final Set<Long> snap = cells;
        final int sz = snap.size();

        final Map<Long, Integer> counts;
        final Set<Long> next;

        if (sz < PARALLEL_THRESHOLD) {
            // ── Sequential path (small boards) ──────────────────────────────

            // Size the map so it rarely needs to resize:
            // each of sz alive cells contributes up to 8 neighbour increments.
            counts = new HashMap<>(sz * 10 + 16);
            for (long ck : snap) {
                int cx = keyX(ck), cy = keyY(ck);
                for (int dy = -1; dy <= 1; dy++)
                    for (int dx = -1; dx <= 1; dx++)
                        if (dx != 0 || dy != 0)
                            counts.merge(key(cx + dx, cy + dy), 1, Integer::sum);
            }

            next = new HashSet<>((int)(counts.size() / 0.75f) + 1);
            for (Map.Entry<Long, Integer> e : counts.entrySet()) {
                int n = e.getValue(); long k = e.getKey();
                if (n == 3 || (n == 2 && snap.contains(k))) next.add(k);
            }

        } else {
            // ── Parallel path (large boards) ────────────────────────────────
            //
            // Phase 1 – neighbour counting.
            // Each parallel segment accumulates into a thread-local HashMap;
            // the combiner merges two HashMaps in a tree-reduction pattern so
            // that no lock is needed.
            counts = snap.parallelStream().collect(
                () -> new HashMap<Long, Integer>(),
                (map, ck) -> {
                    int cx = keyX(ck), cy = keyY(ck);
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dx = -1; dx <= 1; dx++)
                            if (dx != 0 || dy != 0)
                                map.merge(key(cx + dx, cy + dy), 1, Integer::sum);
                },
                (m1, m2) -> m2.forEach((k, v) -> m1.merge(k, v, Integer::sum))
            );

            // Phase 2 – rule application.
            // Each segment filters and collects its surviving keys into a
            // thread-local HashSet; the combiner merges with addAll.
            next = counts.entrySet().parallelStream()
                .filter(e -> {
                    int n = e.getValue(); long k = e.getKey();
                    return n == 3 || (n == 2 && snap.contains(k));
                })
                .map(Map.Entry::getKey)
                .collect(
                    () -> new HashSet<Long>(),
                    HashSet::add,
                    HashSet::addAll
                );
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

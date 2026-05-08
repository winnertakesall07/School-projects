import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Conway's Game of Life GUI (v9).
 *
 * What is new in v6 compared with v5:
 *
 * 1. Performance — all alive cells are always processed, none skipped.
 *    - nextGeneration() now runs in parallel for boards with ≥ 5 000 alive
 *      cells (see GameOfLifeBoard), giving roughly a linear speedup with core
 *      count on large boards.
 *    - Simulation runs in a dedicated daemon thread ("GoL-Sim"), completely
 *      decoupled from the Swing EDT.  The simulation can spin at full CPU speed
 *      without being gated by repaint or GUI events.
 *    - Rendering uses a separate 30-FPS display timer.  The display thread
 *      grabs a snapshot of the board under a lock and renders from the immutable
 *      snapshot array, so the sim thread is never blocked by painting.
 *
 * 2. Much higher achievable speed.
 *    - The speed slider now covers 1–1 000.
 *      · 1–200  → 1–200 gen/s  (linear, same feel as before)
 *      · 200–999 → 200–~2 000 000 gen/s  (exponential: 200 × 10^((v−200)/200))
 *      · 1 000  → unlimited (MAX) — the sim thread runs in a tight loop with
 *        only Thread.yield() between generations.
 *    - A measured "Actual" gen/s counter in the status bar shows real throughput.
 *
 * Features carried over from v7:
 *  - Infinite sparse board, pan (RMB-drag), scroll-wheel zoom (0.05–64 px/cell).
 *  - Pattern placement with ghost preview (18 patterns + 3 Turing Machines).
 *  - Sub-pixel pixel-buffer rendering when zoomed out below 1 px/cell.
 *  - Glow effect when zoomed in ≥ 8 px/cell.
 *  - Turing Machine region overlays.
 *
 * New in v9:
 *  - Adds Clock v1 computer pattern support.
 *  - Reduces snapshot/render overhead for very large patterns.
 */
public class GameOfLifeGUI extends JFrame {

    // ── Model ─────────────────────────────────────────────────────────────────
    private final GameOfLifeBoard board;
    private GridPanel gridPanel;

    // ── Thread-safety ─────────────────────────────────────────────────────────
    /**
     * All board mutations and reads that could race with the sim thread are
     * guarded by this lock:
     *   - nextGeneration()  (sim thread)
     *   - setCell() / setCellPattern() / clear() / randomize()  (EDT)
     *   - snapshot capture for display  (EDT display timer)
     */
    private final Object boardLock = new Object();

    // ── Simulation thread ─────────────────────────────────────────────────────
    private Thread  simThread;
    private volatile boolean simRunning    = false;
    /** Nanoseconds between generations; 0 means unlimited. */
    private volatile long    targetNsPerGen = 100_000_000L; // default ≈ 10 gen/s
    private volatile boolean unlimitedMode = false;

    // ── Display (30 FPS, always running) ──────────────────────────────────────
    private Timer  displayTimer;
    /** Snapshot captured under boardLock, rendered without the lock. */
    private Set<Long> currentSnapshot = Collections.emptySet();
    private int    currentGeneration = 0;
    private int    currentPopulation = 0;

    // ── Measured gen/s ────────────────────────────────────────────────────────
    private long measureStartMs  = System.currentTimeMillis();
    private long measureStartGen = 0;
    private long measuredGenPerSec = 0;

    // ── Viewport ──────────────────────────────────────────────────────────────
    /** Pixels per board cell (floating-point for smooth zoom). */
    private double cellSize = 12.0;
    private double viewX    = -40.0;
    private double viewY    = -30.0;

    private static final double MIN_CELL_SIZE = 0.05;
    private static final double MAX_CELL_SIZE = 64.0;
    private static final double ZOOM_FACTOR   = 1.15;

    // ── Pattern placement ─────────────────────────────────────────────────────
    private Pattern pendingPattern = null;
    private boolean blueprintMode = true;

    // ── Status labels ─────────────────────────────────────────────────────────
    private JLabel generationLabel;
    private JLabel populationLabel;
    private JLabel zoomLabel;
    private JLabel speedLabel;
    private JLabel rateLabel;
    private JLabel placingLabel;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color COLOR_ALIVE         = new Color(0,  230,  64);
    private static final Color COLOR_GLOW          = new Color(100, 255, 100, 60);
    private static final Color COLOR_DEAD          = new Color(15,   15,  20);
    private static final Color COLOR_GRID          = new Color(30,   30,  40);
    private static final Color COLOR_BG            = new Color(10,   10,  15);
    private static final Color COLOR_TOOLBAR       = new Color(20,   20,  30);
    private static final Color COLOR_TEXT          = new Color(0,   255,  65);
    private static final Color COLOR_BUTTON        = new Color(30,   60,  30);
    private static final Color COLOR_BUTTON_BORDER = new Color(0,   200,  50);
    private static final Color COLOR_GHOST         = new Color(0,   180, 255, 120);
    private static final Color COLOR_PLACING_TEXT  = new Color(255, 200,   0);
    private static final Color COLOR_RATE          = new Color(0,   200, 200);

    private static final Color[] REGION_FILL   = { new Color(255,  80,  80, 55), new Color( 80, 160, 255, 55) };
    private static final Color[] REGION_BORDER = { new Color(255, 120, 120),     new Color(100, 180, 255) };

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public GameOfLifeGUI() {
        board = new GameOfLifeBoard();
        initUI();

        // Snapshot the initial board so the display is populated immediately.
        takeSnapshot();

        // Display timer: always running, 30 FPS.
        displayTimer = new Timer(33, e -> onDisplayTick());
        displayTimer.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI construction
    // ─────────────────────────────────────────────────────────────────────────

    private void initUI() {
        setTitle("Conway's Game of Life  v9");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(COLOR_BG);
        getContentPane().setBackground(COLOR_BG);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(COLOR_BG);

        gridPanel = new GridPanel();
        mainPanel.add(gridPanel,        BorderLayout.CENTER);
        mainPanel.add(createToolbar(),   BorderLayout.NORTH);
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(COLOR_TOOLBAR);
        tabs.setForeground(COLOR_TEXT);
        tabs.setFont(new Font("Monospaced", Font.BOLD, 11));
        tabs.addTab("Simulation", mainPanel);
        tabs.addTab("Guidebook", createGuidebookTab());

        add(tabs);
        setMinimumSize(new Dimension(800, 560));
        pack();
        setLocationRelativeTo(null);
    }

    private JComponent createGuidebookTab() {
        JTextArea text = new JTextArea(buildGuidebookText());
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setCaretPosition(0);
        text.setBackground(new Color(8, 12, 10));
        text.setForeground(new Color(180, 255, 180));
        text.setFont(new Font("Monospaced", Font.PLAIN, 13));
        text.setMargin(new Insets(12, 12, 12, 12));

        JScrollPane pane = new JScrollPane(text);
        pane.getViewport().setBackground(new Color(8, 12, 10));
        pane.setBorder(BorderFactory.createEmptyBorder());
        return pane;
    }

    private String buildGuidebookText() {
        return
            "V9 GUIDEBOOK — V2 TURING-COMPLETE COMPUTER\n\n" +
            "What the V2 computer is\n" +
            "The V2 pattern is a very large Conway's Game of Life construction. " +
            "It is Turing-complete, meaning it can perform universal computation " +
            "when the correct initial signals are provided.\n\n" +
            "How it works at a high level\n" +
            "1) The INPUT / INITIAL TAPE area encodes starting data.\n" +
            "2) BOOTSTRAP and CLOCK BACKBONE create synchronized timing pulses.\n" +
            "3) The SIGNAL BUS carries pulses between computing regions.\n" +
            "4) CONTROL MATRIX routes pulses depending on current machine state.\n" +
            "5) READ LOGIC and WRITE LOGIC implement tape read/write behavior.\n" +
            "6) STATE MEMORY keeps the active state encoded in repeating loops.\n" +
            "7) HEAD MOVER X and HEAD MOVER Y propagate movement commands.\n" +
            "8) OUTPUT TAPE collects resulting encoded signals.\n" +
            "9) CLEANUP / EATERS absorb spent gliders/debris to keep the machine stable.\n\n" +
            "Blueprint mode in V8\n" +
            "When placing the Turing Machine v2 pattern, Blueprint mode overlays and labels all major parts.\n" +
            "Use the toolbar button 'Blueprint: ON/OFF' or press the B key to toggle this.\n\n" +
            "Practical usage\n" +
            "- Select 'Turing Machine v2 (computer)'.\n" +
            "- Press 'Place ▸' and move the mouse to preview.\n" +
            "- Keep Blueprint ON while studying regions.\n" +
            "- Left-click to stamp the machine.\n";
    }

    private JPanel createToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 5));
        bar.setBackground(COLOR_TOOLBAR);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BUTTON_BORDER));

        // ── Simulation controls ───────────────────────────────────────────────
        JButton startBtn  = makeButton("▶ Start");
        JButton stopBtn   = makeButton("⏸ Stop");
        JButton stepBtn   = makeButton("⏭ Step");
        JButton resetBtn  = makeButton("↺ Reset");
        JButton randomBtn = makeButton("⚡ Random");

        startBtn .addActionListener(e -> start());
        stopBtn  .addActionListener(e -> stop());
        stepBtn  .addActionListener(e -> step());
        resetBtn .addActionListener(e -> reset());
        randomBtn.addActionListener(e -> {
            stop();
            synchronized(boardLock) { board.randomize(80, 60); }
            takeSnapshot();
            gridPanel.repaint();
            updateStatus();
        });

        bar.add(startBtn); bar.add(stopBtn); bar.add(stepBtn);
        bar.add(resetBtn); bar.add(randomBtn);
        bar.add(separator());

        // ── Speed slider ──────────────────────────────────────────────────────
        //
        // Range 1–1000.
        //   1–200  : linear 1–200 gen/s
        //   201–999: exponential 200 × 10^((v−200)/200)  ≈ 200 to ~2 000 000 gen/s
        //   1000   : unlimited (MAX) — sim thread runs as fast as possible
        bar.add(makeLabel("Speed:"));

        JLabel speedValLabel = makeLabel("10/s ");
        speedValLabel.setForeground(COLOR_RATE);

        JSlider speedSlider = new JSlider(1, 1000, 10);
        speedSlider.setBackground(COLOR_TOOLBAR);
        speedSlider.setForeground(COLOR_TEXT);
        speedSlider.setPreferredSize(new Dimension(160, 28));
        speedSlider.addChangeListener(e -> {
            int v = speedSlider.getValue();
            if (v >= 1000) {
                unlimitedMode  = true;
                targetNsPerGen = 0;
                speedValLabel.setText("MAX ");
            } else {
                unlimitedMode = false;
                long genPerSec = sliderToGenPerSec(v);
                targetNsPerGen = 1_000_000_000L / Math.max(1L, genPerSec);
                speedValLabel.setText(formatGenPerSec(genPerSec) + " ");
            }
        });
        bar.add(speedSlider);
        bar.add(speedValLabel);
        bar.add(separator());

        // ── Zoom controls ─────────────────────────────────────────────────────
        JButton zoomInBtn  = makeButton("🔍+");
        JButton zoomOutBtn = makeButton("🔍-");
        JButton centerBtn  = makeButton("⌂ Center");
        zoomInBtn .addActionListener(e -> zoom(ZOOM_FACTOR,       gridPanel.getWidth()  / 2.0, gridPanel.getHeight() / 2.0));
        zoomOutBtn.addActionListener(e -> zoom(1.0 / ZOOM_FACTOR, gridPanel.getWidth()  / 2.0, gridPanel.getHeight() / 2.0));
        centerBtn .addActionListener(e -> centerView());
        bar.add(zoomInBtn); bar.add(zoomOutBtn); bar.add(centerBtn);
        bar.add(separator());

        // ── Pattern picker ────────────────────────────────────────────────────
        bar.add(makeLabel("Pattern:"));

        Pattern[] patterns = buildPatternList();
        JComboBox<Pattern> patternBox = new JComboBox<>(patterns);
        patternBox.setBackground(COLOR_BUTTON);
        patternBox.setForeground(COLOR_TEXT);
        patternBox.setFont(new Font("Monospaced", Font.PLAIN, 11));
        patternBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? COLOR_BUTTON_BORDER : COLOR_BUTTON);
                setForeground(COLOR_TEXT);
                setFont(new Font("Monospaced", Font.PLAIN, 11));
                return this;
            }
        });

        JButton placeBtn  = makeButton("Place ▸");
        JButton cancelBtn = makeButton("✕ Cancel");
        JButton blueprintBtn = makeButton("Blueprint: ON");
        cancelBtn.setEnabled(false);

        placeBtn.addActionListener(e -> {
            Pattern p = (Pattern) patternBox.getSelectedItem();
            if (p != null) {
                pendingPattern = p;
                cancelBtn.setEnabled(true);
                gridPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                updateStatus();
                gridPanel.repaint();
            }
        });
        cancelBtn.addActionListener(e -> {
            cancelPlacement();
            cancelBtn.setEnabled(false);
        });
        blueprintBtn.addActionListener(e -> {
            blueprintMode = !blueprintMode;
            blueprintBtn.setText("Blueprint: " + (blueprintMode ? "ON" : "OFF"));
            gridPanel.repaint();
        });

        bar.add(patternBox);
        bar.add(placeBtn);
        bar.add(cancelBtn);
        bar.add(blueprintBtn);

        gridPanel.putClientProperty("cancelBtn", cancelBtn);
        return bar;
    }

    private Pattern[] buildPatternList() {
        java.util.List<Pattern> list = new java.util.ArrayList<>();
        list.add(Pattern.block());       list.add(Pattern.beehive());
        list.add(Pattern.loaf());        list.add(Pattern.boat());
        list.add(Pattern.tub());
        list.add(Pattern.blinker());     list.add(Pattern.toad());
        list.add(Pattern.clock());       list.add(Pattern.beacon());
        list.add(Pattern.pulsar());      list.add(Pattern.figureEight());
        list.add(Pattern.pentadecathlon());
        list.add(Pattern.glider());      list.add(Pattern.lwss());
        list.add(Pattern.gosperGliderGun());
        list.add(Pattern.rPentomino()); list.add(Pattern.diehard());
        list.add(Pattern.acorn());
        Pattern tmV1 = Pattern.turingMachineV1();
        Pattern tmV2 = Pattern.turingMachineV2();
        Pattern tmV3 = Pattern.turingMachineV3();
        Pattern clkV1 = Pattern.clockV1();
        if (tmV1 != null) list.add(tmV1);
        if (tmV2 != null) list.add(tmV2);
        if (tmV3 != null) list.add(tmV3);
        if (clkV1 != null) list.add(clkV1);
        return list.toArray(new Pattern[0]);
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 3));
        bar.setBackground(COLOR_TOOLBAR);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BUTTON_BORDER));

        generationLabel = makeLabel("Gen: 0");
        populationLabel = makeLabel("Pop: 0");
        zoomLabel       = makeLabel("Zoom: 12px/cell");
        speedLabel      = makeLabel("Target: 10/s");
        speedLabel.setForeground(new Color(180, 255, 180));
        rateLabel       = makeLabel("Actual: —");
        rateLabel.setForeground(COLOR_RATE);
        placingLabel    = makeLabel("");
        placingLabel.setForeground(COLOR_PLACING_TEXT);

        JLabel hint = makeLabel("| RMB-drag: pan  |  Wheel: zoom  |  LMB: draw  |  B: blueprint");
        hint.setForeground(new Color(0, 130, 40));

        bar.add(generationLabel); bar.add(populationLabel);
        bar.add(zoomLabel);       bar.add(speedLabel);
        bar.add(rateLabel);       bar.add(hint);
        bar.add(placingLabel);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Speed helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static long sliderToGenPerSec(int v) {
        if (v <= 200) return v;
        return (long)(200.0 * Math.pow(10.0, (v - 200.0) / 200.0));
    }

    private static String formatGenPerSec(long gps) {
        if (gps >= 1_000_000) return String.format("%.1fM/s", gps / 1_000_000.0);
        if (gps >= 1_000)     return String.format("%.1fk/s", gps / 1_000.0);
        return gps + "/s";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Simulation commands
    // ─────────────────────────────────────────────────────────────────────────

    private void start() {
        if (simRunning) return;
        simRunning = true;
        simThread  = new Thread(this::simulationLoop, "GoL-Sim");
        simThread.setDaemon(true);
        simThread.start();
    }

    private void stop() {
        simRunning = false;
        if (simThread != null) {
            simThread.interrupt();
            simThread = null;
        }
    }

    /**
     * The simulation loop runs in a dedicated daemon thread.
     * It calls nextGeneration() under boardLock, then sleeps the appropriate
     * amount of time to hit the target gen/s (or yields if in unlimited mode).
     * The display timer on the EDT reads the board independently at 30 FPS.
     */
    private void simulationLoop() {
        long nextTargetNs = System.nanoTime();
        while (simRunning) {
            synchronized (boardLock) {
                board.nextGeneration();
            }
            if (unlimitedMode) {
                Thread.yield();
            } else {
                nextTargetNs += targetNsPerGen;
                long sleepNs = nextTargetNs - System.nanoTime();
                if (sleepNs >= 1_000_000L) {
                    try {
                        Thread.sleep(sleepNs / 1_000_000L,
                                     (int)(sleepNs % 1_000_000L));
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else if (sleepNs < -5_000_000_000L) {
                    // We fell more than 5 s behind; reset the clock to
                    // prevent a runaway catch-up spiral.
                    nextTargetNs = System.nanoTime();
                }
            }
        }
    }

    /** Called every 33 ms by the display timer (on the EDT). */
    private void onDisplayTick() {
        takeSnapshot();
        // Update measured gen/s once per second
        long nowMs  = System.currentTimeMillis();
        long elapsed = nowMs - measureStartMs;
        if (elapsed >= 1000) {
            long curGen = currentGeneration;
            measuredGenPerSec = (curGen - measureStartGen) * 1000L / elapsed;
            measureStartMs    = nowMs;
            measureStartGen   = curGen;
        }
        gridPanel.repaint();
        updateStatus();
    }

    /**
     * Captures the current board state into the snapshot fields used by the
     * display.  Must be called on the EDT (or with external synchronisation).
     * Acquires boardLock briefly so the sim thread cannot modify the board
     * while we copy the cell set.
     */
    private void takeSnapshot() {
        synchronized (boardLock) {
            Set<Long> alive = board.getAliveCells();
            currentSnapshot = alive;
            currentGeneration = board.getGeneration();
            currentPopulation = alive.size();
        }
    }

    private void step() {
        synchronized (boardLock) { board.nextGeneration(); }
        takeSnapshot();
        gridPanel.repaint();
        updateStatus();
    }

    private void reset() {
        stop();
        synchronized (boardLock) { board.clear(); }
        cancelPlacement();
        currentSnapshot = Collections.emptySet();
        currentGeneration = 0;
        currentPopulation = 0;
        gridPanel.repaint();
        updateStatus();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Viewport helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void zoom(double factor, double mx, double my) {
        double boardMX = viewX + mx / cellSize;
        double boardMY = viewY + my / cellSize;
        cellSize = Math.min(MAX_CELL_SIZE, Math.max(MIN_CELL_SIZE, cellSize * factor));
        viewX = boardMX - mx / cellSize;
        viewY = boardMY - my / cellSize;
        updateStatus();
        gridPanel.repaint();
    }

    /** Pan so the alive-cell bounding box is centred in the viewport. */
    private void centerView() {
        long[] snap;
        synchronized (boardLock) {
            Set<Long> alive = board.getAliveCells();
            snap = new long[alive.size()];
            int i = 0;
            for (long k : alive) snap[i++] = k;
        }
        double cx = 0, cy = 0;
        if (snap.length > 0) {
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            for (long k : snap) {
                int bx = GameOfLifeBoard.keyX(k), by = GameOfLifeBoard.keyY(k);
                if (bx < minX) minX = bx; if (bx > maxX) maxX = bx;
                if (by < minY) minY = by; if (by > maxY) maxY = by;
            }
            cx = (minX + maxX) / 2.0;
            cy = (minY + maxY) / 2.0;
        }
        viewX = cx - gridPanel.getWidth()  / 2.0 / cellSize;
        viewY = cy - gridPanel.getHeight() / 2.0 / cellSize;
        updateStatus();
        gridPanel.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pattern placement
    // ─────────────────────────────────────────────────────────────────────────

    private void cancelPlacement() {
        pendingPattern = null;
        gridPanel.setCursor(Cursor.getDefaultCursor());
        Object cancelBtn = gridPanel.getClientProperty("cancelBtn");
        if (cancelBtn instanceof JButton) ((JButton) cancelBtn).setEnabled(false);
        updateStatus();
        gridPanel.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Status bar
    // ─────────────────────────────────────────────────────────────────────────

    private void updateStatus() {
        generationLabel.setText("Gen: " + currentGeneration);
        populationLabel.setText("Pop: " + currentPopulation);

        if (cellSize >= 10)
            zoomLabel.setText(String.format("Zoom: %.0fpx/cell", cellSize));
        else
            zoomLabel.setText(String.format("Zoom: %.1fpx/cell", cellSize));

        if (unlimitedMode) {
            speedLabel.setText("Target: MAX");
        } else {
            long gps = targetNsPerGen > 0 ? 1_000_000_000L / targetNsPerGen : 0;
            speedLabel.setText("Target: " + formatGenPerSec(gps));
        }

        if (simRunning && measuredGenPerSec > 0) {
            rateLabel.setText("Actual: " + formatGenPerSec(measuredGenPerSec));
        } else if (!simRunning) {
            rateLabel.setText("Actual: —");
        }

        if (pendingPattern != null) {
            placingLabel.setText(
                "✦ PLACING: " + pendingPattern.getName() +
                "  —  left-click to place,  Esc to cancel");
        } else {
            placingLabel.setText("");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Widget factory helpers
    // ─────────────────────────────────────────────────────────────────────────

    private JSeparator separator() {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 24));
        sep.setForeground(COLOR_BUTTON_BORDER);
        return sep;
    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(COLOR_BUTTON);
        btn.setForeground(COLOR_TEXT);
        btn.setBorder(BorderFactory.createLineBorder(COLOR_BUTTON_BORDER, 1));
        btn.setFocusPainted(false);
        btn.setFont(new Font("Monospaced", Font.BOLD, 11));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(new Color(40, 90, 40));
            }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(COLOR_BUTTON); }
        });
        return btn;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(COLOR_TEXT);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        return lbl;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GridPanel — canvas that renders and handles interaction
    // ─────────────────────────────────────────────────────────────────────────

    private class GridPanel extends JPanel {

        private boolean painting   = false;
        private boolean paintValue = true;
        private boolean panning    = false;
        private int     lastPanX, lastPanY;
        private int     ghostBX   = 0;
        private int     ghostBY   = 0;
        private boolean showGhost = false;

        public GridPanel() {
            setBackground(COLOR_BG);
            setFocusable(true);
            setPreferredSize(new Dimension(960, 680));

            MouseAdapter ma = new MouseAdapter() {

                @Override public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    if (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                        panning  = true;
                        lastPanX = e.getX();
                        lastPanY = e.getY();
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (pendingPattern != null) {
                            // Stamp pattern
                            int bx = screenToBoardX(e.getX()), by = screenToBoardY(e.getY());
                            int[] bounds = pendingPattern.getBounds();
                            int offX = bx - bounds[0] / 2, offY = by - bounds[1] / 2;
                            synchronized (boardLock) {
                                board.setCellPattern(pendingPattern, offX, offY);
                            }
                            takeSnapshot();
                            cancelPlacement();
                        } else {
                            int bx = screenToBoardX(e.getX()), by = screenToBoardY(e.getY());
                            synchronized (boardLock) {
                                paintValue = !board.getCell(bx, by);
                                painting   = true;
                                board.setCell(bx, by, paintValue);
                            }
                            takeSnapshot();
                            repaint();
                            updateStatus();
                        }
                    }
                }

                @Override public void mouseDragged(MouseEvent e) {
                    if (panning) {
                        viewX -= (e.getX() - lastPanX) / cellSize;
                        viewY -= (e.getY() - lastPanY) / cellSize;
                        lastPanX = e.getX();
                        lastPanY = e.getY();
                        updateStatus();
                        repaint();
                    } else if (painting && pendingPattern == null) {
                        int bx = screenToBoardX(e.getX()), by = screenToBoardY(e.getY());
                        synchronized (boardLock) { board.setCell(bx, by, paintValue); }
                        takeSnapshot();
                        repaint();
                        updateStatus();
                    }
                }

                @Override public void mouseReleased(MouseEvent e) {
                    panning  = false;
                    painting = false;
                }

                @Override public void mouseMoved(MouseEvent e) {
                    if (pendingPattern != null) {
                        ghostBX   = screenToBoardX(e.getX());
                        ghostBY   = screenToBoardY(e.getY());
                        showGhost = true;
                        repaint();
                    }
                }

                @Override public void mouseWheelMoved(MouseWheelEvent e) {
                    double factor = (e.getWheelRotation() < 0) ? ZOOM_FACTOR : 1.0 / ZOOM_FACTOR;
                    zoom(factor, e.getX(), e.getY());
                }

                @Override public void mouseExited(MouseEvent e) {
                    if (showGhost) { showGhost = false; repaint(); }
                }
            };

            addMouseListener(ma);
            addMouseMotionListener(ma);
            addMouseWheelListener(ma);

            addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_ESCAPE:
                            if (pendingPattern != null) cancelPlacement();
                            break;
                        case KeyEvent.VK_EQUALS:
                        case KeyEvent.VK_PLUS:
                            zoom(ZOOM_FACTOR, getWidth() / 2.0, getHeight() / 2.0);
                            break;
                        case KeyEvent.VK_MINUS:
                            zoom(1.0 / ZOOM_FACTOR, getWidth() / 2.0, getHeight() / 2.0);
                            break;
                        case KeyEvent.VK_0:
                        case KeyEvent.VK_NUMPAD0:
                            cellSize = 12.0;
                            updateStatus();
                            repaint();
                            break;
                        case KeyEvent.VK_SPACE:
                            if (!simRunning) step();
                            break;
                        case KeyEvent.VK_B:
                            blueprintMode = !blueprintMode;
                            repaint();
                            break;
                        case KeyEvent.VK_LEFT:  viewX -= 5; repaint(); break;
                        case KeyEvent.VK_RIGHT: viewX += 5; repaint(); break;
                        case KeyEvent.VK_UP:    viewY -= 5; repaint(); break;
                        case KeyEvent.VK_DOWN:  viewY += 5; repaint(); break;
                        default: break;
                    }
                }
            });
        }

        // ── Coordinate conversion ─────────────────────────────────────────────

        private int screenToBoardX(int sx) { return (int) Math.floor(viewX + sx / cellSize); }
        private int screenToBoardY(int sy) { return (int) Math.floor(viewY + sy / cellSize); }
        private int boardToScreenX(int bx) { return (int) Math.round((bx - viewX) * cellSize); }
        private int boardToScreenY(int by) { return (int) Math.round((by - viewY) * cellSize); }

        // ── Rendering ─────────────────────────────────────────────────────────

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            int W = getWidth(), H = getHeight();
            int cs = Math.max(1, (int) cellSize);

            // Background
            g2.setColor(COLOR_DEAD);
            g2.fillRect(0, 0, W, H);

            // Grid lines (only when cells are large enough)
            if (cellSize >= 4.0) {
                g2.setColor(COLOR_GRID);
                int xMin = (int) Math.floor(viewX);
                int xMax = (int) Math.ceil(viewX + W / cellSize);
                int yMin = (int) Math.floor(viewY);
                int yMax = (int) Math.ceil(viewY + H / cellSize);
                for (int bx = xMin; bx <= xMax; bx++) {
                    int sx = boardToScreenX(bx); g2.drawLine(sx, 0, sx, H);
                }
                for (int by = yMin; by <= yMax; by++) {
                    int sy = boardToScreenY(by); g2.drawLine(0, sy, W, sy);
                }
            }

            // Visible board range (with 1-cell margin)
            int xMinVis = (int) Math.floor(viewX) - 1;
            int xMaxVis = (int) Math.ceil(viewX + W / cellSize) + 1;
            int yMinVis = (int) Math.floor(viewY) - 1;
            int yMaxVis = (int) Math.ceil(viewY + H / cellSize) + 1;

            // Collect visible alive cells from the latest snapshot.
            // The snapshot is a long[] of cell keys captured under boardLock;
            // it is immutable for the lifetime of this repaint call.
            final Set<Long> snap = currentSnapshot;

            if (cellSize < 1.0) {
                // Sub-pixel: draw one pixel per visible live cell.
                g2.setColor(COLOR_ALIVE);
                for (long ck : snap) {
                    int bx = GameOfLifeBoard.keyX(ck), by = GameOfLifeBoard.keyY(ck);
                    if (bx < xMinVis || bx > xMaxVis || by < yMinVis || by > yMaxVis) continue;
                    int sx = (int)((bx - viewX) * cellSize);
                    int sy = (int)((by - viewY) * cellSize);
                    if (sx >= 0 && sx < W && sy >= 0 && sy < H) {
                        g2.fillRect(sx, sy, 1, 1);
                    }
                }
            } else {
                // Glow pass (only when zoomed in enough)
                if (cs >= 8) {
                    g2.setColor(COLOR_GLOW);
                    for (long ck : snap) {
                        int bx = GameOfLifeBoard.keyX(ck), by = GameOfLifeBoard.keyY(ck);
                        if (bx < xMinVis || bx > xMaxVis || by < yMinVis || by > yMaxVis) continue;
                        int sx = boardToScreenX(bx), sy = boardToScreenY(by);
                        g2.fillRect(sx, sy, cs + 1, cs + 1);
                    }
                }
                // Cell fill
                g2.setColor(COLOR_ALIVE);
                int margin = cs >= 4 ? 1 : 0;
                for (long ck : snap) {
                    int bx = GameOfLifeBoard.keyX(ck), by = GameOfLifeBoard.keyY(ck);
                    if (bx < xMinVis || bx > xMaxVis || by < yMinVis || by > yMaxVis) continue;
                    int sx = boardToScreenX(bx), sy = boardToScreenY(by);
                    g2.fillRect(sx + margin, sy + margin, cs - margin, cs - margin);
                }
            }

            // Ghost preview for pending pattern
            if (pendingPattern != null && showGhost) {
                int[] bounds = pendingPattern.getBounds();
                int offX = ghostBX - bounds[0] / 2;
                int offY = ghostBY - bounds[1] / 2;

                g2.setColor(COLOR_GHOST);
                int gcs = Math.max(1, cs);
                for (int[] coord : pendingPattern.getCoordinates()) {
                    int sx = boardToScreenX(coord[0] + offX);
                    int sy = boardToScreenY(coord[1] + offY);
                    g2.fillRect(sx, sy, gcs, gcs);
                }

                // INPUT / OUTPUT region overlays
                String[] regionLabels = pendingPattern.getRegionLabels();
                int[][] regionBoxes   = pendingPattern.getRegionBoxes();
                if (blueprintMode && regionLabels != null && regionBoxes != null) {
                    Font regionFont = new Font("Monospaced", Font.BOLD, regionLabels.length > 6 ? 11 : 14);
                    g2.setFont(regionFont);
                    FontMetrics fm = g2.getFontMetrics(regionFont);
                    Stroke prevStroke = g2.getStroke();
                    g2.setStroke(new BasicStroke(2));
                    for (int i = 0; i < regionLabels.length; i++) {
                        int rx = boardToScreenX(offX + regionBoxes[i][0]);
                        int ry = boardToScreenY(offY + regionBoxes[i][1]);
                        int rw = (int) Math.max(2, regionBoxes[i][2] * cellSize);
                        int rh = (int) Math.max(2, regionBoxes[i][3] * cellSize);
                        if (rx > W || ry > H || rx + rw < 0 || ry + rh < 0) continue;
                        Color fill   = REGION_FILL  [i % REGION_FILL.length];
                        Color border = REGION_BORDER[i % REGION_BORDER.length];
                        g2.setColor(fill);   g2.fillRect(rx, ry, rw, rh);
                        g2.setColor(border); g2.drawRect(rx, ry, rw, rh);
                        int lx = Math.max(4, Math.min(rx + 6, W - fm.stringWidth(regionLabels[i]) - 4));
                        int ly = Math.max(fm.getAscent() + 4, Math.min(ry + fm.getAscent() + 4, H - 4));
                        g2.setColor(Color.BLACK); g2.drawString(regionLabels[i], lx + 1, ly + 1);
                        g2.setColor(border);      g2.drawString(regionLabels[i], lx, ly);
                    }
                    g2.setStroke(prevStroke);
                }
            }
        }
    }
}

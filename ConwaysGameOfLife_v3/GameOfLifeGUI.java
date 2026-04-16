import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Improved Conway's Game of Life GUI (v2).
 *
 * New features over v1:
 *  - Infinite (sparse) board — the board never has a fixed edge.
 *  - Zoom in/out with the mouse wheel (or toolbar buttons), from 1 px/cell
 *    to 64 px/cell.  At 1 px/cell a typical HD screen shows ~2000 × 1100
 *    cells.  The board itself is unbounded.
 *  - Pan by right-click-dragging (or middle-click-dragging) anywhere on the
 *    canvas.
 *  - Pattern placement mode: clicking "Place ▸" arms a placement mode.
 *    A transparent ghost preview follows the cursor.  Left-clicking stamps
 *    the pattern at that position WITHOUT clearing the board.  Press Escape
 *    or click "Cancel" to abort.
 *  - 18 pre-made structures across 5 categories.
 */
public class GameOfLifeGUI extends JFrame {

    // ── Model / state ─────────────────────────────────────────────────────────
    private GameOfLifeBoard board;
    private GridPanel        gridPanel;
    private Timer            timer;
    private boolean          running = false;

    // ── Viewport ──────────────────────────────────────────────────────────────
    /** Pixels per board cell (floating-point for smooth zoom). */
    private double cellSize = 12.0;
    /** Board x-coordinate of the left edge of the viewport. */
    private double viewX = -40.0;
    /** Board y-coordinate of the top edge of the viewport. */
    private double viewY = -30.0;

    private static final double MIN_CELL_SIZE  =  1.0;
    private static final double MAX_CELL_SIZE  = 64.0;
    private static final double ZOOM_FACTOR    =  1.15;

    // ── Pattern placement ─────────────────────────────────────────────────────
    /** Non-null while the user is in placement mode. */
    private Pattern pendingPattern = null;

    // ── Status labels (updated every step/zoom/pan) ───────────────────────────
    private JLabel generationLabel;
    private JLabel populationLabel;
    private JLabel zoomLabel;
    private JLabel placingLabel;

    // ── TicTacToe mode ────────────────────────────────────────────────────────
    private boolean tttMode          = false;
    private int     tttOffsetX       = 0;
    private int     tttOffsetY       = 0;
    private int[]   tttBoardState    = new int[9];
    private int     tttCurrentPlayer = 1;
    private boolean tttGameOver      = false;
    private int[][] tttCellType      = new int[63][63]; // 0=normal,1=wall,2=X,3=O
    private JLabel  tttStatusLabel;
    private JPanel  tttPanel;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color COLOR_ALIVE          = new Color(0,  230,  64);
    private static final Color COLOR_GLOW           = new Color(100, 255, 100,  60);
    private static final Color COLOR_DEAD           = new Color(15,   15,  20);
    private static final Color COLOR_GRID           = new Color(30,   30,  40);
    private static final Color COLOR_BG             = new Color(10,   10,  15);
    private static final Color COLOR_TOOLBAR        = new Color(20,   20,  30);
    private static final Color COLOR_TEXT           = new Color(0,   255,  65);
    private static final Color COLOR_BUTTON         = new Color(30,   60,  30);
    private static final Color COLOR_BUTTON_BORDER  = new Color(0,   200,  50);
    private static final Color COLOR_GHOST          = new Color(0,   180, 255, 120);
    private static final Color COLOR_PLACING_TEXT   = new Color(255, 200,   0);

    // ── TTT-specific colours ──────────────────────────────────────────────────
    private static final Color COLOR_WALL_TTT = new Color(0x4a90d9);
    private static final Color COLOR_X_TTT    = new Color(0xff6b6b);
    private static final Color COLOR_O_TTT    = new Color(0xffd93d);

    // ── Piece patterns (11×11) — same as TicTacToeGoLGUI ─────────────────────
    private static final boolean[][] X_PATTERN = new boolean[11][11];
    private static final boolean[][] O_PATTERN = new boolean[11][11];
    static {
        int[][] xCells = {
            {0,0},{0,1},{0,9},{0,10},
            {1,0},{1,1},{1,2},{1,8},{1,9},{1,10},
            {2,2},{2,3},{2,7},{2,8},
            {3,3},{3,4},{3,6},{3,7},
            {4,4},{4,5},{4,6},
            {5,4},{5,5},{5,6},
            {6,4},{6,5},{6,6},
            {7,3},{7,4},{7,6},{7,7},
            {8,2},{8,3},{8,7},{8,8},
            {9,0},{9,1},{9,2},{9,8},{9,9},{9,10},
            {10,0},{10,1},{10,9},{10,10}
        };
        for (int[] p : xCells) X_PATTERN[p[0]][p[1]] = true;
        int[][] oCells = {
            {0,3},{0,4},{0,5},{0,6},{0,7},
            {1,1},{1,2},{1,8},{1,9},
            {2,0},{2,1},{2,9},{2,10},
            {3,0},{3,10},
            {4,0},{4,10},
            {5,0},{5,10},
            {6,0},{6,10},
            {7,0},{7,10},
            {8,0},{8,1},{8,9},{8,10},
            {9,1},{9,2},{9,8},{9,9},
            {10,3},{10,4},{10,5},{10,6},{10,7}
        };
        for (int[] p : oCells) O_PATTERN[p[0]][p[1]] = true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public GameOfLifeGUI() {
        board = new GameOfLifeBoard();
        board.randomize(80, 60);
        initUI();
        timer = new Timer(100, e -> step());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI construction
    // ─────────────────────────────────────────────────────────────────────────

    private void initUI() {
        setTitle("Conway's Game of Life  v2");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(COLOR_BG);
        getContentPane().setBackground(COLOR_BG);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(COLOR_BG);

        gridPanel = new GridPanel();
        mainPanel.add(gridPanel, BorderLayout.CENTER);
        mainPanel.add(createToolbar(),   BorderLayout.NORTH);
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

        add(mainPanel);
        setMinimumSize(new Dimension(800, 560));
        pack();
        setLocationRelativeTo(null);
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
        randomBtn.addActionListener(e -> { stop(); board.randomize(80, 60); gridPanel.repaint(); updateStatus(); });

        bar.add(startBtn); bar.add(stopBtn); bar.add(stepBtn);
        bar.add(resetBtn); bar.add(randomBtn);
        bar.add(separator());

        // ── Speed slider ──────────────────────────────────────────────────────
        bar.add(makeLabel("Speed:"));
        JSlider speedSlider = new JSlider(1, 60, 10);
        speedSlider.setBackground(COLOR_TOOLBAR);
        speedSlider.setForeground(COLOR_TEXT);
        speedSlider.setPreferredSize(new Dimension(110, 28));
        speedSlider.addChangeListener(e -> timer.setDelay(1000 / speedSlider.getValue()));
        bar.add(speedSlider);
        bar.add(separator());

        // ── Zoom controls ─────────────────────────────────────────────────────
        JButton zoomInBtn  = makeButton("🔍+");
        JButton zoomOutBtn = makeButton("🔍-");
        JButton centerBtn  = makeButton("⌂ Center");
        zoomInBtn .addActionListener(e -> zoom(ZOOM_FACTOR,      gridPanel.getWidth()  / 2.0,
                                                                  gridPanel.getHeight() / 2.0));
        zoomOutBtn.addActionListener(e -> zoom(1.0 / ZOOM_FACTOR, gridPanel.getWidth()  / 2.0,
                                                                   gridPanel.getHeight() / 2.0));
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
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
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

        bar.add(patternBox);
        bar.add(placeBtn);
        bar.add(cancelBtn);

        // Store cancel button reference so we can disable it from elsewhere
        gridPanel.putClientProperty("cancelBtn", cancelBtn);

        // ── TicTacToe mode panel (hidden until TTT mode is active) ────────────
        tttPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        tttPanel.setBackground(new Color(10, 15, 30));
        tttPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_WALL_TTT));

        tttStatusLabel = new JLabel("Player X's turn");
        tttStatusLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        tttStatusLabel.setForeground(COLOR_X_TTT);

        JButton tttNewGameBtn2 = makeButton("\u21ba New TTT Game");
        tttNewGameBtn2.addActionListener(e -> resetTttGame());

        JButton tttExitBtn = makeButton("\u2715 Exit TicTacToe");
        tttExitBtn.addActionListener(e -> exitTttMode());

        JLabel tttModeLabel = makeLabel("\uD83C\uDFAE TicTacToe Mode \u2014 click a square to play:");
        tttModeLabel.setForeground(COLOR_WALL_TTT);

        tttPanel.add(tttModeLabel);
        tttPanel.add(tttStatusLabel);
        tttPanel.add(tttNewGameBtn2);
        tttPanel.add(tttExitBtn);
        tttPanel.setVisible(false);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(COLOR_TOOLBAR);
        wrapper.add(bar);
        wrapper.add(tttPanel);

        return wrapper;
    }

    /** Build the full list of patterns in category order. */
    private Pattern[] buildPatternList() {
        return new Pattern[]{
            // Still lifes
            Pattern.block(),
            Pattern.beehive(),
            Pattern.loaf(),
            Pattern.boat(),
            Pattern.tub(),
            // Oscillators
            Pattern.blinker(),
            Pattern.toad(),
            Pattern.clock(),
            Pattern.beacon(),
            Pattern.pulsar(),
            Pattern.figureEight(),
            Pattern.pentadecathlon(),
            // Spaceships
            Pattern.glider(),
            Pattern.lwss(),
            // Guns
            Pattern.gosperGliderGun(),
            // Methuselahs
            Pattern.rPentomino(),
            Pattern.diehard(),
            Pattern.acorn(),
            // TicTacToe
            Pattern.ticTacToeComputer()
        };
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 3));
        bar.setBackground(COLOR_TOOLBAR);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BUTTON_BORDER));

        generationLabel = makeLabel("Generation: 0");
        populationLabel = makeLabel("Population: 0");
        zoomLabel       = makeLabel("Zoom: 12px/cell");
        placingLabel    = makeLabel("");
        placingLabel.setForeground(COLOR_PLACING_TEXT);

        JLabel hint = makeLabel(" | RMB-drag: pan  |  Wheel: zoom  |  LMB: draw");
        hint.setForeground(new Color(0, 130, 40));

        bar.add(generationLabel);
        bar.add(populationLabel);
        bar.add(zoomLabel);
        bar.add(hint);
        bar.add(placingLabel);
        return bar;
    }

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
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(COLOR_BUTTON); }
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
    // Simulation commands
    // ─────────────────────────────────────────────────────────────────────────

    private void start() { if (!running) { running = true; timer.start(); } }
    private void stop()  { running = false; timer.stop(); }

    private void reset() {
        stop();
        board.clear();
        cancelPlacement();
        updateStatus();
        gridPanel.repaint();
    }

    private void step() {
        board.nextGeneration();
        if (tttMode) maintainTttPins();
        gridPanel.repaint();
        updateStatus();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Viewport helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Apply a zoom factor, keeping the screen point (mx, my) fixed.
     * @param factor  > 1 zooms in, 0 < factor < 1 zooms out.
     */
    private void zoom(double factor, double mx, double my) {
        double boardMX = viewX + mx / cellSize;
        double boardMY = viewY + my / cellSize;
        cellSize = Math.min(MAX_CELL_SIZE, Math.max(MIN_CELL_SIZE, cellSize * factor));
        viewX = boardMX - mx / cellSize;
        viewY = boardMY - my / cellSize;
        updateStatus();
        gridPanel.repaint();
    }

    /** Pan so that the alive-cell bounding box is centred in the viewport. */
    private void centerView() {
        Set<Long> snapshot = board.getAliveCells();
        double cx = 0, cy = 0;
        if (!snapshot.isEmpty()) {
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            for (long k : snapshot) {
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

    /**
     * Stamp the pattern centred on (boardX, boardY), adding its cells to the
     * current board without touching any existing cells.
     */
    private void stampPattern(Pattern p, int boardX, int boardY) {
        if ("TicTacToe Computer (GoL)".equals(p.getName())) {
            activateTicTacToeMode(boardX, boardY);
            return;
        }
        int[] bounds = p.getBounds();
        int offX = boardX - bounds[0] / 2;
        int offY = boardY - bounds[1] / 2;
        board.setCellPattern(p, offX, offY);
        updateStatus();
        gridPanel.repaint();
    }

    private void cancelPlacement() {
        pendingPattern = null;
        gridPanel.setCursor(Cursor.getDefaultCursor());
        // Disable cancel button if accessible
        Object cancelBtn = gridPanel.getClientProperty("cancelBtn");
        if (cancelBtn instanceof JButton) ((JButton) cancelBtn).setEnabled(false);
        updateStatus();
        gridPanel.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TicTacToe mode
    // ─────────────────────────────────────────────────────────────────────────

    private void activateTicTacToeMode(int boardX, int boardY) {
        tttOffsetX = boardX - 31;
        tttOffsetY = boardY - 31;
        board.clear();
        tttCellType      = new int[63][63];
        tttBoardState    = new int[9];
        tttCurrentPlayer = 1;
        tttGameOver      = false;
        tttMode          = true;

        for (int r = 0; r < 63; r++)
            for (int c = 0; c < 63; c++)
                if (isTttWall(r, c)) {
                    tttCellType[r][c] = 1;
                    board.setCell(tttOffsetX + c, tttOffsetY + r, true);
                }

        java.util.Random rng = new java.util.Random();
        for (int r = 0; r < 63; r++)
            for (int c = 0; c < 63; c++)
                if (tttCellType[r][c] == 0 && rng.nextFloat() < 0.30f)
                    board.setCell(tttOffsetX + c, tttOffsetY + r, true);

        tttPanel.setVisible(true);
        tttStatusLabel.setText("Player X's turn");
        tttStatusLabel.setForeground(COLOR_X_TTT);
        revalidate();
        updateStatus();
        gridPanel.repaint();
    }

    private boolean isTttWall(int r, int c) {
        return (r == 0 || r == 20 || r == 21 || r == 41 || r == 42 || r == 62)
            || (c == 0 || c == 20 || c == 21 || c == 41 || c == 42 || c == 62);
    }

    private void maintainTttPins() {
        for (int r = 0; r < 63; r++)
            for (int c = 0; c < 63; c++)
                if (tttCellType[r][c] != 0)
                    board.setCell(tttOffsetX + c, tttOffsetY + r, true);
    }

    private void handleTttClick(int boardX, int boardY) {
        if (tttGameOver) return;
        int relCol = boardX - tttOffsetX;
        int relRow = boardY - tttOffsetY;
        if (relRow < 0 || relRow >= 63 || relCol < 0 || relCol >= 63) return;
        int sq = getTttSquare(relRow, relCol);
        if (sq < 0 || tttBoardState[sq] != 0) return;

        if (tttCurrentPlayer == 1) tttPlaceX(sq);
        else                        tttPlaceO(sq);

        int winner = tttCheckWinner();
        if (winner == 1) {
            tttGameOver = true;
            tttStatusLabel.setText("Player X wins! \uD83C\uDF89");
            tttStatusLabel.setForeground(COLOR_X_TTT);
        } else if (winner == 2) {
            tttGameOver = true;
            tttStatusLabel.setText("Player O wins! \uD83C\uDF89");
            tttStatusLabel.setForeground(COLOR_O_TTT);
        } else if (winner == 3) {
            tttGameOver = true;
            tttStatusLabel.setText("It's a Draw!");
            tttStatusLabel.setForeground(COLOR_ALIVE);
        } else {
            tttCurrentPlayer = (tttCurrentPlayer == 1) ? 2 : 1;
            updateTttStatus();
        }
        gridPanel.repaint();
    }

    private int getTttSquare(int row, int col) {
        if (isTttWall(row, col)) return -1;
        int rb = tttRowBand(row);
        int cb = tttColBand(col);
        if (rb < 0 || cb < 0) return -1;
        return rb * 3 + cb;
    }

    private int tttRowBand(int r) {
        if (r >= 1  && r <= 19) return 0;
        if (r >= 22 && r <= 40) return 1;
        if (r >= 43 && r <= 61) return 2;
        return -1;
    }

    private int tttColBand(int c) {
        if (c >= 1  && c <= 19) return 0;
        if (c >= 22 && c <= 40) return 1;
        if (c >= 43 && c <= 61) return 2;
        return -1;
    }

    private int[] tttSquareOrigin(int sq) {
        return new int[]{ 1 + (sq / 3) * 21, 1 + (sq % 3) * 21 };
    }

    private void tttPlaceX(int sq) {
        int[] o = tttSquareOrigin(sq);
        int baseR = o[0] + 4, baseC = o[1] + 4;
        for (int r = 0; r < 11; r++)
            for (int c = 0; c < 11; c++)
                if (X_PATTERN[r][c]) {
                    tttCellType[baseR + r][baseC + c] = 2;
                    board.setCell(tttOffsetX + (baseC + c), tttOffsetY + (baseR + r), true);
                }
        tttBoardState[sq] = 1;
    }

    private void tttPlaceO(int sq) {
        int[] o = tttSquareOrigin(sq);
        int baseR = o[0] + 4, baseC = o[1] + 4;
        for (int r = 0; r < 11; r++)
            for (int c = 0; c < 11; c++)
                if (O_PATTERN[r][c]) {
                    tttCellType[baseR + r][baseC + c] = 3;
                    board.setCell(tttOffsetX + (baseC + c), tttOffsetY + (baseR + r), true);
                }
        tttBoardState[sq] = 2;
    }

    private int tttCheckWinner() {
        int[][] lines = {
            {0,1,2},{3,4,5},{6,7,8},
            {0,3,6},{1,4,7},{2,5,8},
            {0,4,8},{2,4,6}
        };
        for (int[] line : lines) {
            int a = tttBoardState[line[0]], b = tttBoardState[line[1]], c = tttBoardState[line[2]];
            if (a != 0 && a == b && b == c) return a;
        }
        for (int s : tttBoardState) if (s == 0) return 0;
        return 3;
    }

    private void updateTttStatus() {
        if (tttCurrentPlayer == 1) {
            tttStatusLabel.setText("Player X's turn");
            tttStatusLabel.setForeground(COLOR_X_TTT);
        } else {
            tttStatusLabel.setText("Player O's turn");
            tttStatusLabel.setForeground(COLOR_O_TTT);
        }
    }

    private void resetTttGame() {
        tttBoardState    = new int[9];
        tttCurrentPlayer = 1;
        tttGameOver      = false;
        for (int r = 0; r < 63; r++)
            for (int c = 0; c < 63; c++)
                if (tttCellType[r][c] == 2 || tttCellType[r][c] == 3) {
                    tttCellType[r][c] = 0;
                    board.setCell(tttOffsetX + c, tttOffsetY + r, false);
                }
        updateTttStatus();
        gridPanel.repaint();
    }

    private void exitTttMode() {
        tttMode = false;
        tttPanel.setVisible(false);
        board.clear();
        revalidate();
        updateStatus();
        gridPanel.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Status bar
    // ─────────────────────────────────────────────────────────────────────────

    private void updateStatus() {
        generationLabel.setText("Generation: " + board.getGeneration());
        populationLabel.setText("Population: " + board.countAlive());
        if (cellSize >= 10)
            zoomLabel.setText(String.format("Zoom: %.0fpx/cell", cellSize));
        else
            zoomLabel.setText(String.format("Zoom: %.1fpx/cell", cellSize));

        if (pendingPattern != null) {
            placingLabel.setText(
                "✦ PLACING: " + pendingPattern.getName() +
                "  —  left-click to place,  Esc to cancel");
        } else {
            placingLabel.setText("");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GridPanel — the canvas that renders the board and handles all interaction
    // ─────────────────────────────────────────────────────────────────────────

    private class GridPanel extends JPanel {

        // ── Painting (free-draw) state ────────────────────────────────────────
        private boolean painting   = false;
        private boolean paintValue = true;

        // ── Panning state ─────────────────────────────────────────────────────
        private boolean panning  = false;
        private int     lastPanX, lastPanY;

        // ── Ghost-preview state ───────────────────────────────────────────────
        private int     ghostBX   = 0;
        private int     ghostBY   = 0;
        private boolean showGhost = false;

        public GridPanel() {
            setBackground(COLOR_BG);
            setFocusable(true);
            setPreferredSize(new Dimension(960, 680));

            MouseAdapter ma = new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();

                    if (SwingUtilities.isRightMouseButton(e)
                            || SwingUtilities.isMiddleMouseButton(e)) {
                        panning  = true;
                        lastPanX = e.getX();
                        lastPanY = e.getY();

                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (pendingPattern != null) {
                            // Stamp and leave placement mode
                            stampPattern(pendingPattern, screenToBoardX(e.getX()),
                                                         screenToBoardY(e.getY()));
                            cancelPlacement();
                        } else if (tttMode) {
                            handleTttClick(screenToBoardX(e.getX()), screenToBoardY(e.getY()));
                        } else {
                            int bx = screenToBoardX(e.getX());
                            int by = screenToBoardY(e.getY());
                            paintValue = !board.getCell(bx, by);
                            painting   = true;
                            board.setCell(bx, by, paintValue);
                            repaint();
                            updateStatus();
                        }
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (panning) {
                        viewX -= (e.getX() - lastPanX) / cellSize;
                        viewY -= (e.getY() - lastPanY) / cellSize;
                        lastPanX = e.getX();
                        lastPanY = e.getY();
                        updateStatus();
                        repaint();
                    } else if (painting && pendingPattern == null && !tttMode) {
                        int bx = screenToBoardX(e.getX());
                        int by = screenToBoardY(e.getY());
                        board.setCell(bx, by, paintValue);
                        repaint();
                        updateStatus();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    panning  = false;
                    painting = false;
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    if (pendingPattern != null) {
                        ghostBX   = screenToBoardX(e.getX());
                        ghostBY   = screenToBoardY(e.getY());
                        showGhost = true;
                        repaint();
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    double factor = (e.getWheelRotation() < 0) ? ZOOM_FACTOR : 1.0 / ZOOM_FACTOR;
                    zoom(factor, e.getX(), e.getY());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (showGhost) { showGhost = false; repaint(); }
                }
            };

            addMouseListener(ma);
            addMouseMotionListener(ma);
            addMouseWheelListener(ma);

            // Keyboard shortcuts
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_ESCAPE:
                            if (pendingPattern != null) cancelPlacement();
                            else if (tttMode) exitTttMode();
                            break;
                        case KeyEvent.VK_EQUALS:
                        case KeyEvent.VK_PLUS:
                            zoom(ZOOM_FACTOR,      getWidth() / 2.0, getHeight() / 2.0);
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
                            if (!running) step();
                            break;
                        case KeyEvent.VK_LEFT:
                            viewX -= 5; repaint(); break;
                        case KeyEvent.VK_RIGHT:
                            viewX += 5; repaint(); break;
                        case KeyEvent.VK_UP:
                            viewY -= 5; repaint(); break;
                        case KeyEvent.VK_DOWN:
                            viewY += 5; repaint(); break;
                        default:
                            break;
                    }
                }
            });
        }

        // ── Coordinate conversion ─────────────────────────────────────────────

        private int screenToBoardX(int sx) {
            return (int) Math.floor(viewX + sx / cellSize);
        }

        private int screenToBoardY(int sy) {
            return (int) Math.floor(viewY + sy / cellSize);
        }

        private int boardToScreenX(int bx) {
            return (int) Math.round((bx - viewX) * cellSize);
        }

        private int boardToScreenY(int by) {
            return (int) Math.round((by - viewY) * cellSize);
        }

        // ── Rendering ─────────────────────────────────────────────────────────

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_OFF);

            int W = getWidth(), H = getHeight();
            int cs = Math.max(1, (int) cellSize);   // cell size in whole pixels

            // Background
            g2.setColor(COLOR_DEAD);
            g2.fillRect(0, 0, W, H);

            // Grid lines (only when cells are large enough to warrant them)
            if (cellSize >= 4.0) {
                g2.setColor(COLOR_GRID);
                int xMin = (int) Math.floor(viewX);
                int xMax = (int) Math.ceil(viewX + W / cellSize);
                int yMin = (int) Math.floor(viewY);
                int yMax = (int) Math.ceil(viewY + H / cellSize);
                for (int bx = xMin; bx <= xMax; bx++) {
                    int sx = boardToScreenX(bx);
                    g2.drawLine(sx, 0, sx, H);
                }
                for (int by = yMin; by <= yMax; by++) {
                    int sy = boardToScreenY(by);
                    g2.drawLine(0, sy, W, sy);
                }
            }

            // Visible board range (with 1-cell margin)
            int xMinVis = (int) Math.floor(viewX) - 1;
            int xMaxVis = (int) Math.ceil(viewX + W / cellSize) + 1;
            int yMinVis = (int) Math.floor(viewY) - 1;
            int yMaxVis = (int) Math.ceil(viewY + H / cellSize) + 1;

            // Collect visible alive cells
            Set<Long> snapshot = board.getAliveCells();
            int capacity = Math.max(16, snapshot.size());
            java.util.List<int[]> visible = new java.util.ArrayList<>(capacity);
            for (long ck : snapshot) {
                int bx = GameOfLifeBoard.keyX(ck);
                int by = GameOfLifeBoard.keyY(ck);
                if (bx >= xMinVis && bx <= xMaxVis && by >= yMinVis && by <= yMaxVis)
                    visible.add(new int[]{bx, by});
            }

            // Glow pass (only when zoomed in enough)
            if (cs >= 8) {
                g2.setColor(COLOR_GLOW);
                for (int[] bc : visible) {
                    int sx = boardToScreenX(bc[0]);
                    int sy = boardToScreenY(bc[1]);
                    g2.fillRect(sx, sy, cs + 1, cs + 1);
                }
            }

            // Cell fill pass
            int margin = cs >= 4 ? 1 : 0;
            if (tttMode) {
                for (int[] bc : visible) {
                    int relCol = bc[0] - tttOffsetX;
                    int relRow = bc[1] - tttOffsetY;
                    int type = (relRow >= 0 && relRow < 63 && relCol >= 0 && relCol < 63)
                               ? tttCellType[relRow][relCol] : 0;
                    Color cellColor;
                    switch (type) {
                        case 1:  cellColor = COLOR_WALL_TTT; break;
                        case 2:  cellColor = COLOR_X_TTT;    break;
                        case 3:  cellColor = COLOR_O_TTT;    break;
                        default: cellColor = COLOR_ALIVE;    break;
                    }
                    g2.setColor(cellColor);
                    int sx = boardToScreenX(bc[0]);
                    int sy = boardToScreenY(bc[1]);
                    g2.fillRect(sx + margin, sy + margin, cs - margin, cs - margin);
                }
            } else {
                g2.setColor(COLOR_ALIVE);
                for (int[] bc : visible) {
                    int sx = boardToScreenX(bc[0]);
                    int sy = boardToScreenY(bc[1]);
                    g2.fillRect(sx + margin, sy + margin, cs - margin, cs - margin);
                }
            }

            // Ghost preview for pending pattern
            if (pendingPattern != null && showGhost) {
                int[] bounds = pendingPattern.getBounds();
                int offX = ghostBX - bounds[0] / 2;
                int offY = ghostBY - bounds[1] / 2;
                g2.setColor(COLOR_GHOST);
                for (int[] coord : pendingPattern.getCoordinates()) {
                    int sx = boardToScreenX(coord[0] + offX);
                    int sy = boardToScreenY(coord[1] + offY);
                    g2.fillRect(sx, sy, cs, cs);
                }
            }
        }
    }
}

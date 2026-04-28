import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class TicTacToeGoLGUI extends JFrame {

    // ── Grid constants ─────────────────────────────────────────────────────────
    static final int GRID    = 63;
    static final int CELL_PX = 10;

    // ── Cell-type constants ────────────────────────────────────────────────────
    static final int TYPE_NORMAL = 0;
    static final int TYPE_WALL   = 1;
    static final int TYPE_X      = 2;
    static final int TYPE_O      = 3;

    // ── Colors ─────────────────────────────────────────────────────────────────
    static final Color COLOR_DEAD = new Color(0x0d1117);
    static final Color COLOR_LIVE = new Color(0x39d353);
    static final Color COLOR_WALL = new Color(0x4a90d9);
    static final Color COLOR_X    = new Color(0xff6b6b);
    static final Color COLOR_O    = new Color(0xffd93d);

    // ── Piece patterns (11 × 11) ───────────────────────────────────────────────
    static final boolean[][] X_PATTERN = new boolean[11][11];
    static final boolean[][] O_PATTERN = new boolean[11][11];

    static {
        // X pattern cells
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

        // O pattern cells
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

    // ── State ──────────────────────────────────────────────────────────────────
    boolean[][] cells   = new boolean[GRID][GRID];
    int[][] cellType    = new int[GRID][GRID];
    int[] boardState    = new int[9];   // 0=empty, 1=X, 2=O
    int currentPlayer   = 1;            // 1=X, 2=O
    boolean gameOver    = false;

    // ── Swing components ───────────────────────────────────────────────────────
    GridPanel gridPanel;
    JLabel    statusLabel;
    Timer     timer;

    // ──────────────────────────────────────────────────────────────────────────
    public TicTacToeGoLGUI() {
        setTitle("Tic-Tac-Toe \u2014 Running on Conway's Game of Life");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        gridPanel = new GridPanel();
        gridPanel.setPreferredSize(new Dimension(GRID * CELL_PX, GRID * CELL_PX));
        gridPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleClick(e.getX(), e.getY()); }
        });

        statusLabel = new JLabel("Player X's turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        statusLabel.setForeground(COLOR_X);
        statusLabel.setBackground(new Color(0x0d1117));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JButton newGameBtn = new JButton("New Game");
        newGameBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
        newGameBtn.setBackground(new Color(20, 50, 20));
        newGameBtn.setForeground(COLOR_LIVE);
        newGameBtn.setFocusPainted(false);
        newGameBtn.addActionListener(e -> resetGame());

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
        bottomPanel.setBackground(new Color(0x0d1117));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(newGameBtn, BorderLayout.EAST);

        add(gridPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Stop timer when window closes to avoid resource leak
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { if (timer != null) timer.stop(); }
        });

        initGrid();

        timer = new Timer(100, e -> {
            nextGeneration();
            gridPanel.repaint();
        });
        timer.start();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Initialisation ─────────────────────────────────────────────────────────

    void initGrid() {
        cells     = new boolean[GRID][GRID];
        cellType  = new int[GRID][GRID];
        boardState = new int[9];
        currentPlayer = 1;
        gameOver = false;
        placeWalls();
        randomizeEmpty();
        updateStatus();
    }

    void placeWalls() {
        for (int r = 0; r < GRID; r++)
            for (int c = 0; c < GRID; c++)
                if (isWall(r, c)) { cellType[r][c] = TYPE_WALL; cells[r][c] = true; }
    }

    boolean isWall(int r, int c) {
        return (r == 0 || r == 20 || r == 21 || r == 41 || r == 42 || r == 62)
            || (c == 0 || c == 20 || c == 21 || c == 41 || c == 42 || c == 62);
    }

    void randomizeEmpty() {
        Random rng = new Random();
        for (int r = 0; r < GRID; r++)
            for (int c = 0; c < GRID; c++)
                if (cellType[r][c] == TYPE_NORMAL) cells[r][c] = rng.nextFloat() < 0.30f;
    }

    // ── GoL evolution ──────────────────────────────────────────────────────────

    void nextGeneration() {
        boolean[][] next = new boolean[GRID][GRID];
        for (int r = 0; r < GRID; r++) {
            for (int c = 0; c < GRID; c++) {
                if (cellType[r][c] != TYPE_NORMAL) {
                    next[r][c] = cells[r][c]; // pinned cells never change
                } else {
                    int n = countNeighbors(r, c);
                    next[r][c] = cells[r][c] ? (n == 2 || n == 3) : (n == 3);
                }
            }
        }
        cells = next;
    }

    int countNeighbors(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (nr >= 0 && nr < GRID && nc >= 0 && nc < GRID && cells[nr][nc]) count++;
            }
        return count;
    }

    // ── Square addressing ──────────────────────────────────────────────────────

    /**
     * Returns which of the 9 board squares (0–8) a grid cell belongs to,
     * or -1 if the cell is on a wall/divider.
     *
     * Layout (0-indexed):
     *   Wall rows/cols : 0, 20, 21, 41, 42, 62
     *   Square row bands: 1-19 (band 0), 22-40 (band 1), 43-61 (band 2)
     *   Square col bands: 1-19 (band 0), 22-40 (band 1), 43-61 (band 2)
     */
    int getSquareIndex(int row, int col) {
        if (isWall(row, col)) return -1;
        int rb = rowBand(row);
        int cb = colBand(col);
        if (rb < 0 || cb < 0) return -1;
        return rb * 3 + cb;
    }

    int rowBand(int r) {
        if (r >= 1  && r <= 19) return 0;
        if (r >= 22 && r <= 40) return 1;
        if (r >= 43 && r <= 61) return 2;
        return -1;
    }

    int colBand(int c) {
        if (c >= 1  && c <= 19) return 0;
        if (c >= 22 && c <= 40) return 1;
        if (c >= 43 && c <= 61) return 2;
        return -1;
    }

    /** Top-left (row, col) of the 19×19 area for square sq. */
    int[] squareOrigin(int sq) {
        return new int[]{ 1 + (sq / 3) * 21, 1 + (sq % 3) * 21 };
    }

    // ── Piece placement ────────────────────────────────────────────────────────

    void placeX(int sq) {
        int[] o = squareOrigin(sq);
        int baseR = o[0] + 4, baseC = o[1] + 4;
        for (int r = 0; r < 11; r++)
            for (int c = 0; c < 11; c++)
                if (X_PATTERN[r][c]) { cellType[baseR+r][baseC+c] = TYPE_X; cells[baseR+r][baseC+c] = true; }
        boardState[sq] = 1;
    }

    void placeO(int sq) {
        int[] o = squareOrigin(sq);
        int baseR = o[0] + 4, baseC = o[1] + 4;
        for (int r = 0; r < 11; r++)
            for (int c = 0; c < 11; c++)
                if (O_PATTERN[r][c]) { cellType[baseR+r][baseC+c] = TYPE_O; cells[baseR+r][baseC+c] = true; }
        boardState[sq] = 2;
    }

    // ── Win detection ──────────────────────────────────────────────────────────

    /** Returns 1 (X wins), 2 (O wins), 3 (draw), or 0 (still playing). */
    int checkWinner() {
        int[][] lines = {
            {0,1,2},{3,4,5},{6,7,8},  // rows
            {0,3,6},{1,4,7},{2,5,8},  // cols
            {0,4,8},{2,4,6}           // diagonals
        };
        for (int[] line : lines) {
            int a = boardState[line[0]], b = boardState[line[1]], c = boardState[line[2]];
            if (a != 0 && a == b && b == c) return a;
        }
        for (int s : boardState) if (s == 0) return 0;
        return 3;
    }

    // ── Interaction ────────────────────────────────────────────────────────────

    void handleClick(int px, int py) {
        if (gameOver) return;
        int col = px / CELL_PX;
        int row = py / CELL_PX;
        if (row < 0 || row >= GRID || col < 0 || col >= GRID) return;
        int sq = getSquareIndex(row, col);
        if (sq < 0 || boardState[sq] != 0) return;

        if (currentPlayer == 1) placeX(sq); else placeO(sq);

        int winner = checkWinner();
        if (winner == 1) {
            gameOver = true;
            statusLabel.setText("Player X wins! \uD83C\uDF89");
            statusLabel.setForeground(COLOR_X);
        } else if (winner == 2) {
            gameOver = true;
            statusLabel.setText("Player O wins! \uD83C\uDF89");
            statusLabel.setForeground(COLOR_O);
        } else if (winner == 3) {
            gameOver = true;
            statusLabel.setText("It's a Draw!");
            statusLabel.setForeground(COLOR_LIVE);
        } else {
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
            updateStatus();
        }
        gridPanel.repaint();
    }

    void updateStatus() {
        if (currentPlayer == 1) { statusLabel.setText("Player X's turn"); statusLabel.setForeground(COLOR_X); }
        else                    { statusLabel.setText("Player O's turn"); statusLabel.setForeground(COLOR_O); }
    }

    void resetGame() {
        initGrid();
        gridPanel.repaint();
    }

    // ── Rendering ──────────────────────────────────────────────────────────────

    class GridPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int r = 0; r < GRID; r++) {
                for (int c = 0; c < GRID; c++) {
                    Color color;
                    switch (cellType[r][c]) {
                        case TYPE_WALL: color = COLOR_WALL; break;
                        case TYPE_X:    color = COLOR_X;    break;
                        case TYPE_O:    color = COLOR_O;    break;
                        default:        color = cells[r][c] ? COLOR_LIVE : COLOR_DEAD;
                    }
                    g.setColor(color);
                    g.fillRect(c * CELL_PX, r * CELL_PX, CELL_PX, CELL_PX);
                }
            }
        }
    }
}

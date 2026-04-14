import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class TicTacToeGUI extends JFrame {
    private TicTacToeBoard board;
    private MinimaxAI ai;
    private JPanel boardPanel;
    private JLabel statusLabel;
    private GoLBackground golBg;
    private Timer golTimer;

    private static final Color BG_COLOR        = new Color(15, 15, 20);
    private static final Color GRID_COLOR       = new Color(0, 180, 50);
    private static final Color PLAYER_COLOR     = new Color(255, 180, 0);
    private static final Color AI_COLOR         = new Color(0, 255, 65);
    private static final Color HOVER_COLOR      = new Color(0, 60, 20);
    private static final Color WIN_COLOR        = new Color(0, 255, 65, 80);
    private static final Color STATUS_COLOR     = new Color(0, 255, 65);
    private static final Color TEXT_DIM         = new Color(0, 140, 40);

    private int hoveredRow = -1, hoveredCol = -1;
    private boolean gameOver = false;
    private int[] winLine = null; // indices of winning cells flat 0-8

    public TicTacToeGUI() {
        board = new TicTacToeBoard();
        ai = new MinimaxAI();
        initUI();
    }

    private void initUI() {
        setTitle("Tic-Tac-Toe — GoL Computer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(BG_COLOR);
        getContentPane().setBackground(BG_COLOR);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_COLOR);

        // GoL animated background border panel
        golBg = new GoLBackground();
        golBg.setLayout(new BorderLayout(10, 10));
        golBg.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        boardPanel = new JPanel(new GridLayout(3, 3, 4, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 30, 10));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        boardPanel.setBackground(new Color(0, 30, 10));
        boardPanel.setBorder(BorderFactory.createLineBorder(GRID_COLOR, 2));
        boardPanel.setOpaque(false);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                boardPanel.add(createCell(r, c));
            }
        }

        golBg.add(createTopBar(), BorderLayout.NORTH);
        golBg.add(boardPanel, BorderLayout.CENTER);
        golBg.add(createBottomBar(), BorderLayout.SOUTH);

        mainPanel.add(golBg, BorderLayout.CENTER);
        add(mainPanel);

        setPreferredSize(new Dimension(520, 600));
        pack();
        setLocationRelativeTo(null);
        setResizable(false);

        golTimer = new Timer(120, e -> { golBg.step(); golBg.repaint(); });
        golTimer.start();
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("◈ TIC-TAC-TOE on GoL COMPUTER ◈", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 16));
        title.setForeground(AI_COLOR);
        top.add(title, BorderLayout.NORTH);

        JLabel legend = new JLabel("YOU: X (amber)   |   AI: O (green)", SwingConstants.CENTER);
        legend.setFont(new Font("Monospaced", Font.PLAIN, 11));
        legend.setForeground(TEXT_DIM);
        top.add(legend, BorderLayout.SOUTH);
        return top;
    }

    private JPanel createBottomBar() {
        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);

        statusLabel = new JLabel("Your turn — click a cell", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        statusLabel.setForeground(STATUS_COLOR);
        bottom.add(statusLabel, BorderLayout.CENTER);

        JButton newGameBtn = new JButton("[ NEW GAME ]");
        newGameBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
        newGameBtn.setBackground(new Color(20, 50, 20));
        newGameBtn.setForeground(AI_COLOR);
        newGameBtn.setBorder(BorderFactory.createLineBorder(GRID_COLOR, 2));
        newGameBtn.setFocusPainted(false);
        newGameBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newGameBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { newGameBtn.setBackground(new Color(30, 80, 30)); }
            @Override public void mouseExited(MouseEvent e)  { newGameBtn.setBackground(new Color(20, 50, 20)); }
        });
        newGameBtn.addActionListener(e -> resetGame());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(newGameBtn);
        bottom.add(btnPanel, BorderLayout.SOUTH);
        return bottom;
    }

    private JPanel createCell(int row, int col) {
        JPanel cell = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                if (isWinningCell(row, col)) {
                    g2.setColor(WIN_COLOR);
                } else if (hoveredRow == row && hoveredCol == col && !gameOver &&
                           board.getCell(row, col) == TicTacToeBoard.EMPTY) {
                    g2.setColor(HOVER_COLOR);
                } else {
                    g2.setColor(new Color(10, 20, 10));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(GRID_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                // Symbol
                int v = board.getCell(row, col);
                if (v == TicTacToeBoard.PLAYER) {
                    drawX(g2, getWidth(), getHeight());
                } else if (v == TicTacToeBoard.AI) {
                    drawO(g2, getWidth(), getHeight());
                }
            }

            private void drawX(Graphics2D g2, int w, int h) {
                g2.setColor(PLAYER_COLOR);
                g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = 20;
                g2.drawLine(pad, pad, w - pad, h - pad);
                g2.drawLine(w - pad, pad, pad, h - pad);
                // Glow
                g2.setColor(new Color(255, 180, 0, 40));
                g2.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(pad, pad, w - pad, h - pad);
                g2.drawLine(w - pad, pad, pad, h - pad);
            }

            private void drawO(Graphics2D g2, int w, int h) {
                g2.setColor(AI_COLOR);
                g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = 18;
                g2.drawOval(pad, pad, w - 2 * pad, h - 2 * pad);
                // Glow
                g2.setColor(new Color(0, 255, 65, 40));
                g2.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(pad, pad, w - 2 * pad, h - 2 * pad);
            }
        };

        cell.setPreferredSize(new Dimension(130, 130));
        cell.setOpaque(false);
        cell.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameOver || board.getCurrentPlayer() != TicTacToeBoard.PLAYER) return;
                if (board.makeMove(row, col, TicTacToeBoard.PLAYER)) {
                    boardPanel.repaint();
                    if (checkEnd()) return;
                    statusLabel.setText("AI is thinking...");
                    Timer delay = new Timer(300, ev -> {
                        int[] move = ai.getBestMove(board);
                        board.makeMove(move[0], move[1], TicTacToeBoard.AI);
                        boardPanel.repaint();
                        checkEnd();
                    });
                    delay.setRepeats(false);
                    delay.start();
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) { hoveredRow = row; hoveredCol = col; boardPanel.repaint(); }
            @Override
            public void mouseExited(MouseEvent e)  { hoveredRow = -1; hoveredCol = -1; boardPanel.repaint(); }
        });

        return cell;
    }

    private boolean isWinningCell(int row, int col) {
        if (winLine == null) return false;
        int flat = row * 3 + col;
        for (int w : winLine) if (w == flat) return true;
        return false;
    }

    private boolean checkEnd() {
        int winner = board.checkWinner();
        if (winner == TicTacToeBoard.PLAYER) {
            gameOver = true;
            winLine = findWinLine();
            statusLabel.setText("✦ YOU WIN! ✦ — New Game?");
            statusLabel.setForeground(PLAYER_COLOR);
            boardPanel.repaint();
            return true;
        } else if (winner == TicTacToeBoard.AI) {
            gameOver = true;
            winLine = findWinLine();
            statusLabel.setText("✦ AI WINS ✦ — New Game?");
            statusLabel.setForeground(AI_COLOR);
            boardPanel.repaint();
            return true;
        } else if (board.isDraw()) {
            gameOver = true;
            statusLabel.setText("◈ DRAW ◈ — New Game?");
            statusLabel.setForeground(new Color(200, 200, 0));
            return true;
        }
        if (board.getCurrentPlayer() == TicTacToeBoard.PLAYER)
            statusLabel.setText("Your turn — click a cell");
        return false;
    }

    private int[] findWinLine() {
        // Rows
        for (int i = 0; i < 3; i++) {
            if (board.getCell(i,0) != TicTacToeBoard.EMPTY &&
                board.getCell(i,0) == board.getCell(i,1) && board.getCell(i,1) == board.getCell(i,2))
                return new int[]{i*3, i*3+1, i*3+2};
        }
        // Cols
        for (int j = 0; j < 3; j++) {
            if (board.getCell(0,j) != TicTacToeBoard.EMPTY &&
                board.getCell(0,j) == board.getCell(1,j) && board.getCell(1,j) == board.getCell(2,j))
                return new int[]{j, 3+j, 6+j};
        }
        // Diagonals
        if (board.getCell(0,0) != TicTacToeBoard.EMPTY &&
            board.getCell(0,0) == board.getCell(1,1) && board.getCell(1,1) == board.getCell(2,2))
            return new int[]{0, 4, 8};
        if (board.getCell(0,2) != TicTacToeBoard.EMPTY &&
            board.getCell(0,2) == board.getCell(1,1) && board.getCell(1,1) == board.getCell(2,0))
            return new int[]{2, 4, 6};
        return null;
    }

    private void resetGame() {
        board.reset();
        gameOver = false;
        winLine = null;
        statusLabel.setForeground(STATUS_COLOR);
        statusLabel.setText("Your turn — click a cell");
        boardPanel.repaint();
    }

    // GoL animated background panel
    private static class GoLBackground extends JPanel {
        private boolean[][] cells;
        private static final int COLS = 60;
        private static final int ROWS = 50;
        private static final Color CELL_COLOR = new Color(0, 60, 20, 100);
        private static final Color BG_COLOR = new Color(15, 15, 20);

        public GoLBackground() {
            cells = new boolean[ROWS][COLS];
            Random rng = new Random();
            for (int r = 0; r < ROWS; r++)
                for (int c = 0; c < COLS; c++)
                    cells[r][c] = rng.nextDouble() < 0.15;
            setBackground(BG_COLOR);
        }

        public void step() {
            boolean[][] next = new boolean[ROWS][COLS];
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    int n = 0;
                    for (int dr = -1; dr <= 1; dr++)
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            int nr = (r + dr + ROWS) % ROWS;
                            int nc = (c + dc + COLS) % COLS;
                            if (cells[nr][nc]) n++;
                        }
                    next[r][c] = cells[r][c] ? (n == 2 || n == 3) : (n == 3);
                }
            }
            cells = next;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int cw = getWidth()  / COLS;
            int ch = getHeight() / ROWS;
            if (cw < 1) cw = 1;
            if (ch < 1) ch = 1;
            g.setColor(CELL_COLOR);
            for (int r = 0; r < ROWS; r++)
                for (int c = 0; c < COLS; c++)
                    if (cells[r][c])
                        g.fillRect(c * cw, r * ch, cw, ch);
        }
    }
}

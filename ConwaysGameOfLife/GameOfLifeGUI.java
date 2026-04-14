import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class GameOfLifeGUI extends JFrame {
    private GameOfLifeBoard board;
    private GridPanel gridPanel;
    private Timer timer;
    private JLabel generationLabel;
    private JLabel populationLabel;
    private boolean running = false;
    private int cellSize = 12;
    private int boardW = 80;
    private int boardH = 60;

    private static final Color COLOR_ALIVE   = new Color(0, 230, 64);
    private static final Color COLOR_AGING   = new Color(0, 160, 40);
    private static final Color COLOR_DEAD    = new Color(15, 15, 20);
    private static final Color COLOR_GRID    = new Color(30, 30, 40);
    private static final Color COLOR_BG      = new Color(10, 10, 15);
    private static final Color COLOR_TOOLBAR = new Color(20, 20, 30);
    private static final Color COLOR_TEXT    = new Color(0, 255, 65);
    private static final Color COLOR_BUTTON  = new Color(30, 60, 30);
    private static final Color COLOR_BUTTON_BORDER = new Color(0, 200, 50);

    public GameOfLifeGUI() {
        board = new GameOfLifeBoard(boardW, boardH);
        board.randomize();
        initUI();
        timer = new Timer(100, e -> step());
    }

    private void initUI() {
        setTitle("Conway's Game of Life");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(COLOR_BG);
        getContentPane().setBackground(COLOR_BG);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(COLOR_BG);

        gridPanel = new GridPanel();
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBackground(COLOR_BG);
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BUTTON_BORDER, 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(createToolbar(), BorderLayout.NORTH);
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 400));
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(COLOR_TOOLBAR);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BUTTON_BORDER));

        JButton startBtn   = makeButton("▶ Start");
        JButton stopBtn    = makeButton("⏸ Stop");
        JButton stepBtn    = makeButton("⏭ Step");
        JButton resetBtn   = makeButton("↺ Reset");
        JButton randomBtn  = makeButton("⚡ Random");

        startBtn.addActionListener(e -> start());
        stopBtn.addActionListener(e -> stop());
        stepBtn.addActionListener(e -> step());
        resetBtn.addActionListener(e -> reset());
        randomBtn.addActionListener(e -> { stop(); board.randomize(); gridPanel.repaint(); });

        toolbar.add(startBtn);
        toolbar.add(stopBtn);
        toolbar.add(stepBtn);
        toolbar.add(resetBtn);
        toolbar.add(randomBtn);

        toolbar.add(Box.createHorizontalStrut(16));

        JLabel speedLabel = makeLabel("Speed:");
        JSlider speedSlider = new JSlider(1, 60, 10);
        speedSlider.setBackground(COLOR_TOOLBAR);
        speedSlider.setForeground(COLOR_TEXT);
        speedSlider.setPreferredSize(new Dimension(120, 30));
        speedSlider.addChangeListener(e -> {
            int fps = speedSlider.getValue();
            timer.setDelay(1000 / fps);
        });
        toolbar.add(speedLabel);
        toolbar.add(speedSlider);

        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(makeLabel("Pattern:"));

        Pattern[] patterns = {
            Pattern.glider(),
            Pattern.gosperGliderGun(),
            Pattern.pulsar(),
            Pattern.blinker(),
            Pattern.beacon()
        };
        JComboBox<Pattern> patternBox = new JComboBox<>(patterns);
        patternBox.setBackground(COLOR_BUTTON);
        patternBox.setForeground(COLOR_TEXT);
        patternBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? COLOR_BUTTON_BORDER : COLOR_BUTTON);
                setForeground(COLOR_TEXT);
                return this;
            }
        });

        JButton placeBtn = makeButton("Place");
        placeBtn.addActionListener(e -> {
            Pattern p = (Pattern) patternBox.getSelectedItem();
            if (p != null) {
                stop();
                board.clear();
                board.setCellPattern(p, 5, 5);
                gridPanel.repaint();
            }
        });

        toolbar.add(patternBox);
        toolbar.add(placeBtn);
        return toolbar;
    }

    private JPanel createStatusBar() {
        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        status.setBackground(COLOR_TOOLBAR);
        status.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BUTTON_BORDER));
        generationLabel  = makeLabel("Generation: 0");
        populationLabel  = makeLabel("Population: 0");
        JLabel helpLabel = makeLabel(" | Click: toggle cell | Drag: paint");
        helpLabel.setForeground(new Color(0, 140, 40));
        status.add(generationLabel);
        status.add(populationLabel);
        status.add(helpLabel);
        return status;
    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(COLOR_BUTTON);
        btn.setForeground(COLOR_TEXT);
        btn.setBorder(BorderFactory.createLineBorder(COLOR_BUTTON_BORDER, 1));
        btn.setFocusPainted(false);
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(40, 90, 40)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(COLOR_BUTTON); }
        });
        return btn;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(COLOR_TEXT);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return lbl;
    }

    private void start() { if (!running) { running = true; timer.start(); } }
    private void stop()  { running = false; timer.stop(); }
    private void reset() { stop(); board.clear(); gridPanel.repaint(); updateStatus(); }

    private void step() {
        board.nextGeneration();
        gridPanel.repaint();
        updateStatus();
    }

    private void updateStatus() {
        int pop = 0;
        for (int y = 0; y < board.getHeight(); y++)
            for (int x = 0; x < board.getWidth(); x++)
                if (board.getCell(x, y)) pop++;
        generationLabel.setText("Generation: " + board.getGeneration());
        populationLabel.setText("Population: " + pop);
    }

    // Inner panel that renders the grid
    private class GridPanel extends JPanel {
        private boolean painting = false;
        private boolean paintValue = true;

        public GridPanel() {
            setPreferredSize(new Dimension(boardW * cellSize, boardH * cellSize));
            setBackground(COLOR_BG);

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int x = e.getX() / cellSize;
                    int y = e.getY() / cellSize;
                    if (x >= 0 && x < board.getWidth() && y >= 0 && y < board.getHeight()) {
                        paintValue = !board.getCell(x, y);
                        painting = true;
                        board.setCell(x, y, paintValue);
                        repaint();
                        updateStatus();
                    }
                }
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!painting) return;
                    int x = e.getX() / cellSize;
                    int y = e.getY() / cellSize;
                    if (x >= 0 && x < board.getWidth() && y >= 0 && y < board.getHeight()) {
                        board.setCell(x, y, paintValue);
                        repaint();
                        updateStatus();
                    }
                }
                @Override
                public void mouseReleased(MouseEvent e) { painting = false; }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            int w = board.getWidth();
            int h = board.getHeight();

            // Draw dead cells and grid lines together
            g2.setColor(COLOR_DEAD);
            g2.fillRect(0, 0, w * cellSize, h * cellSize);

            // Draw grid lines
            g2.setColor(COLOR_GRID);
            for (int x = 0; x <= w; x++)
                g2.drawLine(x * cellSize, 0, x * cellSize, h * cellSize);
            for (int y = 0; y <= h; y++)
                g2.drawLine(0, y * cellSize, w * cellSize, y * cellSize);

            // Draw alive cells
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (board.getCell(x, y)) {
                        g2.setColor(COLOR_ALIVE);
                        g2.fillRect(x * cellSize + 1, y * cellSize + 1, cellSize - 1, cellSize - 1);
                        // Glow effect
                        g2.setColor(new Color(100, 255, 100, 60));
                        g2.fillRect(x * cellSize, y * cellSize, cellSize + 1, cellSize + 1);
                    }
                }
            }
        }
    }
}

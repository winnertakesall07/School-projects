import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * TitleScreen – Animated splash screen shown before the game starts.
 *
 * Features:
 *   • Twinkling starfield background with dark-blue gradient
 *   • Pulsing "JEOPARDY!" title painted via Graphics2D
 *   • Two team-name text fields
 *   • Hover-animated "Start" button
 *
 * When the user clicks "SPIEL STARTEN!" this frame is disposed and
 * a new JeopardyGameV2 is created with the entered team names.
 */
public class TitleScreen extends JFrame {

    // ── Colours ──────────────────────────────────────────────
    private static final Color C_BG_TOP    = new Color(2,   5, 110);
    private static final Color C_BG_BTM    = new Color(0,   0,  20);
    private static final Color C_GOLD      = new Color(255, 215,  0);
    private static final Color C_WHITE     = Color.WHITE;
    private static final Color C_FIELD_BG  = new Color(0,   0,  80);
    private static final Color C_SUBTITLE  = new Color(190, 190, 255);
    // ────────────────────────────────────────────────────────

    // ── Stars ────────────────────────────────────────────────
    private static final int NUM_STARS = 130;
    private final int[]   starX, starY, starR;
    private final float[] starPhase;           // oscillation phase per star
    // ────────────────────────────────────────────────────────

    private final JTextField[] nameFields = new JTextField[2];
    private Timer animTimer;

    // ── Constructor ──────────────────────────────────────────

    public TitleScreen() {
        setTitle("Jeopardy! – Team-Auswahl");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Initialise stars with random positions / phases
        Random rng = new Random();
        starX     = new int[NUM_STARS];
        starY     = new int[NUM_STARS];
        starR     = new int[NUM_STARS];
        starPhase = new float[NUM_STARS];
        for (int i = 0; i < NUM_STARS; i++) {
            starX[i]     = rng.nextInt(900);
            starY[i]     = rng.nextInt(620);
            starR[i]     = rng.nextInt(2) + 1;
            starPhase[i] = rng.nextFloat() * 2f * (float) Math.PI;
        }

        buildUI();

        setMinimumSize(new Dimension(780, 560));
        pack();
        setLocationRelativeTo(null);

        // Start animation timer (≈ 20 fps is enough for stars + pulse)
        animTimer = new Timer(50, e -> getContentPane().repaint());
        animTimer.start();

        setVisible(true);
    }

    // ── Build UI ─────────────────────────────────────────────

    private void buildUI() {

        // Root: custom-painted background
        JPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout(0, 28));
        root.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 80));

        // --- Pulsing title -----------------------------------------------
        PulsingTitle titleComp = new PulsingTitle();
        root.add(titleComp, BorderLayout.NORTH);

        // --- Centre: subtitle + team-name fields -------------------------
        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));

        JLabel subtitle = new JLabel("Nietzsche: \u00abGott ist tot\u00bb", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.ITALIC, 22));
        subtitle.setForeground(C_SUBTITLE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centre.add(subtitle);
        centre.add(Box.createVerticalStrut(40));

        // Team-name input grid
        JPanel nameGrid = new JPanel(new GridLayout(2, 2, 22, 14));
        nameGrid.setOpaque(false);
        nameGrid.setMaximumSize(new Dimension(520, 110));
        nameGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] defaults = {"Team 1", "Team 2"};
        for (int i = 0; i < 2; i++) {
            JLabel lbl = new JLabel("Team " + (i + 1) + "  Name:", SwingConstants.RIGHT);
            lbl.setFont(new Font("Arial", Font.BOLD, 18));
            lbl.setForeground(C_GOLD);

            nameFields[i] = new JTextField(defaults[i], 18);
            nameFields[i].setFont(new Font("Arial", Font.PLAIN, 18));
            nameFields[i].setBackground(C_FIELD_BG);
            nameFields[i].setForeground(C_WHITE);
            nameFields[i].setCaretColor(C_WHITE);
            nameFields[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(C_GOLD, 2),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));

            nameGrid.add(lbl);
            nameGrid.add(nameFields[i]);
        }
        centre.add(nameGrid);
        root.add(centre, BorderLayout.CENTER);

        // --- Start button ------------------------------------------------
        JButton startBtn = new JButton("\u25B6  SPIEL STARTEN!");
        startBtn.setFont(new Font("Impact", Font.PLAIN, 30));
        startBtn.setForeground(new Color(0, 0, 60));
        startBtn.setBackground(C_GOLD);
        startBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_WHITE, 2),
                BorderFactory.createEmptyBorder(14, 48, 14, 48)));
        startBtn.setFocusPainted(false);
        startBtn.setOpaque(true);
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { startBtn.setBackground(new Color(255, 233, 60)); }
            public void mouseExited(MouseEvent e)  { startBtn.setBackground(C_GOLD); }
        });
        startBtn.addActionListener(e -> launchGame());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(startBtn);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ── Start the game ───────────────────────────────────────

    private void launchGame() {
        animTimer.stop();
        String n1 = nameFields[0].getText().trim();
        String n2 = nameFields[1].getText().trim();
        if (n1.isEmpty()) n1 = "Team 1";
        if (n2.isEmpty()) n2 = "Team 2";
        dispose();
        new JeopardyGameV2(new String[]{n1, n2});
    }

    // ════════════════════════════════════════════════════════
    //  Inner component – animated gradient + starfield panel
    // ════════════════════════════════════════════════════════

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Gradient fill
            GradientPaint gp = new GradientPaint(0, 0, C_BG_TOP, 0, getHeight(), C_BG_BTM);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Stars (twinkling via sine oscillation)
            long now = System.currentTimeMillis();
            for (int i = 0; i < NUM_STARS; i++) {
                float phase = starPhase[i] + now * 0.0015f;
                float brightness = 0.45f + 0.55f * (float) Math.abs(Math.sin(phase));
                int c = Math.min(255, (int) (brightness * 255));
                g2.setColor(new Color(c, c, c));
                int x = starX[i] % getWidth();
                int y = starY[i] % getHeight();
                g2.fillOval(x, y, starR[i], starR[i]);
            }
        }
    }

    // ════════════════════════════════════════════════════════
    //  Inner component – pulsing "JEOPARDY!" title
    // ════════════════════════════════════════════════════════

    private static class PulsingTitle extends JComponent {
        private static final float MIN_SZ = 80f;
        private static final float MAX_SZ = 92f;
        private float fontSize = MIN_SZ;
        private boolean growing = true;

        PulsingTitle() {
            setOpaque(false);
            setPreferredSize(new Dimension(700, 120));
            new Timer(40, e -> {
                if (growing) { fontSize += 0.6f; if (fontSize >= MAX_SZ) growing = false; }
                else         { fontSize -= 0.6f; if (fontSize <= MIN_SZ) growing = true; }
                repaint();
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Font font = new Font("Impact", Font.PLAIN, (int) fontSize);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            String text = "JEOPARDY!";
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = getHeight() / 2 + fm.getAscent() / 2 - 4;

            // Drop shadow
            g2.setColor(new Color(0, 0, 120, 160));
            g2.drawString(text, x + 4, y + 4);

            // Gold text
            g2.setColor(new Color(255, 215, 0));
            g2.drawString(text, x, y);
        }
    }

    // ── Entry point (for standalone testing only) ────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(TitleScreen::new);
    }
}

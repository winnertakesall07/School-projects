import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * QuestionDialogV2 – Full-screen-ish modal shown when a question tile is clicked.
 *
 * Improvements over V1:
 *   • Only 2 team award buttons (not 3).
 *   • Fade-in animation when the dialog opens (opacity 0 → 1).
 *   • Celebration glass-pane animation when a team answers correctly:
 *       a full-dialog green overlay pulses three times then auto-closes.
 *
 * Flow:
 *   Phase 1 → Question is displayed, answer is hidden.
 *   Phase 2 → Host clicks "ANTWORT ZEIGEN"; answer is revealed.
 *             Host awards the points to a team (or nobody).
 *
 * Returns the team index (1 or 2) who earned the points, or -1 if nobody.
 */
public class QuestionDialogV2 extends JDialog {

    // ── Colour palette ────────────────────────────────────────
    private static final Color C_BG      = new Color(4,   8, 180);
    private static final Color C_BG2     = new Color(0,   0,  60);
    private static final Color C_CELL    = new Color(0,   0, 120);
    private static final Color C_GOLD    = new Color(255, 215,   0);
    private static final Color C_WHITE   = Color.WHITE;
    private static final Color C_GREEN   = new Color(0,  145,   0);
    private static final Color C_RED     = new Color(170,  0,   0);
    private static final Color C_PURPLE  = new Color(90,   0, 160);
    // ────────────────────────────────────────────────────────

    private int result = -1;   // -1 = nobody; 1 or 2 = team index

    /** @param teamNames Array of exactly 2 team-name strings. */
    public QuestionDialogV2(JFrame parent, Question q, int points,
                            SoundManager sound, String[] teamNames) {
        super(parent, "Frage \u2013 $" + points, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackground(C_BG);
        buildUI(q, points, sound, teamNames);
        startFadeIn();
    }

    // ── Build UI ─────────────────────────────────────────────

    private void buildUI(Question q, int points, SoundManager sound, String[] teamNames) {

        // Root gradient panel
        JPanel root = new GradientPanel(C_BG, C_BG2);
        root.setLayout(new BorderLayout(0, 0));
        root.setBorder(BorderFactory.createLineBorder(C_GOLD, 3));

        // ── North: point value ──────────────────────────────
        JLabel lblPoints = new JLabel("$" + points, SwingConstants.CENTER);
        lblPoints.setFont(new Font("Impact", Font.PLAIN, 60));
        lblPoints.setForeground(C_GOLD);
        lblPoints.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        root.add(lblPoints, BorderLayout.NORTH);

        // ── Centre: question text ───────────────────────────
        JTextArea txtQuestion = new JTextArea(q.getQuestion());
        txtQuestion.setFont(new Font("Arial", Font.BOLD, 26));
        txtQuestion.setForeground(C_WHITE);
        txtQuestion.setBackground(C_BG);
        txtQuestion.setEditable(false);
        txtQuestion.setLineWrap(true);
        txtQuestion.setWrapStyleWord(true);
        txtQuestion.setOpaque(false);
        txtQuestion.setFocusable(false);
        JScrollPane scroll = new JScrollPane(txtQuestion,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        root.add(scroll, BorderLayout.CENTER);

        // ── Answer panel (hidden until revealed) ─────────────
        JPanel answerPanel = new JPanel(new BorderLayout());
        answerPanel.setBackground(C_CELL);
        answerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, C_GOLD),
                BorderFactory.createEmptyBorder(10, 40, 10, 40)));
        answerPanel.setVisible(false);

        JLabel lblAnswerHead = new JLabel("\uD83D\uDCA1  ANTWORT:", SwingConstants.LEFT);
        lblAnswerHead.setFont(new Font("Arial", Font.BOLD, 16));
        lblAnswerHead.setForeground(C_GOLD);

        JTextArea txtAnswer = new JTextArea(q.getAnswer());
        txtAnswer.setFont(new Font("Arial", Font.PLAIN, 20));
        txtAnswer.setForeground(C_WHITE);
        txtAnswer.setBackground(C_CELL);
        txtAnswer.setEditable(false);
        txtAnswer.setLineWrap(true);
        txtAnswer.setWrapStyleWord(true);
        txtAnswer.setOpaque(true);
        txtAnswer.setFocusable(false);

        answerPanel.add(lblAnswerHead, BorderLayout.NORTH);
        answerPanel.add(txtAnswer, BorderLayout.CENTER);

        // ── Button strip ────────────────────────────────────
        // Layout: [Reveal] [Team1✅] [Team2✅] [Nobody❌]  (4 columns)
        JPanel btnStrip = new JPanel(new GridLayout(1, 4, 8, 0));
        btnStrip.setBackground(C_BG);
        btnStrip.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        JButton btnReveal = makeButton("\uD83D\uDCA1 ANTWORT\nZEIGEN", C_GREEN);
        btnReveal.addActionListener(e -> {
            sound.playRevealSound();
            answerPanel.setVisible(true);
            pack();
            setLocationRelativeTo(getParent());
        });

        JButton btnT1 = makeButton("\u2705 " + teamNames[0], C_PURPLE);
        JButton btnT2 = makeButton("\u2705 " + teamNames[1], C_PURPLE);
        btnT1.addActionListener(e -> awardTeam(1, teamNames[0], points, sound));
        btnT2.addActionListener(e -> awardTeam(2, teamNames[1], points, sound));

        JButton btnNone = makeButton("\u274C NIEMAND", C_RED);
        btnNone.addActionListener(e -> {
            sound.playWrongSound();
            dispose();
        });

        btnStrip.add(btnReveal);
        btnStrip.add(btnT1);
        btnStrip.add(btnT2);
        btnStrip.add(btnNone);

        // Wrap answer + buttons in one south block
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(C_BG);
        south.add(answerPanel, BorderLayout.CENTER);
        south.add(btnStrip,    BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
        setPreferredSize(new Dimension(780, 500));
        pack();
        setLocationRelativeTo(getParent());
    }

    // ── Fade-in animation ─────────────────────────────────────

    private void startFadeIn() {
        try {
            setOpacity(0f);
        } catch (Exception ignored) {
            // Translucency not supported – just show immediately
            return;
        }
        final float[] op = {0f};
        Timer t = new Timer(16, null);
        t.addActionListener(e -> {
            op[0] = Math.min(1f, op[0] + 0.06f);
            try { setOpacity(op[0]); } catch (Exception ignored) {}
            if (op[0] >= 1f) ((Timer) e.getSource()).stop();
        });
        t.start();
    }

    // ── Award team + celebration animation ───────────────────

    private void awardTeam(int team, String name, int points, SoundManager sound) {
        sound.playCorrectSound();
        result = team;

        // Build celebration glass pane
        CelebrationPane glass = new CelebrationPane(name, points);
        setGlassPane(glass);
        glass.setVisible(true);

        // After animation (≈ 1.8 s) close the dialog
        Timer closeTimer = new Timer(1800, e -> dispose());
        closeTimer.setRepeats(false);
        closeTimer.start();
    }

    // ── Helpers ───────────────────────────────────────────────

    private JButton makeButton(String label, Color bg) {
        String html = "<html><center>" + label.replace("\n", "<br>") + "</center></html>";
        JButton btn = new JButton(html);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(C_WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(C_GOLD, 2));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 60));
        // Slight hover brightening
        btn.addMouseListener(new MouseAdapter() {
            final Color orig = bg;
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(orig.brighter());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(orig);
            }
        });
        return btn;
    }

    /** Returns 1 or 2 for the winning team, or -1 if nobody. */
    public int getResult() { return result; }

    // ════════════════════════════════════════════════════════
    //  Inner class – gradient background panel
    // ════════════════════════════════════════════════════════

    static class GradientPanel extends JPanel {
        private final Color top, bottom;
        GradientPanel(Color top, Color bottom) {
            this.top = top; this.bottom = bottom;
            setOpaque(false); // prevent super from filling over the gradient
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g2.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g); // safe: opaque=false so no background fill
        }
    }

    // ════════════════════════════════════════════════════════
    //  Inner class – celebration glass-pane overlay
    // ════════════════════════════════════════════════════════

    private class CelebrationPane extends JComponent {

        private final String teamName;
        private final int    pts;

        // Animation state: alpha 0→1 (pulse 3 times)
        private float   alpha      = 0f;
        private boolean increasing = true;
        private int     pulseCount = 0;

        CelebrationPane(String teamName, int pts) {
            this.teamName = teamName;
            this.pts      = pts;
            setOpaque(false);

            Timer t = new Timer(30, null);
            t.addActionListener(e -> {
                if (increasing) {
                    alpha += 0.06f;
                    if (alpha >= 1f) { alpha = 1f; increasing = false; }
                } else {
                    alpha -= 0.06f;
                    if (alpha <= 0f) {
                        alpha = 0f;
                        pulseCount++;
                        if (pulseCount < 3) {
                            increasing = true;
                        } else {
                            ((Timer) e.getSource()).stop();
                        }
                    }
                }
                repaint();
            });
            t.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (alpha <= 0f) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Green overlay
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.88f));
            g2.setColor(new Color(0, 160, 30));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // "RICHTIG!" text
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setFont(new Font("Impact", Font.PLAIN, 82));
            FontMetrics fm = g2.getFontMetrics();
            String richtig = "\u2705  RICHTIG!";
            int rx = (getWidth() - fm.stringWidth(richtig)) / 2;
            // Shadow
            g2.setColor(new Color(0, 80, 0));
            g2.drawString(richtig, rx + 4, getHeight() / 2 - 10 + 4);
            // Main
            g2.setColor(Color.WHITE);
            g2.drawString(richtig, rx, getHeight() / 2 - 10);

            // Team name + points
            g2.setFont(new Font("Arial", Font.BOLD, 28));
            fm = g2.getFontMetrics();
            String sub = "+" + pts + " Punkte f\u00fcr " + teamName + "!";
            int sx = (getWidth() - fm.stringWidth(sub)) / 2;
            g2.setColor(new Color(255, 230, 0));
            g2.drawString(sub, sx, getHeight() / 2 + 60);
        }
    }
}

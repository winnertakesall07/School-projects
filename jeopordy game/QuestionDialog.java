import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * QuestionDialog – Full-screen-ish modal shown when a question tile is clicked.
 *
 * Flow:
 *   Phase 1 → Question is displayed, answer is hidden.
 *   Phase 2 → Host clicks "ANTWORT ZEIGEN"; answer is revealed.
 *             Host then awards the points to the correct player (or nobody).
 *
 * The dialog returns the player index (1, 2, or 3) who earned the points,
 * or -1 if nobody answered correctly.
 */
public class QuestionDialog extends JDialog {

    // ── Jeopardy colour palette ──────────────────────────────
    private static final Color C_BG       = new Color(4,  8,  180);
    private static final Color C_CELL     = new Color(0,  0,  120);
    private static final Color C_GOLD     = new Color(255, 215, 0);
    private static final Color C_WHITE    = Color.WHITE;
    private static final Color C_GREEN    = new Color(0,  140,  0);
    private static final Color C_RED      = new Color(170, 0,   0);
    private static final Color C_PURPLE   = new Color(90,  0,  160);
    // ────────────────────────────────────────────────────────

    private int result = -1; // -1 = nobody; 1/2/3 = player

    /** @param playerNames Array of 3 player name strings. */
    public QuestionDialog(JFrame parent, Question q, int points,
                          SoundManager sound, String[] playerNames) {
        super(parent, "Frage – $" + points, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackground(C_BG);
        buildUI(q, points, sound, playerNames);
    }

    // ── Build UI ─────────────────────────────────────────────

    private void buildUI(Question q, int points, SoundManager sound, String[] playerNames) {

        // Root panel
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(C_BG);
        root.setBorder(BorderFactory.createLineBorder(C_GOLD, 3));

        // ── Top: point value ─────────────────────────────────
        JLabel lblPoints = new JLabel("$" + points, SwingConstants.CENTER);
        lblPoints.setFont(loadFont("Impact", Font.BOLD, 56));
        lblPoints.setForeground(C_GOLD);
        lblPoints.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        root.add(lblPoints, BorderLayout.NORTH);

        // ── Centre: question text ─────────────────────────────
        JTextArea txtQuestion = new JTextArea(q.getQuestion());
        txtQuestion.setFont(loadFont("Arial", Font.BOLD, 26));
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

        JLabel lblAnswerHead = new JLabel("💡  ANTWORT:", SwingConstants.LEFT);
        lblAnswerHead.setFont(loadFont("Arial", Font.BOLD, 16));
        lblAnswerHead.setForeground(C_GOLD);

        JTextArea txtAnswer = new JTextArea(q.getAnswer());
        txtAnswer.setFont(loadFont("Arial", Font.PLAIN, 20));
        txtAnswer.setForeground(C_WHITE);
        txtAnswer.setBackground(C_CELL);
        txtAnswer.setEditable(false);
        txtAnswer.setLineWrap(true);
        txtAnswer.setWrapStyleWord(true);
        txtAnswer.setOpaque(true);
        txtAnswer.setFocusable(false);

        answerPanel.add(lblAnswerHead, BorderLayout.NORTH);
        answerPanel.add(txtAnswer,     BorderLayout.CENTER);
        root.add(answerPanel, BorderLayout.SOUTH);

        // ── Button strip ─────────────────────────────────────
        JPanel btnStrip = new JPanel(new GridLayout(1, 5, 8, 0));
        btnStrip.setBackground(C_BG);
        btnStrip.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        // "Antwort zeigen" button
        JButton btnReveal = makeButton("💡 ANTWORT\nZEIGEN", C_GREEN);
        btnReveal.addActionListener(e -> {
            sound.playRevealSound();
            answerPanel.setVisible(true);
            pack();
            setLocationRelativeTo(getParent());
        });

        // Player award buttons
        JButton btnP1 = makeButton("✅ " + playerNames[0], C_PURPLE);
        JButton btnP2 = makeButton("✅ " + playerNames[1], C_PURPLE);
        JButton btnP3 = makeButton("✅ " + playerNames[2], C_PURPLE);
        btnP1.addActionListener(e -> awardPlayer(1, playerNames[0], points, sound));
        btnP2.addActionListener(e -> awardPlayer(2, playerNames[1], points, sound));
        btnP3.addActionListener(e -> awardPlayer(3, playerNames[2], points, sound));

        // "Niemand" button
        JButton btnNone = makeButton("❌ NIEMAND", C_RED);
        btnNone.addActionListener(e -> {
            sound.playWrongSound();
            dispose();
        });

        btnStrip.add(btnReveal);
        btnStrip.add(btnP1);
        btnStrip.add(btnP2);
        btnStrip.add(btnP3);
        btnStrip.add(btnNone);

        // Wrap centre + answerPanel + btnStrip
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(C_BG);
        south.add(answerPanel, BorderLayout.CENTER);
        south.add(btnStrip,    BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
        setPreferredSize(new Dimension(760, 480));
        pack();
        setLocationRelativeTo(getParent());
    }

    // ── Actions ───────────────────────────────────────────────

    private void awardPlayer(int player, String name, int points, SoundManager sound) {
        sound.playCorrectSound();
        JOptionPane.showMessageDialog(this,
                "<html><center>"
                + "<font style='font-size:28pt;color:#00DD00;'>✅ RICHTIG!</font><br><br>"
                + "<font style='font-size:16pt;color:#FFD700;'>+"
                + points + " Punkte für <b>" + name + "</b>!</font>"
                + "</center></html>",
                "Richtig!", JOptionPane.PLAIN_MESSAGE);
        result = player;
        dispose();
    }

    // ── Helpers ───────────────────────────────────────────────

    /** Themed button with multiline label support (use \n). */
    private JButton makeButton(String label, Color bg) {
        String html = "<html><center>" + label.replace("\n", "<br>") + "</center></html>";
        JButton btn = new JButton(html);
        btn.setFont(loadFont("Arial", Font.BOLD, 13));
        btn.setForeground(C_WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(C_GOLD, 2));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 60));
        return btn;
    }

    private Font loadFont(String family, int style, int size) {
        return new Font(family, style, size);
    }

    /** Returns 1/2/3 for the winning player, or -1 if nobody. */
    public int getResult() { return result; }
}

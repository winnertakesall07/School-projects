import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ============================================================
 *  JeopardyGameV2 – 2-Team Edition
 *  Jeopardy-Spielbrett für den Unterricht (BlueJ-kompatibel)
 * ============================================================
 *
 *  STARTEN:
 *    Rechtsklick auf JeopardyGameV2 → main(String[]) → OK
 *    → Titelbildschirm erscheint → Teamnamen eingeben → Start!
 *
 *  FRAGEN ANPASSEN:
 *    Scrolle zur Methode initializeQuestions() weiter unten.
 *
 *  Unterschiede zu V1:
 *    • Nur 2 Teams (nicht 3)
 *    • Titelbildschirm mit Namenseingabe vor dem Spiel
 *    • Schönere Benutzeroberfläche mit Farbverläufen
 *    • Animationen beim Öffnen von Fragen und bei richtigen Antworten
 * ============================================================
 */
public class JeopardyGameV2 extends JFrame {

    // ── Team-Namen (werden vom Titelbildschirm übergeben) ────
    private String[] teamNames;

    // ── Kategorien ───────────────────────────────────────────
    private static final String[] CATEGORIES = {
        "NIETZSCHE\nGRUNDLAGEN",
        "MORAL\n& WERTE",
        "NIHILISMUS",
        "\"GOTT\nIST TOT\"",
        "KONSE-\nQUENZEN"
    };

    // ── Punktwerte ───────────────────────────────────────────
    private static final int[] POINTS = { 100, 200, 300, 400, 500 };

    // ── Farbpalette ──────────────────────────────────────────
    private static final Color C_BG_TOP     = new Color(4,   10, 200);
    private static final Color C_BG_BTM     = new Color(0,    0,  60);
    private static final Color C_CELL       = new Color(0,    0, 120);
    private static final Color C_CELL_HOVER = new Color(15,  30, 210);
    private static final Color C_CELL_DONE  = new Color(0,    0,  50);
    private static final Color C_GOLD       = new Color(255, 215,   0);
    private static final Color C_WHITE      = Color.WHITE;
    private static final Color C_GREY       = new Color(80,  80, 100);
    private static final Color C_RED_BTN    = new Color(150,   0,   0);
    private static final Color C_SCORE_BG   = new Color(0,    0,  90);
    // ────────────────────────────────────────────────────────

    // ── Spielzustand ─────────────────────────────────────────
    private Question[][] questions;
    private boolean[][]  answered;
    private int[]        scores = { 0, 0 };

    private JButton[][]  boardButtons;
    private JLabel[]     scoreLabels;
    private SoundManager sound;

    // ── Puls-Animation für Punkte ────────────────────────────
    private static final Color C_PULSE_ON  = new Color(0,  200,  60);
    private static final Color C_PULSE_OFF = C_SCORE_BG;

    // ─────────────────────────────────────────────────────────
    //  Konstruktor
    // ─────────────────────────────────────────────────────────

    public JeopardyGameV2(String[] teamNames) {
        this.teamNames = teamNames;
        sound = new SoundManager();
        initializeQuestions();
        buildWindow();
        sound.playBackgroundMusic();
    }

    // ─────────────────────────────────────────────────────────
    //  FRAGEN – hier anpassen!
    // ─────────────────────────────────────────────────────────

    private void initializeQuestions() {
        questions = new Question[5][5];
        answered  = new boolean[5][5];

        // ════════════════════════════════════════════════════
        //  KATEGORIE 0 – NIETZSCHE GRUNDLAGEN
        // ════════════════════════════════════════════════════

        questions[0][0] = new Question(
            "Wogegen argumentiert Nietzsche NICHT direkt?",
            "Gegen die Existenz Gottes – Nietzsche widerlegt Gott nicht, er setzt dessen kulturellen Bedeutungsverlust voraus"
        );
        questions[0][1] = new Question(
            "Was setzt Nietzsche in seinem Denken über die Menschen voraus?",
            "Dass viele Menschen bereits nicht mehr an Gott glauben – der Glaube hat in der Gesellschaft schon an Bedeutung verloren"
        );
        questions[0][2] = new Question(
            "Worauf liegt Nietzsches eigentlicher Fokus in seiner Philosophie?",
            "Auf den Folgen des Glaubensverlustes – was passiert mit Moral, Sinn und Werten, wenn der Glaube wegfällt"
        );
        questions[0][3] = new Question(
            "Warum lehnt Nietzsche eine übernatürliche Begründung von Werten ab?",
            "Weil der Mensch Teil der Natur ist – Nietzsche sieht keine Notwendigkeit für eine göttliche Grundlage menschlicher Werte"
        );
        questions[0][4] = new Question(
            "Wovor warnt Nietzsche in seiner Philosophie des Gottesverlustes?",
            "Vor Sinnverlust und dem Zusammenbruch moralischer Werte – die Menschen erkennen die Schwere dieser Konsequenzen nicht"
        );

        // ════════════════════════════════════════════════════
        //  KATEGORIE 1 – MORAL & WERTE
        // ════════════════════════════════════════════════════

        questions[1][0] = new Question(
            "Worauf basiert traditionelle Moral laut Nietzsche?",
            "Auf dem Glauben an Gott – Gott ist das Fundament, das der Moral ihren Anspruch und ihre Verbindlichkeit gibt"
        );
        questions[1][1] = new Question(
            "Woran ist Moral laut Nietzsche historisch gebunden?",
            "An den Gottesglauben – Moral und Glaube sind in der Geschichte untrennbar miteinander verknüpft"
        );
        questions[1][2] = new Question(
            "Was fehlt ohne Gott laut Nietzsche?",
            "Ein höherer Sinn und ein Fundament für Werte – ohne Gott verlieren moralische Gebote ihre Grundlage und Begründung"
        );
        questions[1][3] = new Question(
            "Was ist laut Nietzsche widersprüchlich?",
            "Der Versuch, Moral ohne Gott zu erhalten – wer Gott ablehnt, aber die christliche Moral beibehält, denkt nicht zu Ende"
        );
        questions[1][4] = new Question(
            "Welche Konsequenz zieht Nietzsche daraus, dass Moral historisch an Gottesglauben gebunden ist?",
            "Ohne Gott verliert die Moral ihr Fundament vollständig – eine säkulare Moral, die dieselben Ansprüche erhebt, ist nicht begründbar"
        );

        // ════════════════════════════════════════════════════
        //  KATEGORIE 2 – NIHILISMUS
        // ════════════════════════════════════════════════════

        questions[2][0] = new Question(
            "Wie nennt Nietzsche die Folge des Gottesverlustes?",
            "Nihilismus – der Zustand, in dem das Leben keinen höheren Sinn, kein Ziel und keine Werte mehr hat"
        );
        questions[2][1] = new Question(
            "Wie erscheint das Leben im Nihilismus laut Nietzsche?",
            "Sinnlos – ohne höheren Zweck fehlt ein Grund, dem Leben Bedeutung zuzuschreiben"
        );
        questions[2][2] = new Question(
            "Welche Bedeutung hatte Leid früher in einer religiösen Weltanschauung?",
            "Eine religiöse Bedeutung – Leid hatte einen Sinn (z.B. Prüfung, Läuterung, Teil eines göttlichen Plans)"
        );
        questions[2][3] = new Question(
            "Was geschieht mit Leid ohne Gott laut Nietzsche?",
            "Leid bleibt ohne Rechtfertigung – es ist sinnlos und nicht mehr in ein größeres Weltbild eingebettet"
        );
        questions[2][4] = new Question(
            "Welche zwei großen Verluste beschreibt Nietzsche im Nihilismus?",
            "Erstens der Verlust von Sinn (das Leben hat keinen Zweck mehr) und zweitens der Zusammenbruch moralischer Werte (kein verbindliches Gut und Böse)"
        );

        // ════════════════════════════════════════════════════
        //  KATEGORIE 3 – «GOTT IST TOT»
        // ════════════════════════════════════════════════════

        questions[3][0] = new Question(
            "Was für eine Art von Aussage ist «Gott ist tot» – eine Widerlegung oder eine Diagnose?",
            "Eine Diagnose – Nietzsche beschreibt einen kulturellen Zustand, er beweist nicht die Nicht-Existenz Gottes"
        );
        questions[3][1] = new Question(
            "Was genau beschreibt der Satz «Gott ist tot»?",
            "Den kulturellen Bedeutungsverlust des Glaubens – Gott spielt im Denken, Fühlen und Handeln der modernen Menschen kaum noch eine Rolle"
        );
        questions[3][2] = new Question(
            "Was erkennen die Menschen laut Nietzsche NICHT?",
            "Die Konsequenzen des Glaubensverlustes – sie merken nicht, wie radikal der Verlust Gottes ihr Leben, ihre Moral und ihren Sinn erschüttert"
        );
        questions[3][3] = new Question(
            "Ist «Gott ist tot» eine Aussage über die Biologie oder über Kultur?",
            "Über Kultur – Nietzsche meint damit, dass der Glaube an Gott in der modernen Gesellschaft seine prägende Kraft verloren hat"
        );
        questions[3][4] = new Question(
            "Feiert Nietzsche mit dem Satz «Gott ist tot» den Tod Gottes?",
            "Nein – Nietzsche zeigt die Konsequenzen auf und warnt. Er beschreibt den Verlust als etwas Ernstes und Gefährliches, nicht als Befreiung"
        );

        // ════════════════════════════════════════════════════
        //  KATEGORIE 4 – KONSEQUENZEN
        // ════════════════════════════════════════════════════

        questions[4][0] = new Question(
            "Welche Folge hat es laut Nietzsche, wenn man Moral ohne Gott beibehält?",
            "Es ist widersprüchlich – eine Moral, die ihre Grundlage verloren hat, aber dieselben Ansprüche stellt, kann nicht dauerhaft bestehen"
        );
        questions[4][1] = new Question(
            "Was zeigt Nietzsche durch seine Philosophie über den Gottesverlust?",
            "Er zeigt die Konsequenzen auf – Sinnverlust, Nihilismus und den Zusammenbruch moralischer Orientierung"
        );
        questions[4][2] = new Question(
            "Warum ist der Zusammenbruch moralischer Werte ohne Gott für Nietzsche unvermeidlich?",
            "Weil Moral historisch und kulturell an den Gottesglauben gebunden ist – fällt das Fundament, fällt auch das Gebäude der Werte"
        );
        questions[4][3] = new Question(
            "Was verliert das menschliche Leid ohne Gott laut Nietzsche, und warum ist das problematisch?",
            "Es verliert seinen Sinn und seine Rechtfertigung – Leid war früher in einen religiösen Rahmen eingebettet, der ihm Bedeutung gab; ohne diesen Rahmen ist es nur noch sinnloses Leiden"
        );
        questions[4][4] = new Question(
            "Fasse Nietzsches zentrale Warnung in einem Satz zusammen.",
            "Wer Gott verliert, verliert damit auch das Fundament für Sinn, Moral und die Bewältigung von Leid – und dieser Verlust ist gefährlicher, als die meisten ahnen"
        );
    }

    // ─────────────────────────────────────────────────────────
    //  UI-Aufbau
    // ─────────────────────────────────────────────────────────

    private void buildWindow() {
        setTitle("\u2605  JEOPARDY  –  Nietzsche: \u00abGott ist tot\u00bb  \u2605");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Root panel with gradient background
        JPanel root = new GradientPanel(C_BG_TOP, C_BG_BTM);
        root.setLayout(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        root.add(buildTitleBar(), BorderLayout.NORTH);
        root.add(buildBoard(),    BorderLayout.CENTER);
        root.add(buildScoreBar(), BorderLayout.SOUTH);

        setContentPane(root);
        setMinimumSize(new Dimension(1050, 720));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Titelleiste ──────────────────────────────────────────

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        // Stylised title label
        JLabel title = new JLabel("JEOPARDY!", SwingConstants.CENTER);
        title.setFont(new Font("Impact", Font.PLAIN, 70));
        title.setForeground(C_GOLD);
        // Gold text shadow via HTML isn't possible in JLabel, but we can add a visual separator
        bar.add(title, BorderLayout.CENTER);

        // Control buttons top-right
        JButton btnSound = makeSmallButton("\uD83D\uDD0A Sound AN");
        btnSound.addActionListener(e -> {
            sound.setSoundEnabled(!sound.isSoundEnabled());
            btnSound.setText(sound.isSoundEnabled() ? "\uD83D\uDD0A Sound AN" : "\uD83D\uDD07 Sound AUS");
        });

        JButton btnNames = makeSmallButton("\u270F\uFE0F Namen");
        btnNames.addActionListener(e -> editNames());

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        topRight.setOpaque(false);
        topRight.add(btnNames);
        topRight.add(btnSound);

        bar.add(topRight, BorderLayout.EAST);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return bar;
    }

    // ── Spielbrett ───────────────────────────────────────────

    private JPanel buildBoard() {
        int rows = 1 + POINTS.length;
        int cols = CATEGORIES.length;

        JPanel board = new JPanel(new GridLayout(rows, cols, 8, 8));
        board.setOpaque(false);
        boardButtons = new JButton[cols][POINTS.length];

        for (int c = 0; c < cols; c++) {
            board.add(makeCategoryHeader(CATEGORIES[c]));
        }

        for (int r = 0; r < POINTS.length; r++) {
            for (int c = 0; c < cols; c++) {
                boardButtons[c][r] = makeQuestionCell(c, r);
                board.add(boardButtons[c][r]);
            }
        }
        return board;
    }

    private JButton makeCategoryHeader(String text) {
        JButton btn = new JButton(
                "<html><center><b>" + text.replace("\n", "<br>") + "</b></center></html>");
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(C_WHITE);
        btn.setBackground(new Color(0, 0, 100));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_GOLD, 2),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        btn.setEnabled(false);
        btn.setPreferredSize(new Dimension(185, 72));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        return btn;
    }

    private JButton makeQuestionCell(final int col, final int row) {
        JButton btn = new JButton("$" + POINTS[row]);
        btn.setFont(new Font("Impact", Font.PLAIN, 38));
        btn.setForeground(C_GOLD);
        btn.setBackground(C_CELL);
        btn.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 80), 2));
        btn.setPreferredSize(new Dimension(185, 90));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addActionListener(e -> openQuestion(col, row));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!answered[col][row]) btn.setBackground(C_CELL_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                if (!answered[col][row]) btn.setBackground(C_CELL);
            }
        });
        return btn;
    }

    // ── Punkteanzeige ────────────────────────────────────────

    private JPanel buildScoreBar() {
        // 3 columns: [Reset] [Team1 panel] [Team2 panel]
        JPanel bar = new JPanel(new GridLayout(1, 3, 14, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton resetBtn = makeSmallButton("\uD83D\uDD04  RESET");
        resetBtn.setBackground(C_RED_BTN);
        resetBtn.addActionListener(e -> confirmReset());
        bar.add(resetBtn);

        scoreLabels = new JLabel[2];
        for (int i = 0; i < 2; i++) {
            bar.add(makeScorePanel(i));
        }
        return bar;
    }

    private JPanel makeScorePanel(final int idx) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setBackground(C_SCORE_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_GOLD, 2),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));

        scoreLabels[idx] = new JLabel(scoreHtml(idx), SwingConstants.CENTER);
        scoreLabels[idx].setFont(new Font("Arial", Font.BOLD, 18));
        scoreLabels[idx].setForeground(C_WHITE);
        p.add(scoreLabels[idx], BorderLayout.CENTER);

        // ± adjustment buttons
        JPanel adj = new JPanel(new GridLayout(1, 2, 2, 0));
        adj.setOpaque(false);
        JButton plus  = makeTinyButton("+");
        JButton minus = makeTinyButton("\u2212");
        plus .addActionListener(e -> adjustScore(idx, +100));
        minus.addActionListener(e -> adjustScore(idx, -100));
        adj.add(plus);
        adj.add(minus);
        p.add(adj, BorderLayout.EAST);
        p.setPreferredSize(new Dimension(280, 60));
        return p;
    }

    // ─────────────────────────────────────────────────────────
    //  Spiellogik
    // ─────────────────────────────────────────────────────────

    private void openQuestion(int col, int row) {
        if (answered[col][row]) return;
        sound.playQuestionSound();

        QuestionDialogV2 dlg = new QuestionDialogV2(
                this, questions[col][row], POINTS[row], sound, teamNames);
        dlg.setVisible(true);

        int winner = dlg.getResult();
        if (winner >= 1 && winner <= 2) {
            scores[winner - 1] += POINTS[row];
            refreshScores(winner - 1);  // with pulse animation
        }

        markAnswered(col, row);
        checkGameOver();
    }

    private void markAnswered(int col, int row) {
        answered[col][row] = true;
        JButton btn = boardButtons[col][row];
        btn.setText("\u2713");
        btn.setFont(new Font("Arial", Font.BOLD, 22));
        btn.setForeground(C_GREY);
        btn.setBackground(C_CELL_DONE);
        btn.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 40), 1));
        btn.setEnabled(false);
    }

    private void adjustScore(int playerIdx, int delta) {
        scores[playerIdx] = Math.max(0, scores[playerIdx] + delta);
        refreshScores(playerIdx);
    }

    /**
     * Refresh score labels; briefly pulse background of the updated player's
     * panel to draw attention.
     */
    private void refreshScores(int pulsedIdx) {
        for (int i = 0; i < 2; i++) {
            scoreLabels[i].setText(scoreHtml(i));
        }
        if (pulsedIdx >= 0) pulseScore(pulsedIdx);
    }

    private void pulseScore(int idx) {
        JLabel lbl = scoreLabels[idx];
        JPanel panel = (JPanel) lbl.getParent();
        final int[] ticks = {0};
        new Timer(80, e -> {
            ticks[0]++;
            panel.setBackground((ticks[0] % 2 == 0) ? C_PULSE_ON : C_SCORE_BG);
            panel.repaint();
            if (ticks[0] >= 6) {
                panel.setBackground(C_SCORE_BG);
                ((Timer) e.getSource()).stop();
            }
        }).start();
    }

    private String scoreHtml(int idx) {
        return "<html><center><b>" + teamNames[idx] + "</b><br>"
                + "<font color='#FFD700'>$" + scores[idx] + "</font></center></html>";
    }

    private void checkGameOver() {
        for (int c = 0; c < 5; c++)
            for (int r = 0; r < 5; r++)
                if (!answered[c][r]) return;
        showWinnerScreen();
    }

    private void showWinnerScreen() {
        sound.playWinnerSound();

        int best = 0;
        for (int s : scores) if (s > best) best = s;
        StringBuilder winners = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            if (scores[i] == best) {
                if (winners.length() > 0) winners.append(" &amp; ");
                winners.append(teamNames[i]);
            }
        }

        JOptionPane.showMessageDialog(this,
                "<html><center>"
                + "<font style='font-size:32pt;color:#FFD700;'>\uD83C\uDFC6 SPIEL VORBEI! \uD83C\uDFC6</font><br><br>"
                + "<font style='font-size:18pt;color:white;'>Gewinner: <b style='color:#00FF80;'>"
                + winners + "</b></font><br><br>"
                + "<font style='font-size:14pt;color:#CCCCCC;'>"
                + teamNames[0] + ":&nbsp;$" + scores[0] + "<br>"
                + teamNames[1] + ":&nbsp;$" + scores[1]
                + "</font></center></html>",
                "Spielende", JOptionPane.PLAIN_MESSAGE);
    }

    private void confirmReset() {
        int ans = JOptionPane.showConfirmDialog(this,
                "Spiel wirklich komplett zur\u00fccksetzen?",
                "Reset best\u00e4tigen", JOptionPane.YES_NO_OPTION);
        if (ans != JOptionPane.YES_OPTION) return;

        for (int i = 0; i < 2; i++) scores[i] = 0;
        for (int i = 0; i < 2; i++) scoreLabels[i].setText(scoreHtml(i));

        for (int c = 0; c < 5; c++) {
            for (int r = 0; r < 5; r++) {
                answered[c][r] = false;
                JButton btn = boardButtons[c][r];
                btn.setText("$" + POINTS[r]);
                btn.setFont(new Font("Impact", Font.PLAIN, 38));
                btn.setForeground(C_GOLD);
                btn.setBackground(C_CELL);
                btn.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 80), 2));
                btn.setEnabled(true);
            }
        }
        sound.stopBackgroundMusic();
        sound.playBackgroundMusic();
    }

    private void editNames() {
        String[] newNames = new String[2];
        for (int i = 0; i < 2; i++) {
            String input = (String) JOptionPane.showInputDialog(this,
                    "Name f\u00fcr Team " + (i + 1) + ":",
                    "Teamnamen bearbeiten",
                    JOptionPane.PLAIN_MESSAGE, null, null, teamNames[i]);
            newNames[i] = (input == null || input.trim().isEmpty())
                    ? teamNames[i] : input.trim();
        }
        teamNames = newNames;
        for (int i = 0; i < 2; i++) scoreLabels[i].setText(scoreHtml(i));
    }

    // ─────────────────────────────────────────────────────────
    //  Button-Helfer
    // ─────────────────────────────────────────────────────────

    private JButton makeSmallButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(C_GOLD);
        btn.setBackground(C_CELL);
        btn.setBorder(BorderFactory.createLineBorder(C_GOLD, 2));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private JButton makeTinyButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setForeground(C_WHITE);
        btn.setBackground(new Color(0, 0, 90));
        btn.setBorder(BorderFactory.createLineBorder(C_GOLD, 1));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(28, 28));
        return btn;
    }

    // ── Gradient background panel ────────────────────────────

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

    // ─────────────────────────────────────────────────────────
    //  Einstiegspunkt
    // ─────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Show title screen first; it will create JeopardyGameV2 when ready
        SwingUtilities.invokeLater(TitleScreen::new);
    }
}

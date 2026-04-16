import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    private static final Color BG      = new Color(10, 10, 15);
    private static final Color FG_MAIN = new Color(0, 255, 65);
    private static final Color FG_DIM  = new Color(0, 140, 40);
    private static final Color BTN_BG  = new Color(20, 50, 20);
    private static final Color BTN_BD  = new Color(0, 200, 50);
    private static final Color BTN_HOV = new Color(30, 80, 30);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::showLauncher);
    }

    private static void showLauncher() {
        JFrame frame = new JFrame("GoL Computer — Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(BG);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BG);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(0, 40, 15));
                for (int x = 0; x < getWidth(); x += 20) g.drawLine(x, 0, x, getHeight());
                for (int y = 0; y < getHeight(); y += 20) g.drawLine(0, y, getWidth(), y);
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setBackground(BG);
        panel.setPreferredSize(new Dimension(520, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        String[] asciiArt = {
            " ██████╗  ██████╗ ██╗      ",
            "██╔════╝ ██╔═══██╗██║      ",
            "██║  ███╗██║   ██║██║      ",
            "██║   ██║██║   ██║██║      ",
            "╚██████╔╝╚██████╔╝███████╗ ",
            " ╚═════╝  ╚═════╝ ╚══════╝ "
        };

        JPanel artPanel = new JPanel(new GridLayout(asciiArt.length, 1, 0, 0));
        artPanel.setOpaque(false);
        for (String line : asciiArt) {
            JLabel l = new JLabel(line, SwingConstants.CENTER);
            l.setFont(new Font("Monospaced", Font.BOLD, 11));
            l.setForeground(FG_MAIN);
            artPanel.add(l);
        }
        panel.add(artPanel, gbc);

        gbc.gridy++;
        JLabel subtitle = new JLabel("[ COMPUTER — GAME LAUNCHER  v2 ]", SwingConstants.CENTER);
        subtitle.setFont(new Font("Monospaced", Font.BOLD, 13));
        subtitle.setForeground(FG_DIM);
        panel.add(subtitle, gbc);

        gbc.gridy++;
        JSeparator sep = new JSeparator();
        sep.setForeground(BTN_BD);
        sep.setBackground(BG);
        panel.add(sep, gbc);

        gbc.gridy++;
        JButton golBtn = makeBigButton("▶  Conway's Game of Life  (v2 — zoom / infinite board)");
        golBtn.addActionListener(e -> {
            frame.dispose();
            SwingUtilities.invokeLater(() -> new GameOfLifeGUI().setVisible(true));
        });
        panel.add(golBtn, gbc);

        gbc.gridy++;
        JButton tttBtn = makeBigButton("▶  Play Tic-Tac-Toe (inside GoL)");
        tttBtn.addActionListener(e -> {
            frame.dispose();
            TicTacToeGoL.main(null);
        });
        panel.add(tttBtn, gbc);

        gbc.gridy++;
        JButton bothBtn = makeBigButton("▶  Launch Both");
        bothBtn.addActionListener(e -> {
            frame.dispose();
            SwingUtilities.invokeLater(() -> {
                new GameOfLifeGUI().setVisible(true);
                new TicTacToeGoLGUI();
            });
        });
        panel.add(bothBtn, gbc);

        gbc.gridy++;
        JLabel hint = new JLabel("Select a game to launch", SwingConstants.CENTER);
        hint.setFont(new Font("Monospaced", Font.PLAIN, 11));
        hint.setForeground(new Color(0, 100, 30));
        panel.add(hint, gbc);

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private static JButton makeBigButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setBackground(BTN_BG);
        btn.setForeground(FG_MAIN);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BTN_BD, 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(BTN_HOV); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(BTN_BG); }
        });
        return btn;
    }
}

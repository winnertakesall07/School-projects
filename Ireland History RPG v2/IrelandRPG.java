import javax.swing.JFrame;

/**
 * Entry point for "Éire's Stand v2 — Ireland's Struggle for Freedom".
 * Improved combat version with proper shooting, mouse-aim, smarter enemies,
 * chapter-appropriate weapons and cover objects.
 *
 * Controls:
 *   WASD / Arrow Keys  — Move
 *   Mouse              — Aim
 *   SPACE or Left-Click — Attack / Shoot
 *   ENTER              — Advance story / confirm
 *   P or ESC           — Pause (in-game)
 */
public class IrelandRPG {
    public static void main(String[] args) {
        JFrame window = new JFrame("Éire's Stand v2 — Ireland's Struggle for Freedom");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.startGameThread();
    }
}

import javax.swing.JFrame;

/**
 * Entry point for "Éire's Stand — Ireland's Struggle for Freedom".
 * A short action RPG for English class covering key periods of Irish history.
 * Run this class in BlueJ (right-click → void main(String[])).
 *
 * Controls:
 *   WASD / Arrow Keys  — Move
 *   SPACE              — Attack (melee)
 *   ENTER              — Advance story / confirm
 *   P or ESC           — Pause (in-game)
 */
public class IrelandRPG {
    public static void main(String[] args) {
        JFrame window = new JFrame("Éire's Stand — Ireland's Struggle for Freedom");
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

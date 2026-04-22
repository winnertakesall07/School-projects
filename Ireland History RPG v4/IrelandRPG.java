import javax.swing.JFrame;

/**
 * Entry point for "Éire's Stand v4 — Ireland's Struggle for Freedom".
 *
 * V4 additions (on top of V3):
 *   - Chapter Select: press 1 / 2 / 3 on the title screen to jump to any chapter
 *   - Story pages now advance on mouse click (click anywhere to continue)
 *   - Enemies no longer spawn inside buildings / cover objects
 *   - Enemies spread apart — separation force prevents stacking
 *   - Smarter enemy AI:
 *       • Bullet dodging — sidestep incoming player shots
 *       • Obstacle-aware steering — rotate direction when path is blocked
 *       • Periodic strafing — enemies don't always charge in a straight line
 *
 * Controls:
 *   WASD / Arrow Keys       — Move
 *   Mouse                   — Aim
 *   SPACE or Left-Click     — Attack / Shoot
 *   Click (story screen)    — Advance story page
 *   ENTER                   — Advance story / confirm / skip intro (also works)
 *   P or ESC                — Pause (in-game)
 *   M                       — Toggle sound mute
 *   1 / 2 / 3 (title only)  — Jump to Chapter I / II / III
 */
public class IrelandRPG {
    public static void main(String[] args) {
        JFrame window = new JFrame("Éire's Stand v4 — Ireland's Struggle for Freedom");
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

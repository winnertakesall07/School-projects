import javax.swing.JFrame;

/**
 * Entry point for "Éire's Stand v3 — Ireland's Struggle for Freedom".
 *
 * V3 additions:
 *   - Sound: MIDI background music per chapter + synthesised SFX (M to mute)
 *   - Camera: World-space battlefield (2400×1800) with scrolling viewport (960×720)
 *   - Allies: Irish warriors / United Irishmen / IRA fight alongside you (need your help)
 *   - Battle Intro: Cinematic pan + context before every battle (skip with ENTER)
 *   - War Cries: Characters shout period-fitting war cries during battle
 *   - Larger maps: Norman castle, Dublin streets, British barracks + buildings
 *   - Commander enemies: High-HP enemies that ignore allies and hunt the player
 *
 * Controls:
 *   WASD / Arrow Keys  — Move
 *   Mouse              — Aim
 *   SPACE or Left-Click — Attack / Shoot
 *   ENTER              — Advance story / confirm / skip intro
 *   P or ESC           — Pause (in-game)
 *   M                  — Toggle sound mute
 */
public class IrelandRPG {
    public static void main(String[] args) {
        JFrame window = new JFrame("Éire's Stand v3 — Ireland's Struggle for Freedom");
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

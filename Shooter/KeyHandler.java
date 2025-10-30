import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean shootUp, shootDown, shootLeft, shootRight;
    public boolean enterPressed;
    private GamePanel gp;

    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        // Toggle inventory (pause) with I or Tab
        if (code == KeyEvent.VK_I || code == KeyEvent.VK_TAB) {
            if (gp.gameState == GamePanel.GameState.PLAY) {
                gp.gameState = GamePanel.GameState.INVENTORY;
                if (gp.player.unlockedWeapons.isEmpty()) {
                    gp.inventorySelectedIndex = 0;
                } else if (gp.inventorySelectedIndex >= gp.player.unlockedWeapons.size()) {
                    gp.inventorySelectedIndex = 0;
                }
            } else if (gp.gameState == GamePanel.GameState.INVENTORY) {
                gp.gameState = GamePanel.GameState.PLAY;
            }
            return;
        }

        // When inventory open: navigate/equip
        if (gp.gameState == GamePanel.GameState.INVENTORY) {
            if (code == KeyEvent.VK_ESCAPE) {
                gp.gameState = GamePanel.GameState.PLAY;
                return;
            }
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                if (!gp.player.unlockedWeapons.isEmpty()) {
                    gp.inventorySelectedIndex = (gp.inventorySelectedIndex - 1 + gp.player.unlockedWeapons.size()) % gp.player.unlockedWeapons.size();
                }
                return;
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                if (!gp.player.unlockedWeapons.isEmpty()) {
                    gp.inventorySelectedIndex = (gp.inventorySelectedIndex + 1) % gp.player.unlockedWeapons.size();
                }
                return;
            }
            if (code == KeyEvent.VK_ENTER) {
                gp.player.equipWeaponIndex(gp.inventorySelectedIndex);
                return;
            }
        }

        // Player Movement
        if (code == KeyEvent.VK_W) upPressed = true;
        if (code == KeyEvent.VK_S) downPressed = true;
        if (code == KeyEvent.VK_A) leftPressed = true;
        if (code == KeyEvent.VK_D) rightPressed = true;

        // Player Shooting
        if (code == KeyEvent.VK_UP) shootUp = true;
        if (code == KeyEvent.VK_DOWN) shootDown = true;
        if (code == KeyEvent.VK_LEFT) shootLeft = true;
        if (code == KeyEvent.VK_RIGHT) shootRight = true;

        // UI Interaction
        if (code == KeyEvent.VK_ENTER) enterPressed = true;
        
        // Upgrade Selection
        if (gp.gameState == GamePanel.GameState.LEVEL_UP) {
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                gp.upgradeSystem.changeSelection(-1);
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                gp.upgradeSystem.changeSelection(1);
            }
        }
        
        // Game Over Screen
        if (gp.gameState == GamePanel.GameState.GAME_OVER) {
            if (code == KeyEvent.VK_ENTER) {
                gp.resetGame();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        
        if (code == KeyEvent.VK_W) upPressed = false;
        if (code == KeyEvent.VK_S) downPressed = false;
        if (code == KeyEvent.VK_A) leftPressed = false;
        if (code == KeyEvent.VK_D) rightPressed = false;
        
        if (code == KeyEvent.VK_UP) shootUp = false;
        if (code == KeyEvent.VK_DOWN) shootDown = false;
        if (code == KeyEvent.VK_LEFT) shootLeft = false;
        if (code == KeyEvent.VK_RIGHT) shootRight = false;

        if (code == KeyEvent.VK_ENTER) enterPressed = false;
    }
}
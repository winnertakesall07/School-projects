import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class UI {
    GamePanel gp;
    Font arial_20, arial_40, arial_80;

    public UI(GamePanel gp) {
        this.gp = gp;
        arial_20 = new Font("Arial", Font.PLAIN, 20);
        arial_40 = new Font("Arial", Font.BOLD, 40);
        arial_80 = new Font("Arial", Font.BOLD, 80);
    }

    public void draw(Graphics2D g2) {
        if (gp.gameState == GamePanel.GameState.PLAY) {
            drawPlayStateUI(g2);
        } else if (gp.gameState == GamePanel.GameState.LEVEL_UP) {
            drawLevelUpScreen(g2);
        } else if (gp.gameState == GamePanel.GameState.GAME_OVER) {
            drawGameOverScreen(g2);
        } else if (gp.gameState == GamePanel.GameState.INVENTORY) {
            drawInventoryScreen(g2);
        }
    }

    private void drawPlayStateUI(Graphics2D g2) {
        g2.setFont(arial_20);
        g2.setColor(Color.WHITE);

        // HP
        g2.drawString("HP: " + gp.player.hp + "/" + gp.player.maxHp, 20, 30);
        
        // XP
        g2.drawString("Level: " + gp.player.level, 20, 60);
        g2.drawString("XP: " + gp.player.xp + "/" + gp.player.nextLevelXp, 20, 90);
        
        // Wave
        g2.drawString("Wave: " + gp.waveManager.waveNumber, gp.screenWidth - 120, 30);

        // Current weapon, damage, speed
        String weaponLine = "Weapon: " + (gp.player.currentWeapon != null ? gp.player.currentWeapon.getName() : "None");
        String dmgLine = "Damage: " + (gp.player.currentWeapon != null ? gp.player.currentWeapon.getDamage() : 0);
        String speedLine = "Speed: " + gp.player.speed;

        int rightX = gp.screenWidth - 220;
        int y = 60;
        g2.drawString(weaponLine, rightX, y);
        g2.drawString(dmgLine, rightX, y + 25);
        g2.drawString(speedLine, rightX, y + 50);

        // Hint to open inventory
        g2.setColor(Color.GRAY);
        g2.drawString("Press I / Tab for Inventory", gp.screenWidth - 260, gp.screenHeight - 20);
    }

    private void drawLevelUpScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black background
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(arial_40);
        g2.setColor(Color.YELLOW);
        String text = "LEVEL UP!";
        int x = getXforCenteredText(text, g2);
        int y = gp.screenHeight / 4;
        g2.drawString(text, x, y);

        gp.upgradeSystem.draw(g2);
    }
    
    private void drawGameOverScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(arial_80);
        g2.setColor(Color.RED);
        String text = "GAME OVER";
        int x = getXforCenteredText(text, g2);
        int y = gp.screenHeight / 2;
        g2.drawString(text, x, y);

        g2.setFont(arial_20);
        g2.setColor(Color.WHITE);
        text = "Press ENTER to restart";
        x = getXforCenteredText(text, g2);
        g2.drawString(text, x, y + 50);
    }

    // Inventory screen (paused)
    private void drawInventoryScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(arial_40);
        g2.setColor(Color.YELLOW);
        String title = "Inventory";
        int titleX = getXforCenteredText(title, g2);
        g2.drawString(title, titleX, 80);

        g2.setFont(arial_20);
        g2.setColor(Color.WHITE);
        // Sections
        g2.drawString("Unlocked Weapons", 80, 130);
        g2.drawString("Upgrades Acquired", gp.screenWidth / 2 + 40, 130);

        // Weapons list (left)
        int listY = 160;
        for (int i = 0; i < gp.player.unlockedWeapons.size(); i++) {
            Weapon w = gp.player.unlockedWeapons.get(i);
            boolean selected = (i == gp.inventorySelectedIndex);
            g2.setColor(selected ? Color.CYAN : Color.LIGHT_GRAY);
            String line = w.getName() + " (DMG " + w.getDamage() + ")";
            g2.drawString(line, 80, listY + i * 26);
        }

        // Upgrades list (right)
        g2.setColor(Color.LIGHT_GRAY);
        int uY = 160;
        int colX = gp.screenWidth / 2 + 40;
        if (gp.player.acquiredUpgrades.isEmpty()) {
            g2.drawString("(none yet)", colX, uY);
        } else {
            for (int i = 0; i < gp.player.acquiredUpgrades.size(); i++) {
                g2.drawString("- " + gp.player.acquiredUpgrades.get(i), colX, uY + i * 22);
            }
        }

        // Help text
        g2.setColor(Color.GRAY);
        g2.drawString("Up/Down to select weapon, Enter to equip, Esc or I/Tab to close", 80, gp.screenHeight - 40);
    }

    public int getXforCenteredText(String text, Graphics2D g2) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.screenWidth / 2 - length / 2;
    }
}
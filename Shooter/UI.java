
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
        g2.drawString("Wave: " + gp.waveManager.waveNumber, gp.screenWidth - 100, 30);
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

    public int getXforCenteredText(String text, Graphics2D g2) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.screenWidth / 2 - length / 2;
    }
}

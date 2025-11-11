import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.image.BufferedImage;

public class BossEnemy extends Enemy {
    private int size;
    
    public BossEnemy(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 2;
        maxHp = 500;
        hp = maxHp;
        xpValue = 100;
        size = gp.tileSize * 2;
    }
    
    @Override
    public void draw(Graphics2D g2) {
        // Try to load sprite
        BufferedImage sprite = SpriteLoader.get("enemy_boss");
        
        if (sprite != null) {
            g2.drawImage(sprite, x, y, size, size, null);
            
            // HP bar (always show for boss)
            int barWidth = size;
            int barHeight = 8;
            g2.setColor(Color.BLACK);
            g2.fillRect(x, y - 20, barWidth, barHeight);
            g2.setColor(Color.RED);
            g2.fillRect(x, y - 20, barWidth, barHeight);
            g2.setColor(new Color(150, 0, 255));
            int currentBarWidth = (int)((double)hp / maxHp * barWidth);
            g2.fillRect(x, y - 20, currentBarWidth, barHeight);
            
            // BOSS label
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("BOSS", x + size/2 - 20, y - 25);
        } else {
            // Fallback to original rendering
            // Boss - large purple with glowing effect
            g2.setColor(new Color(150, 0, 150, 100));
            g2.fillRect(x - 8, y - 8, size + 16, size + 16);
            g2.setColor(new Color(100, 0, 100));
            g2.fillRect(x - 4, y - 4, size + 8, size + 8);
            g2.setColor(new Color(180, 0, 180));
            g2.fillRect(x, y, size, size);
            
            // HP bar
            int barWidth = size;
            int barHeight = 8;
            g2.setColor(Color.BLACK);
            g2.fillRect(x, y - 20, barWidth, barHeight);
            g2.setColor(Color.RED);
            g2.fillRect(x, y - 20, barWidth, barHeight);
            g2.setColor(new Color(150, 0, 255));
            int currentBarWidth = (int)((double)hp / maxHp * barWidth);
            g2.fillRect(x, y - 20, currentBarWidth, barHeight);
            
            // BOSS label
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("BOSS", x + size/2 - 20, y - 25);
        }
    }
}
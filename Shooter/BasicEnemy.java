import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class BasicEnemy extends Enemy {

    public BasicEnemy(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 2;
        maxHp = 20;
        hp = maxHp;
        xpValue = 5;
    }

    @Override
    public void draw(Graphics2D g2) {
        // Try to load sprite
        BufferedImage sprite = SpriteLoader.get("enemy_basic");
        
        if (sprite != null) {
            g2.drawImage(sprite, x, y, gp.tileSize, gp.tileSize, null);
        } else {
            // Fallback to original rendering
            g2.setColor(Color.RED);
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
        }
    }
}
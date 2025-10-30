import java.awt.Color;
import java.awt.Graphics2D;

public class Assassin extends Enemy {

    public Assassin(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 4; // Very fast
        maxHp = 15; // Low health
        hp = maxHp;
        xpValue = 8;
    }

    @Override
    public void draw(Graphics2D g2) {
        // Assassin is small and purple
        g2.setColor(new Color(128, 0, 128));
        int size = gp.tileSize / 2; // Half size
        g2.fillRect(x + gp.tileSize / 4, y + gp.tileSize / 4, size, size);
    }
}

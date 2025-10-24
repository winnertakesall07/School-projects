import java.awt.Color;
import java.awt.Graphics2D;

public class FastEnemy extends Enemy {
    
    public FastEnemy(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 4;
        maxHp = 5;
        hp = maxHp;
        xpValue = 3;
    }
    
    @Override
    public void draw(Graphics2D g2) {
        // Fast enemy - bright red with orange border
        g2.setColor(new Color(255, 100, 0));
        g2.fillRect(x - 2, y - 2, gp.tileSize + 4, gp.tileSize + 4);
        g2.setColor(new Color(255, 50, 50));
        g2.fillRect(x, y, gp.tileSize, gp.tileSize);
    }
}
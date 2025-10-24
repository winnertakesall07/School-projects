import java.awt.Color;
import java.awt.Graphics2D;

public class TankEnemy extends Enemy {
    
    public TankEnemy(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 1;
        maxHp = 100;
        hp = maxHp;
        xpValue = 15;
    }
    
    @Override
    public void draw(Graphics2D g2) {
        // Tank enemy - large gray with dark border
        int size = gp.tileSize + 10;
        g2.setColor(new Color(40, 40, 40));
        g2.fillRect(x - 7, y - 7, size + 4, size + 4);
        g2.setColor(new Color(120, 120, 120));
        g2.fillRect(x - 5, y - 5, size, size);
        
        // HP bar
        int barWidth = size;
        int barHeight = 5;
        g2.setColor(Color.RED);
        g2.fillRect(x - 5, y - 12, barWidth, barHeight);
        g2.setColor(Color.GREEN);
        int currentBarWidth = (int)((double)hp / maxHp * barWidth);
        g2.fillRect(x - 5, y - 12, currentBarWidth, barHeight);
    }
}
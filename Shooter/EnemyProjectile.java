import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class EnemyProjectile extends Entity {
    int dx, dy;
    int damage;
    
    public EnemyProjectile(int x, int y, int dx, int dy, int damage, int speed, GamePanel gp) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.damage = damage;
        this.speed = speed;
        this.solidArea = new Rectangle(0, 0, 8, 8);
    }
    
    @Override
    public void update() {
        x += dx * speed;
        y += dy * speed;
        
        // De-spawn if it goes off-screen
        if (x < 0 || x > 800 || y < 0 || y > 600) {
            alive = false;
        }
    }
    
    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(new Color(255, 100, 100));
        g2.fillOval(x - 4, y - 4, 8, 8);
    }
    
    public int getDamage() {
        return damage;
    }
}
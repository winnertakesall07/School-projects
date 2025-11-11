import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class EnemyProjectile extends Entity {
    int dx, dy;
    int damage;
    private StatusEffect statusEffect = StatusEffect.NONE;
    private Color color = new Color(255, 100, 100);
    
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
        g2.setColor(color);
        g2.fillOval(x - 4, y - 4, 8, 8);
    }
    
    public int getDamage() {
        return damage;
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }

    public void setStatusEffect(StatusEffect effect) {
        this.statusEffect = effect;
        // Update color based on effect
        switch (effect) {
            case POISON:
                color = new Color(100, 255, 100);
                break;
            case SLOW:
                color = new Color(100, 100, 255);
                break;
            case FREEZE:
                color = new Color(150, 200, 255);
                break;
            case CONFUSION:
                color = new Color(200, 100, 200);
                break;
            default:
                color = new Color(255, 100, 100);
                break;
        }
    }
}
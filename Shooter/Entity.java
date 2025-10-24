import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class Entity {
    public int x, y;
    public int speed;
    public int maxHp;
    public int hp;
    public Rectangle solidArea;
    protected boolean alive = true;

    public abstract void update();
    public abstract void draw(Graphics2D g2);
    
    public boolean isAlive() {
        return alive;
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            alive = false;
        }
    }
}

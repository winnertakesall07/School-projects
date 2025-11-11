import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MeleeHitbox extends Entity {

    public enum Type { RECT, CIRCLE }

    private Type type;
    private int lifetime; // frames remaining
    private int damage;
    private StatusEffect statusEffect = StatusEffect.NONE;

    // Rect params
    private int rx, ry, rwidth, rheight;

    // Circle params
    private int cx, cy, radius;

    // Track which enemies were already damaged by this hitbox
    private List<Enemy> hitEnemies = new ArrayList<>();

    private Color color = new Color(255, 255, 255, 120);
    private String spriteKey = "melee_arc"; // Default sprite key

    // Factory: rectangle hitbox
    public static MeleeHitbox rect(int x, int y, int width, int height, int damage, int lifetime) {
        MeleeHitbox h = new MeleeHitbox();
        h.type = Type.RECT;
        h.rx = x;
        h.ry = y;
        h.rwidth = width;
        h.rheight = height;
        h.damage = damage;
        h.lifetime = lifetime;
        return h;
    }

    // Factory: circle hitbox
    public static MeleeHitbox circle(int cx, int cy, int radius, int damage, int lifetime) {
        MeleeHitbox h = new MeleeHitbox();
        h.type = Type.CIRCLE;
        h.cx = cx;
        h.cy = cy;
        h.radius = radius;
        h.damage = damage;
        h.lifetime = lifetime;
        return h;
    }
    
    public void setSpriteKey(String key) {
        this.spriteKey = key;
    }

    public boolean tryDamage(Enemy e) {
        if (hitEnemies.contains(e)) return false;
        if (collides(e)) {
            e.takeDamage(damage);
            if (statusEffect != StatusEffect.NONE) {
                e.applyStatusEffect(statusEffect, 180); // 3 seconds at 60 FPS
            }
            hitEnemies.add(e);
            return true;
        }
        return false;
    }

    private boolean collides(Enemy e) {
        Rectangle enemyRect = new Rectangle(
                e.x + e.solidArea.x,
                e.y + e.solidArea.y,
                e.solidArea.width,
                e.solidArea.height
        );
        if (type == Type.RECT) {
            Rectangle r = new Rectangle(rx, ry, rwidth, rheight);
            return r.intersects(enemyRect);
        } else {
            // CIRCLE distance check vs. enemy center
            int ex = enemyRect.x + enemyRect.width / 2;
            int ey = enemyRect.y + enemyRect.height / 2;
            int dx = ex - cx;
            int dy = ey - cy;
            return dx * dx + dy * dy <= radius * radius;
        }
    }

    @Override
    public void update() {
        lifetime--;
        if (lifetime <= 0) {
            alive = false;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage sprite = SpriteLoader.get(spriteKey);
        
        if (sprite != null && type == Type.RECT) {
            // Draw sprite for rectangle melee hitbox
            g2.setColor(new Color(255, 255, 255, 150));
            g2.drawImage(sprite, rx, ry, rwidth, rheight, null);
        } else if (sprite != null && type == Type.CIRCLE) {
            // Draw sprite for circle melee hitbox
            g2.setColor(new Color(255, 255, 255, 150));
            g2.drawImage(sprite, cx - radius, cy - radius, radius * 2, radius * 2, null);
        } else {
            // Fallback to simple shapes
            g2.setColor(color);
            if (type == Type.RECT) {
                g2.fillRect(rx, ry, rwidth, rheight);
            } else {
                g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
            }
        }
    }

    public void setStatusEffect(StatusEffect effect) {
        this.statusEffect = effect;
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }
}
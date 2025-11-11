import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Projectile extends Entity {
    int dx, dy;
    int damage;
    private Color color = Color.YELLOW;
    private int pierce = 0;
    private int pierceCount = 0;
    private int explosionRadius = 0;
    private StatusEffect statusEffect = StatusEffect.NONE;
    private String spriteKey = "bullet"; // Default sprite key
    
    public Projectile(int x, int y, int dx, int dy, int damage, int speed, GamePanel gp) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.damage = damage;
        this.speed = speed;
        this.solidArea = new Rectangle(0, 0, 8, 8);
    }
    
    public void setColor(Color color) {
        this.color = color;
        // Infer sprite key from color if not set explicitly
        if (color.equals(new Color(0, 255, 255))) {
            spriteKey = "laser";
        } else if (color.equals(new Color(255, 100, 0)) || color.equals(Color.ORANGE)) {
            spriteKey = "rocket";
        }
    }
    
    public void setSpriteKey(String key) {
        this.spriteKey = key;
    }
    
    public void setPierce(int pierce) {
        this.pierce = pierce;
    }
    
    public void setExplosionRadius(int radius) {
        this.explosionRadius = radius;
    }
    
    public int getExplosionRadius() {
        return explosionRadius;
    }
    
    public boolean canPierce() {
        return pierceCount < pierce;
    }
    
    public void onHit() {
        pierceCount++;
        if (pierceCount >= pierce) {
            alive = false;
        }
    }

    // Compatibility aliases for BlueJ / older references
    public int getExplosionradius() { return getExplosionRadius(); }
    public void setExplosionradius(int radius) { setExplosionRadius(radius); }
    public boolean getpierce() { return canPierce(); }
    public int getPierce() { return pierce; }
    public void setColour(Color color) { setColor(color); }
    public void onhit() { onHit(); }
    // Additional aliases for lower-case usage found in some environments
    public void setcolor(Color color) { setColor(color); }
    public void setpierce(int pierce) { setPierce(pierce); }

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
        BufferedImage sprite = SpriteLoader.get(spriteKey);
        
        if (sprite != null) {
            // Calculate angle for rotation
            double angle = Math.atan2(dy, dx);
            
            // Draw sprite with rotation
            int size = 16;
            GraphicsUtil.drawSpriteRotated(g2, sprite, x - size/2, y - size/2, size, size, angle);
        } else {
            // Fallback to rectangle drawing
            // Draw trail effect
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
            g2.fillRect(x - 6, y - 6, 12, 12);
            
            // Draw main projectile
            g2.setColor(color);
            g2.fillRect(x - 4, y - 4, 8, 8);
        }
    }
    
    public int getDamage() {
        return damage;
    }
    
    public void setDamage(int damage) {
        this.damage = damage;
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }

    public void setStatusEffect(StatusEffect effect) {
        this.statusEffect = effect;
    }
}
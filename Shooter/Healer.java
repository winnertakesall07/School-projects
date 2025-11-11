import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Healer extends Enemy {
    private int healCooldown = 0;

    public Healer(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 1;
        maxHp = 40;
        hp = maxHp;
        xpValue = 15;
        healCooldown = 120; // Heal after 2 seconds
    }

    @Override
    public void update() {
        // Update status effects
        updateStatusEffects();
        
        // Don't move if frozen
        if (freezeTimer > 0) {
            handleHealing();
            return;
        }
        
        // Calculate effective speed
        int effectiveSpeed = speed;
        if (slowTimer > 0) {
            effectiveSpeed = speed / 2;
        }
        
        // Target position (player, with confusion jitter)
        int targetX = gp.player.x;
        int targetY = gp.player.y;
        if (confusionTimer > 0) {
            targetX += random.nextInt(100) - 50;
            targetY += random.nextInt(100) - 50;
        }
        
        if (targetX < this.x) {
            x -= effectiveSpeed;
        }
        if (targetX > this.x) {
            x += effectiveSpeed;
        }
        if (targetY < this.y) {
            y -= effectiveSpeed;
        }
        if (targetY > this.y) {
            y += effectiveSpeed;
        }
        
        handleHealing();
    }
    
    private void handleHealing() {
        healCooldown--;
        if (healCooldown <= 0) {
            // Heal nearby enemies
            int healRadius = 150;
            int healAmount = 10;
            
            for (Enemy e : gp.enemies) {
                if (e == this || !e.isAlive()) continue;
                
                int dx = e.x - x;
                int dy = e.y - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                if (dist <= healRadius) {
                    e.hp = Math.min(e.hp + healAmount, e.maxHp);
                }
            }
            
            healCooldown = 180; // Heal every 3 seconds
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage sprite = SpriteLoader.get("enemy_healer");
        if (sprite != null) {
            g2.drawImage(sprite, x, y, gp.tileSize, gp.tileSize, null);
        } else {
            // Fallback to rectangle - bright green with a cross
            g2.setColor(new Color(0, 255, 0));
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
        }
        
        // Draw a white cross (always, even with sprite)
        g2.setColor(Color.WHITE);
        int cx = x + gp.tileSize / 2;
        int cy = y + gp.tileSize / 2;
        g2.fillRect(cx - 2, cy - 8, 4, 16);
        g2.fillRect(cx - 8, cy - 2, 16, 4);
    }
}

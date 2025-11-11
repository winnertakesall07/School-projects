import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Charger extends Enemy {
    private int restTimer = 0;
    private boolean charging = false;
    private int chargeDx = 0;
    private int chargeDy = 0;
    private int chargeDuration = 0;

    public Charger(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 1; // Slow when not charging
        maxHp = 35;
        hp = maxHp;
        xpValue = 12;
        restTimer = 90; // Rest for 1.5 seconds before first charge
    }

    @Override
    public void update() {
        // Update status effects
        updateStatusEffects();
        
        // Don't move if frozen
        if (freezeTimer > 0) {
            return;
        }
        
        if (charging) {
            // Currently charging
            x += chargeDx;
            y += chargeDy;
            chargeDuration--;
            if (chargeDuration <= 0) {
                charging = false;
                restTimer = 120; // Rest for 2 seconds
            }
        } else if (restTimer > 0) {
            // Resting (not moving)
            restTimer--;
        } else {
            // Start a charge toward player
            int dx = gp.player.x - x;
            int dy = gp.player.y - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                chargeDx = (int) (dx / dist * 8); // Fast charge
                chargeDy = (int) (dy / dist * 8);
                chargeDuration = 30; // Charge for 0.5 seconds
                charging = true;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage sprite = SpriteLoader.get("enemy_charger");
        Color fallbackColor;
        
        if (charging) {
            // Bright red when charging
            fallbackColor = new Color(255, 50, 50);
        } else {
            // Dark red when resting
            fallbackColor = new Color(150, 0, 0);
        }
        
        if (sprite != null) {
            g2.drawImage(sprite, x, y, gp.tileSize, gp.tileSize, null);
            // If charging, add a visual effect
            if (charging) {
                g2.setColor(new Color(255, 50, 50, 100));
                g2.fillRect(x - 2, y - 2, gp.tileSize + 4, gp.tileSize + 4);
            }
        } else {
            // Fallback to rectangle
            g2.setColor(fallbackColor);
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
        }
    }
    
    @Override
    public int getContactDamage() {
        int baseDamage = charging ? 10 : 5; // More damage when charging
        if (weaknessTimer > 0) {
            return baseDamage / 2;
        }
        return baseDamage;
    }
}

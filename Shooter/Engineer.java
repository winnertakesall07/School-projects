import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Engineer extends Enemy {
    private int turretSpawnCooldown = 0;
    private int turretCount = 0;
    private int keepDistanceCooldown = 0;

    public Engineer(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 2;
        maxHp = 35;
        hp = maxHp;
        xpValue = 12;
        turretSpawnCooldown = 120; // Spawn turret after 2 seconds
    }

    @Override
    public void update() {
        // Update status effects
        updateStatusEffects();
        
        // Don't move if frozen
        if (freezeTimer > 0) {
            return;
        }
        
        // Calculate effective speed
        int effectiveSpeed = speed;
        if (slowTimer > 0) {
            effectiveSpeed = speed / 2;
        }
        
        // Keep distance from player (run away if too close)
        int dx = gp.player.x - x;
        int dy = gp.player.y - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 150) {
            // Run away
            if (dx > 0) x -= effectiveSpeed;
            else x += effectiveSpeed;
            if (dy > 0) y -= effectiveSpeed;
            else y += effectiveSpeed;
        } else if (dist > 250) {
            // Get closer but not too close
            if (dx > 0) x += effectiveSpeed;
            else x -= effectiveSpeed;
            if (dy > 0) y += effectiveSpeed;
            else y -= effectiveSpeed;
        }
        
        // Spawn turrets periodically
        turretSpawnCooldown--;
        if (turretSpawnCooldown <= 0 && turretCount < 3) {
            gp.enemies.add(new Turret(gp, x, y));
            turretCount++;
            turretSpawnCooldown = 180; // Spawn every 3 seconds
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Try to load sprite
        BufferedImage sprite = SpriteLoader.get("enemy_engineer");
        
        if (sprite != null) {
            g2.drawImage(sprite, x, y, gp.tileSize, gp.tileSize, null);
        } else {
            // Fallback to original rendering
            // Engineer is cyan
            g2.setColor(new Color(0, 200, 200));
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
        }
    }
}

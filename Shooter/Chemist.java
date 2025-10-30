import java.awt.Color;
import java.awt.Graphics2D;

public class Chemist extends Enemy {
    private int shootCooldown = 0;

    public Chemist(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 2;
        maxHp = 30;
        hp = maxHp;
        xpValue = 10;
        shootCooldown = 90;
    }

    @Override
    public void update() {
        // Update status effects
        updateStatusEffects();
        
        // Don't move if frozen
        if (freezeTimer > 0) {
            handleShooting();
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
        
        handleShooting();
    }
    
    private void handleShooting() {
        shootCooldown--;
        if (shootCooldown <= 0) {
            // Shoot poison projectile toward player
            int cx = x + gp.tileSize / 2;
            int cy = y + gp.tileSize / 2;
            int dx = gp.player.x - x;
            int dy = gp.player.y - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                int ndx = (int) (dx / dist);
                int ndy = (int) (dy / dist);
                EnemyProjectile proj = new EnemyProjectile(cx, cy, ndx, ndy, 5, 4, gp);
                proj.setStatusEffect(StatusEffect.POISON);
                gp.enemyProjectiles.add(proj);
            }
            shootCooldown = 120; // Shoot every 2 seconds
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Chemist is green
        g2.setColor(new Color(0, 200, 0));
        g2.fillRect(x, y, gp.tileSize, gp.tileSize);
    }
}

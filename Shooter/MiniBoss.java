import java.awt.Color;
import java.awt.Graphics2D;

public class MiniBoss extends Enemy {
    private int shootCooldown = 0;

    public MiniBoss(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 1; // Slow but tanky
        maxHp = 200; // Very beefy
        hp = maxHp;
        xpValue = 50;
        shootCooldown = 90;
    }

    @Override
    public void update() {
        // Update status effects
        updateStatusEffects();
        
        // Don't move if frozen
        if (freezeTimer > 0) {
            // Still can shoot
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
            // Shoot toward player
            int cx = x + gp.tileSize / 2;
            int cy = y + gp.tileSize / 2;
            int dx = gp.player.x - x;
            int dy = gp.player.y - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                int ndx = (int) (dx / dist);
                int ndy = (int) (dy / dist);
                EnemyProjectile proj = new EnemyProjectile(cx, cy, ndx, ndy, 10, 5, gp);
                gp.enemyProjectiles.add(proj);
            }
            shootCooldown = 90; // Shoot every 1.5 seconds
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // MiniBoss is large and dark red
        g2.setColor(new Color(139, 0, 0));
        g2.fillRect(x - gp.tileSize / 4, y - gp.tileSize / 4, (int)(gp.tileSize * 1.5), (int)(gp.tileSize * 1.5));
        
        // Draw HP bar
        int barWidth = gp.tileSize * 2;
        int barHeight = 6;
        int barX = x - gp.tileSize / 4;
        int barY = y - 20;
        
        g2.setColor(Color.RED);
        g2.fillRect(barX, barY, barWidth, barHeight);
        
        g2.setColor(Color.GREEN);
        int hpWidth = (int) ((double) hp / maxHp * barWidth);
        g2.fillRect(barX, barY, hpWidth, barHeight);
        
        g2.setColor(Color.WHITE);
        g2.drawRect(barX, barY, barWidth, barHeight);
    }
}

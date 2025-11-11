import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Turret extends Enemy {
    private int shootCooldown = 0;

    public Turret(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 0; // Turrets don't move
        maxHp = 30;
        hp = maxHp;
        xpValue = 10;
        shootCooldown = 60; // Shoot after 1 second
    }

    @Override
    public void update() {
        // Update status effects
        updateStatusEffects();
        
        // Don't move (speed = 0), but shoot radial bursts
        shootCooldown--;
        if (shootCooldown <= 0) {
            shootRadialBurst();
            shootCooldown = 90; // Shoot every 1.5 seconds
        }
    }

    private void shootRadialBurst() {
        int cx = x + gp.tileSize / 2;
        int cy = y + gp.tileSize / 2;
        
        // 8 directions
        int[][] dirs = {
            {0, -1}, {1, -1}, {1, 0}, {1, 1},
            {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}
        };
        
        for (int[] dir : dirs) {
            EnemyProjectile proj = new EnemyProjectile(cx, cy, dir[0], dir[1], 4, 4, gp);
            gp.enemyProjectiles.add(proj);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Try to load sprite
        BufferedImage sprite = SpriteLoader.get("enemy_turret");
        
        if (sprite != null) {
            g2.drawImage(sprite, x, y, gp.tileSize, gp.tileSize, null);
            // Draw a red targeting marker on top
            g2.setColor(Color.RED);
            int cx = x + gp.tileSize / 2;
            int cy = y + gp.tileSize / 2;
            g2.fillOval(cx - 4, cy - 4, 8, 8);
        } else {
            // Fallback to original rendering
            // Turret is dark gray and square
            g2.setColor(new Color(64, 64, 64));
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
            // Draw a red targeting marker
            g2.setColor(Color.RED);
            int cx = x + gp.tileSize / 2;
            int cy = y + gp.tileSize / 2;
            g2.fillOval(cx - 4, cy - 4, 8, 8);
        }
    }
}

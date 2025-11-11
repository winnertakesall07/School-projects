import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ShooterEnemy extends Enemy {
    private int shootCooldown;
    private int shootRate = 90;
    
    public ShooterEnemy(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 2;
        maxHp = 15;
        hp = maxHp;
        xpValue = 10;
        shootCooldown = shootRate;
    }
    
    @Override
    public void update() {
        super.update();
        
        // Shoot at player
        shootCooldown--;
        if (shootCooldown <= 0) {
            shootAtPlayer();
            shootCooldown = shootRate;
        }
    }
    
    private void shootAtPlayer() {
        int dx = 0, dy = 0;
        int diffX = gp.player.x - this.x;
        int diffY = gp.player.y - this.y;
        
        if (Math.abs(diffX) > Math.abs(diffY)) {
            dx = diffX > 0 ? 1 : -1;
        } else {
            dy = diffY > 0 ? 1 : -1;
        }
        
        int startX = x + gp.tileSize / 2;
        int startY = y + gp.tileSize / 2;
        EnemyProjectile proj = new EnemyProjectile(startX, startY, dx, dy, 10, 5, gp);
        gp.enemyProjectiles.add(proj);
    }
    
    @Override
    public void draw(Graphics2D g2) {
        // Try to load sprite
        BufferedImage sprite = SpriteLoader.get("enemy_shooter");
        
        if (sprite != null) {
            g2.drawImage(sprite, x, y, gp.tileSize, gp.tileSize, null);
        } else {
            // Fallback to original rendering
            // Shooter enemy - green with yellow border
            g2.setColor(new Color(200, 200, 0));
            g2.fillRect(x - 2, y - 2, gp.tileSize + 4, gp.tileSize + 4);
            g2.setColor(new Color(50, 200, 50));
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
        }
    }
}
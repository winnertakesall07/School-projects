import java.awt.Rectangle;

public class CollisionChecker {
    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkCollisions() {
        // Check if projectiles hit enemies
        for (Projectile p : gp.projectiles) {
            for (Enemy e : gp.enemies) {
                if (p.isAlive() && e.isAlive()) {
                    Rectangle pRect = new Rectangle(p.x + p.solidArea.x, p.y + p.solidArea.y, p.solidArea.width, p.solidArea.height);
                    Rectangle eRect = new Rectangle(e.x + e.solidArea.x, e.y + e.solidArea.y, e.solidArea.width, e.solidArea.height);
                    if (pRect.intersects(eRect)) {
                        e.takeDamage(p.getDamage());
                        p.takeDamage(1); // Projectile is destroyed on hit
                    }
                }
            }
        }

        // Check if enemies hit player
        for (Enemy e : gp.enemies) {
             if (e.isAlive()) {
                Rectangle pRect = new Rectangle(gp.player.x + gp.player.solidArea.x, gp.player.y + gp.player.solidArea.y, gp.player.solidArea.width, gp.player.solidArea.height);
                Rectangle eRect = new Rectangle(e.x + e.solidArea.x, e.y + e.solidArea.y, e.solidArea.width, e.solidArea.height);
                if (pRect.intersects(eRect)) {
                    gp.player.takeDamage(10); // Player takes damage
                    e.takeDamage(e.maxHp); // Enemy is destroyed on hit
                }
             }
        }
    }
}

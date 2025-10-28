import java.awt.Rectangle;
import java.util.Iterator;

public class CollisionChecker {
    private final GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkCollisions() {
        handlePlayerVsEnemyProjectiles();
        handleProjectilesVsEnemies();
    }

    private void handlePlayerVsEnemyProjectiles() {
        Rectangle playerRect = new Rectangle(
                gp.player.x + gp.player.solidArea.x,
                gp.player.y + gp.player.solidArea.y,
                gp.player.solidArea.width,
                gp.player.solidArea.height
        );

        Iterator<EnemyProjectile> it = gp.enemyProjectiles.iterator();
        while (it.hasNext()) {
            EnemyProjectile ep = it.next();
            Rectangle projRect = new Rectangle(
                    ep.x + ep.solidArea.x,
                    ep.y + ep.solidArea.y,
                    ep.solidArea.width,
                    ep.solidArea.height
            );
            if (projRect.intersects(playerRect)) {
                gp.player.takeDamage(ep.getDamage());
                it.remove();
            }
        }
    }

    private void handleProjectilesVsEnemies() {
        Iterator<Projectile> pit = gp.projectiles.iterator();
        while (pit.hasNext()) {
            Projectile p = pit.next();
            if (!p.isAlive()) continue;

            for (Enemy e : gp.enemies) {
                if (!e.isAlive()) continue;

                Rectangle enemyRect = new Rectangle(
                        e.x + e.solidArea.x,
                        e.y + e.solidArea.y,
                        e.solidArea.width,
                        e.solidArea.height
                );
                Rectangle pRect = new Rectangle(
                        p.x + p.solidArea.x,
                        p.y + p.solidArea.y,
                        p.solidArea.width,
                        p.solidArea.height
                );

                if (pRect.intersects(enemyRect)) {
                    // Direct hit
                    e.takeDamage(p.getDamage());

                    // Explosion damage (if any)
                    int radius = p.getExplosionRadius();
                    if (radius > 0) {
                        explodeAt(p.x, p.y, radius, p.getDamage());
                        p.takeDamage(99999); // destroy after explosion
                        break; // move to next projectile
                    }

                    // Pierce handling
                    if (p.canPierce()) {
                        p.onHit();
                        // keep projectile alive for more hits
                    } else {
                        p.takeDamage(99999); // destroy projectile
                        break; // move to next projectile
                    }
                }
            }
        }
    }

    private void explodeAt(int cx, int cy, int radius, int baseDamage) {
        int r2 = radius * radius;
        for (Enemy e : gp.enemies) {
            if (!e.isAlive()) continue;
            Rectangle b = new Rectangle(
                    e.x + e.solidArea.x,
                    e.y + e.solidArea.y,
                    e.solidArea.width,
                    e.solidArea.height
            );
            int ex = b.x + b.width / 2;
            int ey = b.y + b.height / 2;
            int dx = ex - cx;
            int dy = ey - cy;
            if (dx * dx + dy * dy <= r2) {
                e.takeDamage(baseDamage);
            }
        }
    }
}

public class Shotgun extends Weapon {
    public Shotgun(GamePanel gp) {
        super(gp);
        damage = 8;
        fireRate = 60; // Shoots every 60 frames (1 second)
    }

    @Override
    protected void shoot(int dx, int dy) {
        int startX = gp.player.x + gp.tileSize / 2;
        int startY = gp.player.y + gp.tileSize / 2;

        // Main projectile
        gp.projectiles.add(new Projectile(startX, startY, dx, dy, damage, 8, gp));
        
        // Spread projectiles
        if (dx != 0) { // Horizontal shot
            gp.projectiles.add(new Projectile(startX, startY, dx, dy - 1, damage, 8, gp));
            gp.projectiles.add(new Projectile(startX, startY, dx, dy + 1, damage, 8, gp));
        } else { // Vertical shot
            gp.projectiles.add(new Projectile(startX, startY, dx - 1, dy, damage, 8, gp));
            gp.projectiles.add(new Projectile(startX, startY, dx + 1, dy, damage, 8, gp));
        }
        
        fireCooldown = fireRate;
    }
}

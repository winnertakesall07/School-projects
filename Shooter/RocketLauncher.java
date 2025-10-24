public class RocketLauncher extends Weapon {
    public RocketLauncher(GamePanel gp) {
        super(gp);
        damage = 40;
        fireRate = 120;
    }
    
    @Override
    protected void shoot(int dx, int dy) {
        int startX = gp.player.x + gp.tileSize / 2;
        int startY = gp.player.y + gp.tileSize / 2;
        Projectile proj = new Projectile(startX, startY, dx, dy, damage, 6, gp);
        proj.setColor(new java.awt.Color(255, 0, 0));
        proj.setExplosionRadius(80);
        gp.projectiles.add(proj);
        fireCooldown = fireRate;
    }
}
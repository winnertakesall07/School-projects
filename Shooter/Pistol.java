public class Pistol extends Weapon {
    public Pistol(GamePanel gp) {
        super(gp);
        damage = 10;
        fireRate = 30; // Shoots every 30 frames (0.5 seconds)
    }

    @Override
    protected void shoot(int dx, int dy) {
        int startX = gp.player.x + gp.tileSize / 2;
        int startY = gp.player.y + gp.tileSize / 2;
        Projectile proj = new Projectile(startX, startY, dx, dy, damage, 10, gp);
        proj.setStatusEffect(appliedEffect);
        gp.projectiles.add(proj);
        fireCooldown = fireRate;
    }
}

public class LaserBeam extends Weapon {
    public LaserBeam(GamePanel gp) {
        super(gp);
        damage = 20;
        fireRate = 30;
    }
    
    @Override
    protected void shoot(int dx, int dy) {
        int startX = gp.player.x + gp.tileSize / 2;
        int startY = gp.player.y + gp.tileSize / 2;
        Projectile proj = new Projectile(startX, startY, dx, dy, damage, 14, gp);
        proj.setColor(new java.awt.Color(0, 255, 255));
        proj.setPierce(999);
        gp.projectiles.add(proj);
        fireCooldown = fireRate;
    }
}
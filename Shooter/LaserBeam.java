public class LaserBeam extends Weapon {
    public LaserBeam(GamePanel gp) {
        super(gp);
        damage = 10;  // Nerfed from 20 to 10
        fireRate = 25; // Nerfed from 30 to 25
    }
    
    @Override
    protected void shoot(int dx, int dy) {
        int startX = gp.player.x + gp.tileSize / 2;
        int startY = gp.player.y + gp.tileSize / 2;
        Projectile proj = new Projectile(startX, startY, dx, dy, damage, 14, gp);
        proj.setColor(new java.awt.Color(0, 255, 255));
        proj.setPierce(5); // Nerfed from 999 to 5
        proj.setStatusEffect(appliedEffect);
        gp.projectiles.add(proj);
        fireCooldown = fireRate;
    }
}
public class Sniper extends Weapon {
    public Sniper(GamePanel gp) {
        super(gp);
        damage = 50;
        fireRate = 90; // Shoots every 90 frames (1.5 seconds)
    }
    
    @Override
    protected void shoot(int dx, int dy) {
        int startX = gp.player.x + gp.tileSize / 2;
        int startY = gp.player.y + gp.tileSize / 2;
        Projectile proj = new Projectile(startX, startY, dx, dy, damage, 15, gp);
        proj.setColor(new java.awt.Color(150, 0, 255));
        gp.projectiles.add(proj);
        fireCooldown = fireRate;
    }
}
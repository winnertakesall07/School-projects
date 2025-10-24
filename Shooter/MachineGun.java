public class MachineGun extends Weapon {
    public MachineGun(GamePanel gp) {
        super(gp);
        damage = 8;
        fireRate = 5; // Very fast - shoots every 5 frames
    }
    
    @Override
    protected void shoot(int dx, int dy) {
        int startX = gp.player.x + gp.tileSize / 2;
        int startY = gp.player.y + gp.tileSize / 2;
        Projectile proj = new Projectile(startX, startY, dx, dy, damage, 12, gp);
        proj.setColor(new java.awt.Color(255, 150, 0));
        gp.projectiles.add(proj);
        fireCooldown = fireRate;
    }
}
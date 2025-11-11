public class MachineGun extends Weapon {
    public MachineGun(GamePanel gp) {
        super(gp);
        damage = 5;  // Nerfed from 8 to 5
        fireRate = 8; // Nerfed from 5 to 8
    }
    
    @Override
    protected void shoot(int dx, int dy) {
        int startX = gp.player.x + gp.tileSize / 2;
        int startY = gp.player.y + gp.tileSize / 2;
        Projectile proj = new Projectile(startX, startY, dx, dy, damage, 12, gp);
        proj.setColor(new java.awt.Color(255, 150, 0));
        proj.setStatusEffect(appliedEffect);
        proj.setSpriteKey("bullet");
        gp.projectiles.add(proj);
        fireCooldown = fireRate;
    }
}
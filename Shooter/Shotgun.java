
public class Shotgun extends Weapon {
    public Shotgun(GamePanel gp) {
        super(gp);
        damage = 7;  // Buffed from 8 to 7 per pellet, 5 pellets total = ~35 damage
        fireRate = 50; // Buffed from 60 to 50
    }

    @Override
    protected void shoot(int dx, int dy) {
        int startX = gp.player.x + gp.tileSize / 2;
        int startY = gp.player.y + gp.tileSize / 2;

        // 5-pellet spread
        Projectile p1 = new Projectile(startX, startY, dx, dy, damage, 8, gp);
        p1.setStatusEffect(appliedEffect);
        gp.projectiles.add(p1);
        
        // Spread projectiles
        if (dx != 0) { // Horizontal shot
            Projectile p2 = new Projectile(startX, startY, dx, dy - 1, damage, 8, gp);
            p2.setStatusEffect(appliedEffect);
            gp.projectiles.add(p2);
            
            Projectile p3 = new Projectile(startX, startY, dx, dy + 1, damage, 8, gp);
            p3.setStatusEffect(appliedEffect);
            gp.projectiles.add(p3);
            
            Projectile p4 = new Projectile(startX, startY, dx, dy - 2, damage, 7, gp);
            p4.setStatusEffect(appliedEffect);
            gp.projectiles.add(p4);
            
            Projectile p5 = new Projectile(startX, startY, dx, dy + 2, damage, 7, gp);
            p5.setStatusEffect(appliedEffect);
            gp.projectiles.add(p5);
        } else { // Vertical shot
            Projectile p2 = new Projectile(startX, startY, dx - 1, dy, damage, 8, gp);
            p2.setStatusEffect(appliedEffect);
            gp.projectiles.add(p2);
            
            Projectile p3 = new Projectile(startX, startY, dx + 1, dy, damage, 8, gp);
            p3.setStatusEffect(appliedEffect);
            gp.projectiles.add(p3);
            
            Projectile p4 = new Projectile(startX, startY, dx - 2, dy, damage, 7, gp);
            p4.setStatusEffect(appliedEffect);
            gp.projectiles.add(p4);
            
            Projectile p5 = new Projectile(startX, startY, dx + 2, dy, damage, 7, gp);
            p5.setStatusEffect(appliedEffect);
            gp.projectiles.add(p5);
        }
        
        fireCooldown = fireRate;
    }
}

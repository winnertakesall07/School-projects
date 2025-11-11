public class Axe extends MeleeWeapon {

    public Axe(GamePanel gp) {
        super(gp);
        damage = 35;  // Buffed from 20 to 35
        fireRate = 24; // relatively quick spin
    }

    @Override
    protected void swing() {
        int pcx = gp.player.x + gp.tileSize / 2;
        int pcy = gp.player.y + gp.tileSize / 2;

        // A quick spin hitbox approximated by a circle
        int radius = gp.tileSize; // around the player
        int lifetime = 12;

        MeleeHitbox hb = MeleeHitbox.circle(pcx, pcy, radius, damage, lifetime);
        hb.setStatusEffect(appliedEffect);
        hb.setSpriteKey("melee_arc");
        gp.meleeHitboxes.add(hb);
    }
}
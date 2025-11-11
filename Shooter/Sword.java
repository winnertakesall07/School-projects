public class Sword extends MeleeWeapon {

    public Sword(GamePanel gp) {
        super(gp);
        damage = 35;
        fireRate = 28; // moderate
    }

    @Override
    protected void swing() {
        int pcx = gp.player.x + gp.tileSize / 2;
        int pcy = gp.player.y + gp.tileSize / 2;

        int dx = gp.mouseH.mouseX - pcx;
        int dy = gp.mouseH.mouseY - pcy;

        // Cardinal direction toward mouse
        boolean horizontal = Math.abs(dx) >= Math.abs(dy);

        int length = 50;   // shorter than spear
        int thickness = 28; // fatter arc-ish area
        int lifetime = 10;

        int rx, ry, rw, rh;

        if (horizontal) {
            if (dx >= 0) {
                // swing to the right front
                rx = pcx + 12;
            } else {
                // swing to the left front
                rx = pcx - 12 - length;
            }
            ry = pcy - thickness / 2;
            rw = length;
            rh = thickness;
        } else {
            if (dy >= 0) {
                // swing downward front
                ry = pcy + 12;
            } else {
                // swing upward front
                ry = pcy - 12 - length;
            }
            rx = pcx - thickness / 2;
            rw = thickness;
            rh = length;
        }

        MeleeHitbox hb = MeleeHitbox.rect(rx, ry, rw, rh, damage, lifetime);
        hb.setStatusEffect(appliedEffect);
        gp.meleeHitboxes.add(hb);
    }
}
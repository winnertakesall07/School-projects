public class Spear extends MeleeWeapon {

    public Spear(GamePanel gp) {
        super(gp);
        damage = 25;
        fireRate = 20; // faster poke
    }

    @Override
    protected void swing() {
        int pcx = gp.player.x + gp.tileSize / 2;
        int pcy = gp.player.y + gp.tileSize / 2;

        int dx = gp.mouseH.mouseX - pcx;
        int dy = gp.mouseH.mouseY - pcy;

        // Cardinal direction toward mouse
        boolean horizontal = Math.abs(dx) >= Math.abs(dy);

        int length = 70;
        int thickness = 12;
        int lifetime = 8;

        int rx, ry, rw, rh;

        if (horizontal) {
            if (dx >= 0) {
                // Right
                rx = pcx + 16;
            } else {
                // Left
                rx = pcx - 16 - length;
            }
            ry = pcy - thickness / 2;
            rw = length;
            rh = thickness;
        } else {
            if (dy >= 0) {
                // Down
                ry = pcy + 16;
            } else {
                // Up
                ry = pcy - 16 - length;
            }
            rx = pcx - thickness / 2;
            rw = thickness;
            rh = length;
        }

        gp.meleeHitboxes.add(MeleeHitbox.rect(rx, ry, rw, rh, damage, lifetime));
    }
}
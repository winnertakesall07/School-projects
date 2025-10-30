public abstract class Weapon {
    protected GamePanel gp;
    protected int damage;
    protected int fireRate; // Lower is faster (in frames)
    protected int fireCooldown;
    protected StatusEffect appliedEffect = StatusEffect.NONE;

    public Weapon(GamePanel gp) {
        this.gp = gp;
        this.fireCooldown = 0;
    }

    public void update() {
        if (fireCooldown > 0) {
            fireCooldown--;
        }

        // Mouse click support: fire once per click toward mouse
        if (canShoot() && gp.mouseH != null && gp.mouseH.consumeLeftClick()) {
            // Calculate direction from player center to mouse
            int pcx = gp.player.x + gp.tileSize / 2;
            int pcy = gp.player.y + gp.tileSize / 2;
            int dx = gp.mouseH.mouseX - pcx;
            int dy = gp.mouseH.mouseY - pcy;
            
            // Convert to coarse cardinal direction
            int cdx = 0, cdy = 0;
            if (Math.abs(dx) > Math.abs(dy)) {
                cdx = dx > 0 ? 1 : -1;
            } else {
                cdy = dy > 0 ? 1 : -1;
            }
            shoot(cdx, cdy);
        }

        // Arrow key support: continuous fire
        if (canShoot()) {
            if (gp.keyH.shootUp) {
                shoot(0, -1);
            } else if (gp.keyH.shootDown) {
                shoot(0, 1);
            } else if (gp.keyH.shootLeft) {
                shoot(-1, 0);
            } else if (gp.keyH.shootRight) {
                shoot(1, 0);
            }
        }
    }

    protected boolean canShoot() {
        return fireCooldown == 0;
    }

    protected abstract void shoot(int dx, int dy);

    // New: for UI display
    public int getDamage() {
        return damage;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public StatusEffect getAppliedEffect() {
        return appliedEffect;
    }

    public void setAppliedEffect(StatusEffect effect) {
        this.appliedEffect = effect;
    }

    public boolean tryApplyEffect(StatusEffect effect) {
        if (appliedEffect == StatusEffect.NONE) {
            appliedEffect = effect;
            return true;
        }
        return false;
    }
}
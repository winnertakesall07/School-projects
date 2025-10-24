
public abstract class Weapon {
    protected GamePanel gp;
    protected int damage;
    protected int fireRate; // Lower is faster (in frames)
    protected int fireCooldown;

    public Weapon(GamePanel gp) {
        this.gp = gp;
        this.fireCooldown = 0;
    }

    public void update() {
        if (fireCooldown > 0) {
            fireCooldown--;
        }

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
}

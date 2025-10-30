public abstract class MeleeWeapon extends Weapon {

    public MeleeWeapon(GamePanel gp) {
        super(gp);
    }

    // Melee uses mouse click (edge) to swing once
    @Override
    public void update() {
        if (fireCooldown > 0) fireCooldown--;

        if (fireCooldown == 0 && gp.mouseH != null && gp.mouseH.consumeLeftClick()) {
            swing();
            fireCooldown = fireRate;
        }
    }

    // Not used for melee; provided to satisfy abstract in Weapon
    @Override
    protected void shoot(int dx, int dy) {}

    protected abstract void swing();
}
public abstract class MeleeWeapon extends Weapon {
    
    // Latches to ensure single swing per keypress
    private boolean upLatch = false;
    private boolean downLatch = false;
    private boolean leftLatch = false;
    private boolean rightLatch = false;

    public MeleeWeapon(GamePanel gp) {
        super(gp);
    }

    // Melee uses mouse click (edge) to swing once, or arrow keys with single-swing-per-press
    @Override
    public void update() {
        if (fireCooldown > 0) {
            fireCooldown--;
        }

        // Mouse click: swing toward mouse direction
        if (fireCooldown == 0 && gp.mouseH != null && gp.mouseH.consumeLeftClick()) {
            swing();
            fireCooldown = fireRate;
            return;
        }

        // Arrow keys: single swing per keypress using latches
        if (fireCooldown == 0) {
            if (gp.keyH.shootUp && !upLatch) {
                swing();
                fireCooldown = fireRate;
                upLatch = true;
            } else if (gp.keyH.shootDown && !downLatch) {
                swing();
                fireCooldown = fireRate;
                downLatch = true;
            } else if (gp.keyH.shootLeft && !leftLatch) {
                swing();
                fireCooldown = fireRate;
                leftLatch = true;
            } else if (gp.keyH.shootRight && !rightLatch) {
                swing();
                fireCooldown = fireRate;
                rightLatch = true;
            }
        }

        // Reset latches when keys are released
        if (!gp.keyH.shootUp) upLatch = false;
        if (!gp.keyH.shootDown) downLatch = false;
        if (!gp.keyH.shootLeft) leftLatch = false;
        if (!gp.keyH.shootRight) rightLatch = false;
    }

    // Not used for melee; provided to satisfy abstract in Weapon
    @Override
    protected void shoot(int dx, int dy) {}

    protected abstract void swing();
}
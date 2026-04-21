import java.awt.*;

/**
 * The player character — a Celtic warrior defending Ireland.
 *
 * Movement : WASD / arrow keys (diagonal normalised)
 * Attack   : SPACE (melee swing, 30-frame cooldown)
 * Invincibility frames after damage prevent one-hit combos.
 */
public class Player {

    // ── Size & movement ───────────────────────────────────────────────────────
    private float x, y;
    public  static final int SIZE  = 32;
    private static final float SPEED = 3.0f;

    // ── Health ────────────────────────────────────────────────────────────────
    private int health;
    private static final int MAX_HEALTH = 5;

    // ── Combat ────────────────────────────────────────────────────────────────
    private static final int ATTACK_DAMAGE  = 2;
    private static final int ATTACK_RANGE   = 62;  // pixels from centre
    private static final int ATTACK_COOLDOWN = 30; // frames
    private int attackCooldownTimer = 0;

    // ── Invincibility after hit ───────────────────────────────────────────────
    private static final int INVINCIBLE_FRAMES = 60;
    private int invincibleTimer = 0;

    // ── Last facing direction (for sword visual) ──────────────────────────────
    private int faceDx = 1, faceDy = 0;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Player(int startX, int startY) {
        this.x      = startX;
        this.y      = startY;
        this.health = MAX_HEALTH;
    }

    // ── Update ────────────────────────────────────────────────────────────────
    public void update(KeyHandler keys, int screenW, int screenH) {
        int dx = 0, dy = 0;
        if (keys.upPressed)    dy = -1;
        if (keys.downPressed)  dy =  1;
        if (keys.leftPressed)  dx = -1;
        if (keys.rightPressed) dx =  1;

        if (dx != 0) faceDx = dx;
        if (dy != 0) faceDy = dy;

        if (dx != 0 && dy != 0) {
            // Normalise diagonal movement
            x += dx * SPEED * 0.707f;
            y += dy * SPEED * 0.707f;
        } else {
            x += dx * SPEED;
            y += dy * SPEED;
        }

        // Clamp to screen
        x = Math.max(0, Math.min(screenW - SIZE, x));
        y = Math.max(0, Math.min(screenH - SIZE, y));

        if (attackCooldownTimer > 0) attackCooldownTimer--;
        if (invincibleTimer     > 0) invincibleTimer--;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    public void draw(Graphics2D g) {
        // Flicker during invincibility
        if (invincibleTimer > 0 && (invincibleTimer / 5) % 2 == 0) return;

        int px = (int) x, py = (int) y;

        // Body (green Celtic tunic)
        g.setColor(new Color(55, 155, 55));
        g.fillOval(px, py, SIZE, SIZE);
        g.setColor(new Color(28, 95, 28));
        g.setStroke(new BasicStroke(2));
        g.drawOval(px, py, SIZE, SIZE);

        // Face
        g.setColor(new Color(220, 195, 158));
        g.fillOval(px + SIZE / 4, py + SIZE / 4, SIZE / 2, SIZE / 2);

        // Sword (line in facing direction)
        g.setColor(new Color(185, 185, 205));
        g.setStroke(new BasicStroke(3));
        int cx = px + SIZE / 2, cy = py + SIZE / 2;
        g.drawLine(cx, cy, cx + faceDx * 22, cy + faceDy * 22);

        // Attack flash: glowing tip when attacking
        if (attackCooldownTimer > ATTACK_COOLDOWN - 10) {
            g.setColor(new Color(255, 255, 150, 180));
            g.fillOval(cx + faceDx * 20 - 5, cy + faceDy * 20 - 5, 10, 10);
        }

        g.setStroke(new BasicStroke(1));
    }

    // ── Combat helpers ────────────────────────────────────────────────────────
    public boolean canAttack() { return attackCooldownTimer <= 0; }

    public void performAttack() { attackCooldownTimer = ATTACK_COOLDOWN; }

    public boolean isInAttackRange(float ex, float ey) {
        float dx = getCenterX() - ex;
        float dy = getCenterY() - ey;
        return dx * dx + dy * dy <= (float) ATTACK_RANGE * ATTACK_RANGE;
    }

    public void takeDamage(int amount) {
        if (invincibleTimer > 0) return;
        health -= amount;
        invincibleTimer = INVINCIBLE_FRAMES;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public boolean isAlive()       { return health > 0; }
    public int     getHealth()     { return health; }
    public int     getMaxHealth()  { return MAX_HEALTH; }
    public int     getAttackDamage() { return ATTACK_DAMAGE; }
    public float   getCenterX()    { return x + SIZE / 2f; }
    public float   getCenterY()    { return y + SIZE / 2f; }
    public int     getSize()       { return SIZE; }
}

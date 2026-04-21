import java.awt.*;
import java.util.List;

/**
 * The player character — a Celtic/Irish fighter defending Ireland.
 *
 * Weapon changes by chapter:
 *   0 (Norman Conquest) — Claymore sword: wide melee arc, no bullets
 *   1 (British Rule)    — Flintlock musket: fires bullets, slow cooldown (75 frames)
 *   2 (Easter Rising)   — Lee-Enfield rifle: fires bullets, faster cooldown (40 frames)
 *
 * Aiming: player always faces the mouse cursor, enabling full 360° free aim.
 * Movement: WASD / arrow keys with normalised diagonal movement.
 */
public class Player {

    // ── Position & size ───────────────────────────────────────────────────────
    private float x, y;
    public  static final int   SIZE  = 32;
    private static final float SPEED = 3.5f;

    // ── Health ────────────────────────────────────────────────────────────────
    private int health;
    private static final int MAX_HEALTH = 6;

    // ── Chapter-based weapon stats ────────────────────────────────────────────
    //   index 0 = sword, 1 = musket, 2 = rifle
    private static final int[] ATTACK_DAMAGES   = { 2, 3, 4 };
    private static final int[] ATTACK_COOLDOWNS = { 25, 75, 40 };  // frames
    private static final int   MELEE_RANGE      = 72;              // pixels (chapter 0)
    private int attackCooldownTimer = 0;

    // ── Invincibility frames after being hit ──────────────────────────────────
    private static final int INVINCIBLE_FRAMES = 60;
    private int invincibleTimer = 0;

    // ── Aim direction (unit vector, driven by mouse position) ─────────────────
    private float faceDx = 1f, faceDy = 0f;

    // ── Current chapter ───────────────────────────────────────────────────────
    private final int chapter;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Player(int startX, int startY, int chapter) {
        this.x       = startX;
        this.y       = startY;
        this.health  = MAX_HEALTH;
        this.chapter = chapter;
    }

    // ── Update ────────────────────────────────────────────────────────────────
    public void update(KeyHandler keys, int screenW, int screenH, List<CoverObject> covers) {
        // Movement input
        int mx = 0, my = 0;
        if (keys.upPressed)    my = -1;
        if (keys.downPressed)  my =  1;
        if (keys.leftPressed)  mx = -1;
        if (keys.rightPressed) mx =  1;

        float newX = x, newY = y;
        if (mx != 0 && my != 0) {
            newX += mx * SPEED * 0.707f;
            newY += my * SPEED * 0.707f;
        } else {
            newX += mx * SPEED;
            newY += my * SPEED;
        }

        // Cover collision resolution (slide along edges)
        for (CoverObject c : covers) {
            if (c.overlapsEntity(newX, newY, SIZE)) {
                if (!c.overlapsEntity(newX, y, SIZE)) {
                    newY = y;   // can still move in X
                } else if (!c.overlapsEntity(x, newY, SIZE)) {
                    newX = x;   // can still move in Y
                } else {
                    newX = x; newY = y;
                }
            }
        }

        x = Math.max(0, Math.min(screenW - SIZE, newX));
        y = Math.max(0, Math.min(screenH - SIZE, newY));

        // Mouse-driven aim direction
        float aimDx = keys.mouseX - getCenterX();
        float aimDy = keys.mouseY - getCenterY();
        float aimLen = (float) Math.sqrt(aimDx * aimDx + aimDy * aimDy);
        if (aimLen > 5f) {
            faceDx = aimDx / aimLen;
            faceDy = aimDy / aimLen;
        }

        if (attackCooldownTimer > 0) attackCooldownTimer--;
        if (invincibleTimer     > 0) invincibleTimer--;
    }

    // ── Attack ────────────────────────────────────────────────────────────────

    /**
     * Attempt an attack (called when the fire key is held and canAttack() is true).
     * Returns a Bullet for ranged chapters, null for melee (chapter 0).
     * Sets the attack cooldown timer internally.
     */
    public Bullet performAttack() {
        attackCooldownTimer = ATTACK_COOLDOWNS[chapter];
        if (chapter == 0) {
            return null;    // melee — caller checks isMeleeHitting()
        }
        return new Bullet(getCenterX(), getCenterY(), faceDx, faceDy,
                          ATTACK_DAMAGES[chapter], true);
    }

    /**
     * For chapter 0: returns true if the enemy centre is within the sword's
     * 90° forward arc and within melee range.
     */
    public boolean isMeleeHitting(float ex, float ey) {
        float dx = ex - getCenterX();
        float dy = ey - getCenterY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > MELEE_RANGE) return false;
        float dot = (dx / dist) * faceDx + (dy / dist) * faceDy;
        return dot > 0.5f;  // within ~60° half-angle
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    public void draw(Graphics2D g) {
        // Flicker during invincibility
        if (invincibleTimer > 0 && (invincibleTimer / 5) % 2 == 0) return;

        int px = (int) x, py = (int) y;
        int cx = px + SIZE / 2, cy = py + SIZE / 2;

        // Body
        g.setColor(new Color(55, 155, 55));
        g.fillOval(px, py, SIZE, SIZE);
        g.setColor(new Color(28, 95, 28));
        g.setStroke(new BasicStroke(2));
        g.drawOval(px, py, SIZE, SIZE);

        // Face — shifts in aim direction so it looks like the character is facing it
        int faceCX = cx + (int)(faceDx * 6);
        int faceCY = cy + (int)(faceDy * 6);
        g.setColor(new Color(220, 195, 158));
        g.fillOval(faceCX - SIZE / 5, faceCY - SIZE / 5, SIZE * 2 / 5, SIZE * 2 / 5);

        // Weapon
        if (chapter == 0) {
            drawSword(g, cx, cy);
        } else {
            drawGun(g, cx, cy, chapter);
        }

        // Muzzle flash / swing flash
        if (attackCooldownTimer > ATTACK_COOLDOWNS[chapter] - 10) {
            g.setColor(new Color(255, 230, 80, 200));
            int flashDist = (chapter == 0) ? 22 : 28;
            g.fillOval(cx + (int)(faceDx * flashDist) - 6,
                       cy + (int)(faceDy * flashDist) - 6, 12, 12);
        }

        g.setStroke(new BasicStroke(1));
    }

    private void drawSword(Graphics2D g, int cx, int cy) {
        // Blade
        g.setColor(new Color(185, 185, 205));
        g.setStroke(new BasicStroke(3));
        g.drawLine(cx, cy, cx + (int)(faceDx * 28), cy + (int)(faceDy * 28));
        // Cross-guard (perpendicular)
        float perpX = -faceDy, perpY = faceDx;
        g.setColor(new Color(140, 100, 60));
        g.setStroke(new BasicStroke(4));
        g.drawLine(cx + (int)(faceDx * 10 - perpX * 8), cy + (int)(faceDy * 10 - perpY * 8),
                   cx + (int)(faceDx * 10 + perpX * 8), cy + (int)(faceDy * 10 + perpY * 8));
    }

    private void drawGun(Graphics2D g, int cx, int cy, int ch) {
        // Stock
        g.setColor(new Color(110, 75, 40));
        g.setStroke(new BasicStroke(6));
        g.drawLine(cx - (int)(faceDx * 6), cy - (int)(faceDy * 6),
                   cx + (int)(faceDx * 10), cy + (int)(faceDy * 10));
        // Barrel (longer for rifle in ch2)
        int barrelLen = (ch == 1) ? 24 : 30;
        g.setColor(new Color(80, 60, 40));
        g.setStroke(new BasicStroke(3));
        g.drawLine(cx + (int)(faceDx * 8), cy + (int)(faceDy * 8),
                   cx + (int)(faceDx * barrelLen), cy + (int)(faceDy * barrelLen));
    }

    // ── Damage & health ───────────────────────────────────────────────────────
    public void takeDamage(int amount) {
        if (invincibleTimer > 0) return;
        health -= amount;
        invincibleTimer = INVINCIBLE_FRAMES;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public boolean canAttack()       { return attackCooldownTimer <= 0; }
    public boolean isAlive()         { return health > 0; }
    public int     getHealth()       { return health; }
    public int     getMaxHealth()    { return MAX_HEALTH; }
    public int     getAttackDamage() { return ATTACK_DAMAGES[chapter]; }
    public int     getAttackCooldown() { return ATTACK_COOLDOWNS[chapter]; }
    public int     getAttackTimer()  { return attackCooldownTimer; }
    public float   getCenterX()      { return x + SIZE / 2f; }
    public float   getCenterY()      { return y + SIZE / 2f; }
    public float   getX()            { return x; }
    public float   getY()            { return y; }
    public int     getSize()         { return SIZE; }
    public int     getChapter()      { return chapter; }
}

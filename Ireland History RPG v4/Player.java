import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * The player character — a Celtic / Irish fighter defending Ireland.
 *
 * Weapon changes by chapter:
 *   0 (Norman Conquest)     — Claymore sword: wide melee arc, no bullets
 *   1 (British Rule)        — Flintlock musket: fires bullets, slow cooldown
 *   2 (Easter Rising)       — Lee-Enfield rifle: fires bullets, faster cooldown
 *
 * V3 additions:
 *   - War-cry text displayed periodically above the player
 *   - Stores world-space coordinates (camera offset applied during rendering)
 */
public class Player {

    // ── Position & size ───────────────────────────────────────────────────────
    private float x, y;
    public  static final int   SIZE  = 32;
    private static final float SPEED = 3.8f;

    // ── Health ────────────────────────────────────────────────────────────────
    private int health;
    private static final int MAX_HEALTH = 8;

    // ── Chapter-based weapon stats ────────────────────────────────────────────
    private static final int[] ATTACK_DAMAGES   = { 2, 3, 4 };
    private static final int[] ATTACK_COOLDOWNS = { 22, 70, 38 };
    private static final int   MELEE_RANGE      = 80;
    private int attackCooldownTimer = 0;

    // ── Invincibility frames after being hit ──────────────────────────────────
    private static final int INVINCIBLE_FRAMES = 60;
    private int invincibleTimer = 0;

    // ── Aim direction ─────────────────────────────────────────────────────────
    private float faceDx = 1f, faceDy = 0f;

    // ── Current chapter ───────────────────────────────────────────────────────
    private final int chapter;

    // ── War cry ───────────────────────────────────────────────────────────────
    private static final String[][] WAR_CRIES = {
        // Chapter 0 — Norman Conquest
        { "\u00c9ire Abu!", "For the Clan!", "Faugh A Ballagh!", "Stand firm, brothers!", "Ireland forever!" },
        // Chapter 1 — British Rule
        { "United Irishmen, charge!", "\u00c9ire go Br\u00e1ch!", "Liberty or Death!", "No surrender!", "Rise up!" },
        // Chapter 2 — Easter Rising
        { "\u00d3gl\u00e1igh na h\u00c9ireann!", "Up the Republic!", "For Ireland!", "Fight on!", "Never kneel!" }
    };

    private String  currentWarCry      = null;
    private int     warCryDisplayTimer = 0;
    private int     warCryIntervalTimer;
    private static final int WAR_CRY_DISPLAY = 120;  // 2 s at 60 FPS
    private static final Random RNG = new Random();

    // ── Constructor ───────────────────────────────────────────────────────────
    public Player(float startX, float startY, int chapter) {
        this.x       = startX;
        this.y       = startY;
        this.health  = MAX_HEALTH;
        this.chapter = chapter;
        this.warCryIntervalTimer = 180 + RNG.nextInt(240);
    }

    // ── Update ────────────────────────────────────────────────────────────────
    public void update(KeyHandler keys, int worldW, int worldH, List<CoverObject> covers,
                       float mouseWorldX, float mouseWorldY) {
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
                    newY = y;
                } else if (!c.overlapsEntity(x, newY, SIZE)) {
                    newX = x;
                } else {
                    newX = x; newY = y;
                }
            }
        }

        x = Math.max(0, Math.min(worldW - SIZE, newX));
        y = Math.max(0, Math.min(worldH - SIZE, newY));

        // Mouse-driven aim direction (mouse coords already converted to world space by caller)
        float aimDx = mouseWorldX - getCenterX();
        float aimDy = mouseWorldY - getCenterY();
        float aimLen = (float) Math.sqrt(aimDx * aimDx + aimDy * aimDy);
        if (aimLen > 5f) {
            faceDx = aimDx / aimLen;
            faceDy = aimDy / aimLen;
        }

        if (attackCooldownTimer > 0) attackCooldownTimer--;
        if (invincibleTimer     > 0) invincibleTimer--;

        // War cry timer
        if (warCryIntervalTimer > 0) {
            warCryIntervalTimer--;
        } else {
            String[] cries = WAR_CRIES[Math.min(chapter, 2)];
            currentWarCry       = cries[RNG.nextInt(cries.length)];
            warCryDisplayTimer  = WAR_CRY_DISPLAY;
            warCryIntervalTimer = 200 + RNG.nextInt(280);
        }
        if (warCryDisplayTimer > 0) warCryDisplayTimer--;
        else currentWarCry = null;
    }

    // ── Attack ────────────────────────────────────────────────────────────────
    public Bullet performAttack() {
        attackCooldownTimer = ATTACK_COOLDOWNS[chapter];
        if (chapter == 0) return null;
        return new Bullet(getCenterX(), getCenterY(), faceDx, faceDy,
                          ATTACK_DAMAGES[chapter], true, false);
    }

    public boolean isMeleeHitting(float ex, float ey) {
        float dx   = ex - getCenterX();
        float dy   = ey - getCenterY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > MELEE_RANGE) return false;
        float dot = (dx / dist) * faceDx + (dy / dist) * faceDy;
        return dot > 0.5f;
    }

    // ── Rendering (world-space coordinates — caller applies camera transform) ─
    public void draw(Graphics2D g) {
        if (invincibleTimer > 0 && (invincibleTimer / 5) % 2 == 0) return;

        int px = (int) x, py = (int) y;
        int cx = px + SIZE / 2, cy = py + SIZE / 2;

        // Body
        g.setColor(new Color(55, 155, 55));
        g.fillOval(px, py, SIZE, SIZE);
        g.setColor(new Color(28, 95, 28));
        g.setStroke(new BasicStroke(2));
        g.drawOval(px, py, SIZE, SIZE);

        // Face
        int faceCX = cx + (int)(faceDx * 6);
        int faceCY = cy + (int)(faceDy * 6);
        g.setColor(new Color(220, 195, 158));
        g.fillOval(faceCX - SIZE / 5, faceCY - SIZE / 5, SIZE * 2 / 5, SIZE * 2 / 5);

        // Weapon
        if (chapter == 0) drawSword(g, cx, cy);
        else              drawGun(g, cx, cy, chapter);

        // Muzzle / swing flash
        if (attackCooldownTimer > ATTACK_COOLDOWNS[chapter] - 10) {
            g.setColor(new Color(255, 230, 80, 200));
            int flashDist = (chapter == 0) ? 22 : 28;
            g.fillOval(cx + (int)(faceDx * flashDist) - 6,
                       cy + (int)(faceDy * flashDist) - 6, 12, 12);
        }

        g.setStroke(new BasicStroke(1));

        // War cry speech bubble
        if (currentWarCry != null && warCryDisplayTimer > 0) {
            drawWarCry(g, px, py, currentWarCry);
        }
    }

    private void drawSword(Graphics2D g, int cx, int cy) {
        g.setColor(new Color(185, 185, 205));
        g.setStroke(new BasicStroke(3));
        g.drawLine(cx, cy, cx + (int)(faceDx * 30), cy + (int)(faceDy * 30));
        float perpX = -faceDy, perpY = faceDx;
        g.setColor(new Color(140, 100, 60));
        g.setStroke(new BasicStroke(4));
        g.drawLine(cx + (int)(faceDx * 10 - perpX * 9), cy + (int)(faceDy * 10 - perpY * 9),
                   cx + (int)(faceDx * 10 + perpX * 9), cy + (int)(faceDy * 10 + perpY * 9));
    }

    private void drawGun(Graphics2D g, int cx, int cy, int ch) {
        g.setColor(new Color(110, 75, 40));
        g.setStroke(new BasicStroke(6));
        g.drawLine(cx - (int)(faceDx * 6), cy - (int)(faceDy * 6),
                   cx + (int)(faceDx * 10), cy + (int)(faceDy * 10));
        int barrelLen = (ch == 1) ? 26 : 32;
        g.setColor(new Color(80, 60, 40));
        g.setStroke(new BasicStroke(3));
        g.drawLine(cx + (int)(faceDx * 8), cy + (int)(faceDy * 8),
                   cx + (int)(faceDx * barrelLen), cy + (int)(faceDy * barrelLen));
    }

    static void drawWarCry(Graphics2D g, int entityX, int entityY, String cry) {
        Font font = new Font("SansSerif", Font.BOLD, 11);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(cry);
        int th = fm.getHeight();
        int bx = entityX - tw / 2 - 4;
        int by = entityY - 28 - th;
        int bw = tw + 8;
        int bh = th + 4;
        // Bubble background
        g.setColor(new Color(255, 255, 230, 210));
        g.fillRoundRect(bx, by, bw, bh, 8, 8);
        g.setColor(new Color(100, 90, 50));
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(bx, by, bw, bh, 8, 8);
        // Text
        g.setColor(new Color(40, 40, 40));
        g.drawString(cry, bx + 4, by + bh - fm.getDescent() - 2);
    }

    // ── Damage & health ───────────────────────────────────────────────────────
    public void takeDamage(int amount) {
        if (invincibleTimer > 0) return;
        health -= amount;
        invincibleTimer = INVINCIBLE_FRAMES;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public boolean canAttack()         { return attackCooldownTimer <= 0; }
    public boolean isAlive()           { return health > 0; }
    public int     getHealth()         { return health; }
    public int     getMaxHealth()      { return MAX_HEALTH; }
    public int     getAttackDamage()   { return ATTACK_DAMAGES[chapter]; }
    public int     getAttackCooldown() { return ATTACK_COOLDOWNS[chapter]; }
    public int     getAttackTimer()    { return attackCooldownTimer; }
    public float   getCenterX()        { return x + SIZE / 2f; }
    public float   getCenterY()        { return y + SIZE / 2f; }
    public float   getX()              { return x; }
    public float   getY()              { return y; }
    public int     getSize()           { return SIZE; }
    public int     getChapter()        { return chapter; }
    public float   getFaceDx()         { return faceDx; }
    public float   getFaceDy()         { return faceDy; }
}

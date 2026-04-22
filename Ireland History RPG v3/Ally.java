import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * An allied Irish soldier who fights alongside the player.
 *
 * Chapter 0 — Irish Clan Warriors (melee)
 * Chapter 1 — United Irishmen (melee + some ranged)
 * Chapter 2 — IRA fighters (melee + ranged)
 *
 * Allies are intentionally weaker than enemies in raw numbers — they will
 * slowly lose ground without the player's help (COMMANDER enemies ignore
 * allies entirely and hunt the player, forcing the player to engage).
 *
 * Allies shout historically fitting war cries at random intervals.
 */
public class Ally {

    public static final int SIZE = 30;

    public enum SubType { MELEE, RANGED }

    // ── Position ──────────────────────────────────────────────────────────────
    private float x, y;
    private static final float SPEED = 2.0f;

    // ── Stats ─────────────────────────────────────────────────────────────────
    private int health, maxHealth;
    private final int attackDamage;
    private final int chapter;
    private final SubType subType;

    // ── Attack cooldown ───────────────────────────────────────────────────────
    private static final int MELEE_COOLDOWN  = 110;
    private static final int RANGED_COOLDOWN = 100;
    private int attackTimer = new Random().nextInt(80);

    // ── Invincibility frames ──────────────────────────────────────────────────
    private static final int INV_FRAMES = 45;
    private int invTimer = 0;

    // ── War cry ───────────────────────────────────────────────────────────────
    private static final String[][] WAR_CRIES = {
        // Chapter 0 — Irish Clansmen
        { "\u00c9ire Abu!", "For the Clan!", "Drive them back!", "Brothers, charge!", "Faugh A Ballagh!" },
        // Chapter 1 — United Irishmen
        { "United Irishmen!", "Liberty or Death!", "\u00c9ire go Br\u00e1ch!", "Rebel on!", "No Union, no peace!" },
        // Chapter 2 — IRA
        { "\u00d3gl\u00e1igh na h\u00c9ireann!", "Up the Republic!", "For the Rising!", "Hold the GPO!", "Ireland free!" }
    };

    private String  currentWarCry      = null;
    private int     warCryDisplayTimer = 0;
    private int     warCryIntervalTimer;
    private static final int WAR_CRY_DISPLAY = 110;
    private static final Random RNG = new Random();

    // ── Visuals ───────────────────────────────────────────────────────────────
    // Irish green shades per chapter
    private static final Color[][] COLORS = {
        { new Color( 38, 130,  55), new Color( 18,  85,  30) },  // Ch0 — dark forest green
        { new Color( 45, 160,  75), new Color( 22, 100,  45) },  // Ch1 — mid green
        { new Color( 30, 120,  60), new Color(  8,  75,  30) }   // Ch2 — IRA dark green
    };

    // ── Constructor ───────────────────────────────────────────────────────────
    public Ally(float startX, float startY, int chapter, SubType subType) {
        this.x        = startX;
        this.y        = startY;
        this.chapter  = Math.min(chapter, 2);
        this.subType  = subType;

        switch (this.chapter) {
            case 0:
                health = maxHealth = 5;
                attackDamage = 1;
                break;
            case 1:
                health = maxHealth = 6;
                attackDamage = (subType == SubType.RANGED) ? 2 : 1;
                break;
            default: // chapter 2
                health = maxHealth = 7;
                attackDamage = (subType == SubType.RANGED) ? 2 : 2;
                break;
        }

        warCryIntervalTimer = 160 + RNG.nextInt(280);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Update this ally's AI for one tick.
     *
     * @param enemies  active enemies (targets)
     * @param covers   cover objects for collision
     * @param worldW   world width
     * @param worldH   world height
     * @return a Bullet if a ranged ally fires this tick, otherwise null
     */
    public Bullet update(List<Enemy> enemies, List<CoverObject> covers,
                         int worldW, int worldH) {
        if (attackTimer > 0) attackTimer--;
        if (invTimer    > 0) invTimer--;

        Enemy target = findNearestEnemy(enemies);
        Bullet shot  = null;

        if (target != null) {
            float dx   = target.getCenterX() - getCenterX();
            float dy   = target.getCenterY() - getCenterY();
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < 0.01f) dist = 0.01f;

            if (subType == SubType.MELEE) {
                // Charge the nearest enemy
                if (dist > SIZE * 0.8f) {
                    moveStep(dx / dist, dy / dist, covers, worldW, worldH);
                }
            } else {
                // Ranged: maintain preferred distance
                float preferred = 180f;
                if (dist > preferred + 40) {
                    moveStep(dx / dist, dy / dist, covers, worldW, worldH);
                } else if (dist < preferred - 40) {
                    moveStep(-dx / dist, -dy / dist, covers, worldW, worldH);
                }
                // Shoot when in range and line of sight
                if (attackTimer <= 0 && dist < 320f) {
                    boolean los = hasLineOfSight(getCenterX(), getCenterY(),
                                                 target.getCenterX(), target.getCenterY(),
                                                 covers);
                    if (los) {
                        attackTimer = RANGED_COOLDOWN;
                        shot = new Bullet(getCenterX(), getCenterY(), dx, dy,
                                          attackDamage, false, true);
                    }
                }
            }
        }

        // War cry
        if (warCryIntervalTimer > 0) {
            warCryIntervalTimer--;
        } else {
            String[] pool = WAR_CRIES[chapter];
            currentWarCry       = pool[RNG.nextInt(pool.length)];
            warCryDisplayTimer  = WAR_CRY_DISPLAY;
            warCryIntervalTimer = 220 + RNG.nextInt(320);
        }
        if (warCryDisplayTimer > 0) warCryDisplayTimer--;
        else currentWarCry = null;

        return shot;
    }

    // ── Melee attack ──────────────────────────────────────────────────────────
    public boolean canMeleeAttack() { return attackTimer <= 0 && subType == SubType.MELEE; }
    public void    performMeleeAttack() { attackTimer = MELEE_COOLDOWN; }

    public boolean isOverlapping(float cx, float cy, int size) {
        float ddx    = getCenterX() - cx;
        float ddy    = getCenterY() - cy;
        float radSum = (SIZE + size) / 2f;
        return ddx * ddx + ddy * ddy < radSum * radSum;
    }

    // ── Movement ──────────────────────────────────────────────────────────────
    private void moveStep(float dirX, float dirY,
                          List<CoverObject> covers, int worldW, int worldH) {
        float newX = x + dirX * SPEED;
        float newY = y + dirY * SPEED;

        for (CoverObject c : covers) {
            if (c.overlapsEntity(newX, newY, SIZE)) {
                if (!c.overlapsEntity(newX, y, SIZE)) {
                    newY = y;
                } else if (!c.overlapsEntity(x, newY, SIZE)) {
                    newX = x;
                } else {
                    newX = x; newY = y;
                }
                break;
            }
        }

        x = Math.max(0, Math.min(worldW - SIZE, newX));
        y = Math.max(0, Math.min(worldH - SIZE, newY));
    }

    private Enemy findNearestEnemy(List<Enemy> enemies) {
        Enemy best  = null;
        float bestD = Float.MAX_VALUE;
        for (Enemy e : enemies) {
            if (e.isDead()) continue;
            float dx = e.getCenterX() - getCenterX();
            float dy = e.getCenterY() - getCenterY();
            float d  = dx * dx + dy * dy;
            if (d < bestD) { bestD = d; best = e; }
        }
        return best;
    }

    private boolean hasLineOfSight(float x1, float y1, float x2, float y2,
                                   List<CoverObject> covers) {
        for (CoverObject c : covers) {
            if (c.isProtecting(x2, y2, x1, y1)) return false;
        }
        return true;
    }

    // ── Damage ────────────────────────────────────────────────────────────────
    public void takeDamage(int amount) {
        if (invTimer > 0) return;
        health = Math.max(0, health - amount);
        invTimer = INV_FRAMES;
    }

    // ── Rendering (world coords — caller applies camera transform) ─────────────
    public void draw(Graphics2D g) {
        if (invTimer > 0 && (invTimer / 4) % 2 == 0) return;

        int ax = (int) x, ay = (int) y;
        int idx = Math.min(chapter, 2);
        Color body = COLORS[idx][0];
        Color trim = COLORS[idx][1];

        // Body (diamond shape to distinguish from enemy squares and player circle)
        int[] polyX = { ax + SIZE / 2, ax + SIZE, ax + SIZE / 2, ax };
        int[] polyY = { ay, ay + SIZE / 2, ay + SIZE, ay + SIZE / 2 };
        g.setColor(body);
        g.fillPolygon(polyX, polyY, 4);
        g.setColor(trim);
        g.setStroke(new BasicStroke(2));
        g.drawPolygon(polyX, polyY, 4);

        // Face (small circle)
        g.setColor(new Color(215, 185, 148));
        g.fillOval(ax + SIZE / 4, ay + SIZE / 4, SIZE / 2, SIZE / 2 - 2);

        // Weapon indicator
        if (subType == SubType.RANGED) {
            g.setColor(new Color(90, 65, 35));
            g.setStroke(new BasicStroke(3));
            g.drawLine(ax + SIZE / 2, ay + SIZE / 2, ax + SIZE + 10, ay + SIZE / 2);
        }

        // Health bar
        g.setColor(new Color(0, 45, 0));
        g.fillRect(ax, ay - 8, SIZE, 4);
        int filled = Math.max(0, (int)(SIZE * ((float) health / maxHealth)));
        g.setColor(new Color(50, 200, 80));
        g.fillRect(ax, ay - 8, filled, 4);

        g.setStroke(new BasicStroke(1));

        // War cry
        if (currentWarCry != null && warCryDisplayTimer > 0) {
            Player.drawWarCry(g, ax, ay, currentWarCry);
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public boolean isDead()          { return health <= 0; }
    public float   getCenterX()      { return x + SIZE / 2f; }
    public float   getCenterY()      { return y + SIZE / 2f; }
    public int     getAttackDamage() { return attackDamage; }
    public SubType getSubType()      { return subType; }
    public float   getX()            { return x; }
    public float   getY()            { return y; }
}

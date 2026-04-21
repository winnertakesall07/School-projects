import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * An enemy soldier with chapter-appropriate appearance, weapon, and AI.
 *
 * Chapter 0 — Normans:
 *   All MELEE knights. They charge directly but spread out slightly.
 *
 * Chapter 1 — British soldiers (1800s):
 *   MELEE  — bayonet-charge foot soldiers (faster, high HP)
 *   RANGED — musket-armed redcoats (maintain distance, slow shots)
 *
 * Chapter 2 — Black-and-Tans (1916):
 *   MELEE   — aggressive close-combat Tans
 *   RANGED  — rifle-armed Tans (maintain distance, rapid shots)
 *   FLANKER — Tans that try to circle around to the player's side
 *
 * Ranged enemies retreat to cover when their HP drops below 40%.
 */
public class Enemy {

    public static final int SIZE = 32;

    public enum SubType { MELEE, RANGED, FLANKER }

    // ── Position & movement ───────────────────────────────────────────────────
    private float x, y;
    private final float speed;

    // ── Stats ─────────────────────────────────────────────────────────────────
    private int health, maxHealth, attackDamage;
    private final int     chapterType;
    private final SubType subType;

    // ── AI state ──────────────────────────────────────────────────────────────
    private CoverObject targetCover = null;
    private boolean     inCover     = false;

    // Flank offset — a fixed perpendicular offset applied to the player position
    // so the flanker approaches from the side.
    private final float flankOffsetX, flankOffsetY;
    private static final Random RNG = new Random();

    // ── Cooldowns ─────────────────────────────────────────────────────────────
    private static final int MELEE_COOLDOWN  = 90;   // ~1.5 s at 60 FPS
    private static final int SHOOT_COOLDOWN_CH1 = 130; // slow musket
    private static final int SHOOT_COOLDOWN_CH2 =  75; // faster rifle
    private int attackTimer    = 0;
    private int shootCooldown;

    // ── Visuals ───────────────────────────────────────────────────────────────
    // [chapterType][0=body, 1=trim]
    private static final Color[][] COLORS = {
        { new Color(148,  98,  48), new Color( 95,  58, 18) },  // Norman
        { new Color( 75,  75, 155), new Color(175,  38, 38) },  // British
        { new Color( 48,  48,  48), new Color(115,  88, 38) }   // Tan
    };

    // ── Constructor ───────────────────────────────────────────────────────────
    public Enemy(int startX, int startY, int chapter, SubType subType) {
        this.x          = startX;
        this.y          = startY;
        this.chapterType = Math.min(chapter, 2);
        this.subType    = subType;
        this.shootCooldown = (chapter == 1) ? SHOOT_COOLDOWN_CH1 : SHOOT_COOLDOWN_CH2;

        // Speed: melee fastest, ranged slowest
        float base = 0.8f + chapter * 0.25f;
        switch (subType) {
            case MELEE:   speed = base + 0.55f; break;
            case FLANKER: speed = base + 0.35f; break;
            default:      speed = base;          break;
        }

        // Stats by chapter & subtype
        switch (chapterType) {
            case 0:
                health = maxHealth = 4;
                attackDamage = 1;
                break;
            case 1:
                health = maxHealth = (subType == SubType.RANGED ? 3 : 5);
                attackDamage      = (subType == SubType.RANGED ? 2 : 1);
                break;
            default: // chapter 2
                health = maxHealth = 5;
                attackDamage      = (subType == SubType.RANGED ? 3 : 2);
                break;
        }

        // Random perpendicular flank offset (left or right of approach axis)
        float sign    = RNG.nextBoolean() ? 1f : -1f;
        float offLen  = 110 + RNG.nextInt(90);
        flankOffsetX  =  sign * offLen;
        flankOffsetY  = (RNG.nextBoolean() ? 0.6f : -0.6f) * sign * offLen;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Advances the enemy's AI for one game tick.
     *
     * @param targetX  player centre X
     * @param targetY  player centre Y
     * @param covers   list of cover objects on the current map
     * @param screenW  screen width (for clamping)
     * @param screenH  screen height (for clamping)
     * @return a Bullet if this enemy fires this frame, otherwise null
     */
    public Bullet update(float targetX, float targetY,
                         List<CoverObject> covers,
                         int screenW, int screenH) {
        if (attackTimer > 0) attackTimer--;

        float dx    = targetX - getCenterX();
        float dy    = targetY - getCenterY();
        float dist  = (float) Math.sqrt(dx * dx + dy * dy);

        boolean lowHP = health < maxHealth * 0.4f;
        Bullet  shot  = null;

        // ── Low-health ranged: seek nearest cover ─────────────────────────────
        if (lowHP && subType == SubType.RANGED) {
            if (targetCover == null && !covers.isEmpty()) {
                targetCover = findNearestCover(covers);
            }
            if (targetCover != null) {
                float ccx = targetCover.x + targetCover.width  / 2f;
                float ccy = targetCover.y + targetCover.height / 2f;
                float cdx = ccx - getCenterX();
                float cdy = ccy - getCenterY();
                float cdist = (float) Math.sqrt(cdx * cdx + cdy * cdy);
                if (cdist > SIZE * 0.6f) {
                    moveStep(cdx / cdist, cdy / cdist, covers, screenW, screenH);
                    inCover = false;
                } else {
                    inCover = true;
                }
            }
        } else {
            inCover     = false;
            if (!lowHP) targetCover = null;

            // ── Normal AI by subtype ──────────────────────────────────────────
            switch (subType) {
                case MELEE:
                    // Direct charge
                    if (dist > SIZE * 0.8f) {
                        moveStep(dx / dist, dy / dist, covers, screenW, screenH);
                    }
                    break;

                case RANGED: {
                    // Maintain preferred distance, shoot when in line of sight
                    float preferred = 195f;
                    if (dist > preferred + 40) {
                        moveStep(dx / dist, dy / dist, covers, screenW, screenH);
                    } else if (dist < preferred - 40) {
                        moveStep(-dx / dist, -dy / dist, covers, screenW, screenH);
                    }
                    if (attackTimer <= 0 && dist < 350f) {
                        boolean los = hasLineOfSight(getCenterX(), getCenterY(),
                                                     targetX, targetY, covers);
                        if (los) {
                            attackTimer = shootCooldown;
                            shot = new Bullet(getCenterX(), getCenterY(),
                                              dx, dy, attackDamage, false);
                        }
                    }
                    break;
                }

                case FLANKER: {
                    // Approach the player from the side by adding a perpendicular offset
                    float flankTargX = targetX + flankOffsetX;
                    float flankTargY = targetY + flankOffsetY;
                    float ftdx = flankTargX - getCenterX();
                    float ftdy = flankTargY - getCenterY();
                    float ftdist = (float) Math.sqrt(ftdx * ftdx + ftdy * ftdy);
                    if (dist > SIZE * 0.9f && ftdist > 5f) {
                        moveStep(ftdx / ftdist, ftdy / ftdist, covers, screenW, screenH);
                    }
                    break;
                }
            }
        }

        return shot;
    }

    /** Move one step in direction (dirX, dirY), resolving cover collisions. */
    private void moveStep(float dirX, float dirY,
                          List<CoverObject> covers, int screenW, int screenH) {
        float newX = x + dirX * speed;
        float newY = y + dirY * speed;

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

        x = Math.max(0, Math.min(screenW - SIZE, newX));
        y = Math.max(0, Math.min(screenH - SIZE, newY));
    }

    private CoverObject findNearestCover(List<CoverObject> covers) {
        CoverObject best  = null;
        float       bestD = Float.MAX_VALUE;
        for (CoverObject c : covers) {
            float cx = c.x + c.width  / 2f;
            float cy = c.y + c.height / 2f;
            float ddx = cx - getCenterX();
            float ddy = cy - getCenterY();
            float d   = ddx * ddx + ddy * ddy;
            if (d < bestD) { bestD = d; best = c; }
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

    // ── Rendering ─────────────────────────────────────────────────────────────
    public void draw(Graphics2D g) {
        int ex = (int) x, ey = (int) y;
        Color body = COLORS[chapterType][0];
        Color trim = COLORS[chapterType][1];

        // Body (square distinguishes enemies from the round player)
        g.setColor(body);
        g.fillRect(ex, ey, SIZE, SIZE);
        g.setColor(trim);
        g.setStroke(new BasicStroke(2));
        g.drawRect(ex, ey, SIZE, SIZE);

        // Face
        g.setColor(new Color(218, 188, 158));
        g.fillOval(ex + SIZE / 4, ey + 3, SIZE / 2, SIZE / 2 - 2);

        // Weapon indicator
        if (subType == SubType.RANGED) {
            g.setColor(new Color(80, 60, 40));
            g.setStroke(new BasicStroke(3));
            // Gun barrel pointing right (approximate — full aiming would need more code)
            g.drawLine(ex + SIZE / 2, ey + SIZE / 2, ex + SIZE + 10, ey + SIZE / 2);
        } else if (chapterType == 0) {
            // Norman sword
            g.setColor(new Color(185, 185, 205));
            g.setStroke(new BasicStroke(3));
            g.drawLine(ex + SIZE / 2, ey + SIZE / 2, ex + SIZE + 8, ey + SIZE / 2);
        }

        // Cover indicator (green bar above head)
        if (inCover) {
            g.setColor(new Color(80, 200, 80, 170));
            g.fillRect(ex, ey - 12, SIZE, 4);
        }

        // Health bar
        g.setColor(new Color(55, 0, 0));
        g.fillRect(ex, ey - 8, SIZE, 4);
        int filled = Math.max(0, (int)(SIZE * ((float) health / maxHealth)));
        g.setColor(new Color(195, 48, 48));
        g.fillRect(ex, ey - 8, filled, 4);

        g.setStroke(new BasicStroke(1));
    }

    // ── Combat ────────────────────────────────────────────────────────────────
    public boolean canMeleeAttack()   { return attackTimer <= 0 && subType != SubType.RANGED; }
    public void    performMeleeAttack() { attackTimer = MELEE_COOLDOWN; }

    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
        // Reset cover target if hit — makes them react
        if (health > 0) targetCover = null;
    }

    public boolean isOverlapping(float playerCX, float playerCY, int playerSize) {
        float ddx    = getCenterX() - playerCX;
        float ddy    = getCenterY() - playerCY;
        float radSum = (SIZE + playerSize) / 2f;
        return ddx * ddx + ddy * ddy < radSum * radSum;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public boolean isDead()          { return health <= 0; }
    public int     getAttackDamage() { return attackDamage; }
    public float   getCenterX()      { return x + SIZE / 2f; }
    public float   getCenterY()      { return y + SIZE / 2f; }
    public SubType getSubType()      { return subType; }
    public int     getChapterType()  { return chapterType; }
}

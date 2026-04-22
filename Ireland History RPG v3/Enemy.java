import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * An enemy soldier with chapter-appropriate appearance, weapon, AI, and war cry.
 *
 * Chapter 0 — Norman knights:
 *   MELEE    — straight charge
 *   FLANKER  — approaches from the side
 *   COMMANDER— high-HP elite knight (always targets player)
 *
 * Chapter 1 — British soldiers (1800s):
 *   MELEE    — bayonet charge foot soldiers
 *   RANGED   — musket-armed redcoats (maintain distance)
 *   COMMANDER— Sergeant (high HP, pursues player relentlessly)
 *
 * Chapter 2 — Black-and-Tans (1916–1921):
 *   MELEE    — aggressive close-combat Tans
 *   RANGED   — rifle-armed Tans
 *   FLANKER  — Tans that circle to the player's side
 *   COMMANDER— Officer (high HP, directs fire at player)
 *
 * COMMANDER enemies always target the player and ignore allies, making them
 * particularly dangerous — the player must deal with them personally.
 */
public class Enemy {

    public static final int SIZE = 32;

    public enum SubType { MELEE, RANGED, FLANKER, COMMANDER }

    // ── Position ──────────────────────────────────────────────────────────────
    private float x, y;
    private final float speed;

    // ── Stats ─────────────────────────────────────────────────────────────────
    private int health, maxHealth, attackDamage;
    private final int     chapterType;
    private final SubType subType;

    // ── AI state ──────────────────────────────────────────────────────────────
    private CoverObject targetCover = null;
    private boolean     inCover     = false;

    private final float flankOffsetX, flankOffsetY;
    private static final Random RNG = new Random();

    // ── Cooldowns ─────────────────────────────────────────────────────────────
    private static final int MELEE_COOLDOWN     =  80;
    private static final int SHOOT_COOLDOWN_CH1 = 120;
    private static final int SHOOT_COOLDOWN_CH2 =  65;
    private int attackTimer   = RNG.nextInt(60);  // stagger initial shots
    private int shootCooldown;

    // ── Targeting ─────────────────────────────────────────────────────────────
    // Whether this enemy is currently focusing the player rather than allies
    private boolean targetingPlayer = true;
    private int     targetSwitchTimer = 0;

    // ── War cry ───────────────────────────────────────────────────────────────
    private static final String[][] WAR_CRIES = {
        // Chapter 0 — Normans
        { "Dieu et mon droit!", "For the Crown!", "Advance, knights!", "No mercy!", "Charge!" },
        // Chapter 1 — British soldiers
        { "For King and Country!", "Hold the line!", "Fire at will!", "The Empire stands!", "Advance!" },
        // Chapter 2 — Black and Tans
        { "Burn it down!", "Surrender, rebel!", "Get the IRA!", "Open fire!", "No quarter!" }
    };
    private static final String[] COMMANDER_CRIES = {
        "I will end you!", "Face me, coward!", "You cannot win!", "For the Commander!"
    };

    private String  currentWarCry      = null;
    private int     warCryDisplayTimer = 0;
    private int     warCryIntervalTimer;
    private static final int WAR_CRY_DISPLAY = 100;

    // ── Visuals ───────────────────────────────────────────────────────────────
    private static final Color[][] COLORS = {
        { new Color(148,  98,  48), new Color( 95,  58, 18) },
        { new Color( 75,  75, 155), new Color(175,  38, 38) },
        { new Color( 48,  48,  48), new Color(115,  88, 38) }
    };

    // ── Constructor ───────────────────────────────────────────────────────────
    public Enemy(int startX, int startY, int chapter, SubType subType) {
        this.x           = startX;
        this.y           = startY;
        this.chapterType = Math.min(chapter, 2);
        this.subType     = subType;
        this.shootCooldown = (chapter == 1) ? SHOOT_COOLDOWN_CH1 : SHOOT_COOLDOWN_CH2;

        float base = 0.9f + chapter * 0.22f;
        switch (subType) {
            case MELEE:     speed = base + 0.60f; break;
            case FLANKER:   speed = base + 0.40f; break;
            case COMMANDER: speed = base + 0.50f; break;
            default:        speed = base;          break; // RANGED
        }

        switch (chapterType) {
            case 0:
                if (subType == SubType.COMMANDER) {
                    health = maxHealth = 14;
                    attackDamage = 2;
                } else {
                    health = maxHealth = 5;
                    attackDamage = 1;
                }
                break;
            case 1:
                if (subType == SubType.COMMANDER) {
                    health = maxHealth = 16;
                    attackDamage = 2;
                } else {
                    health = maxHealth = (subType == SubType.RANGED ? 4 : 6);
                    attackDamage      = (subType == SubType.RANGED ? 2 : 1);
                }
                break;
            default: // chapter 2
                if (subType == SubType.COMMANDER) {
                    health = maxHealth = 18;
                    attackDamage = 3;
                } else {
                    health = maxHealth = 6;
                    attackDamage      = (subType == SubType.RANGED ? 3 : 2);
                }
                break;
        }

        float sign   = RNG.nextBoolean() ? 1f : -1f;
        float offLen = 120 + RNG.nextInt(100);
        flankOffsetX = sign * offLen;
        flankOffsetY = (RNG.nextBoolean() ? 0.6f : -0.6f) * sign * offLen;

        warCryIntervalTimer = 200 + RNG.nextInt(300);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * @param targetX  player centre X (world coords)
     * @param targetY  player centre Y (world coords)
     * @param allies   list of ally soldiers (can be null / empty)
     */
    public Bullet update(float targetX, float targetY,
                         List<CoverObject> covers,
                         List<Ally> allies,
                         int worldW, int worldH) {
        if (attackTimer       > 0) attackTimer--;
        if (targetSwitchTimer > 0) targetSwitchTimer--;

        // COMMANDER always locks onto the player
        float aimX = targetX, aimY = targetY;
        if (subType != SubType.COMMANDER && !allies.isEmpty() && targetSwitchTimer <= 0) {
            // 40% chance to switch target to an ally if one is nearby
            Ally nearestAlly = findNearestAlly(allies);
            if (nearestAlly != null) {
                float adx = nearestAlly.getCenterX() - getCenterX();
                float ady = nearestAlly.getCenterY() - getCenterY();
                float ad  = adx * adx + ady * ady;
                float pd  = (targetX - getCenterX()) * (targetX - getCenterX())
                          + (targetY - getCenterY()) * (targetY - getCenterY());
                if (ad < pd * 1.5f && RNG.nextFloat() < 0.4f) {
                    aimX = nearestAlly.getCenterX();
                    aimY = nearestAlly.getCenterY();
                    targetingPlayer = false;
                    targetSwitchTimer = 60 + RNG.nextInt(60);
                } else {
                    targetingPlayer = true;
                }
            }
        }

        float dx   = aimX - getCenterX();
        float dy   = aimY - getCenterY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 0.01f) dist = 0.01f;

        boolean lowHP = health < maxHealth * 0.35f;
        Bullet  shot  = null;

        // Low-health ranged: seek nearest cover
        if (lowHP && (subType == SubType.RANGED || subType == SubType.COMMANDER)) {
            if (targetCover == null && !covers.isEmpty()) {
                targetCover = findNearestCover(covers);
            }
            if (targetCover != null) {
                float ccx  = targetCover.x + targetCover.width  / 2f;
                float ccy  = targetCover.y + targetCover.height / 2f;
                float cdx  = ccx - getCenterX();
                float cdy  = ccy - getCenterY();
                float cd   = (float) Math.sqrt(cdx * cdx + cdy * cdy);
                if (cd > SIZE * 0.6f) {
                    moveStep(cdx / cd, cdy / cd, covers, worldW, worldH);
                    inCover = false;
                } else {
                    inCover = true;
                }
            }
        } else {
            inCover = false;
            if (!lowHP) targetCover = null;

            switch (subType) {
                case MELEE:
                    if (dist > SIZE * 0.8f) moveStep(dx / dist, dy / dist, covers, worldW, worldH);
                    break;

                case COMMANDER:
                    // Aggressive direct charge on player only
                    if (dist > SIZE * 0.8f) moveStep(dx / dist, dy / dist, covers, worldW, worldH);
                    // Also fire if ranged distance — commanders can do both
                    if (chapterType > 0 && attackTimer <= 0 && dist < 380f) {
                        boolean los = hasLineOfSight(getCenterX(), getCenterY(),
                                                     targetX, targetY, covers);
                        if (los) {
                            attackTimer = shootCooldown;
                            float tdx = targetX - getCenterX();
                            float tdy = targetY - getCenterY();
                            shot = new Bullet(getCenterX(), getCenterY(),
                                              tdx, tdy, attackDamage + 1, false, false);
                        }
                    }
                    break;

                case RANGED: {
                    float preferred = 200f;
                    if (dist > preferred + 45) {
                        moveStep(dx / dist, dy / dist, covers, worldW, worldH);
                    } else if (dist < preferred - 45) {
                        moveStep(-dx / dist, -dy / dist, covers, worldW, worldH);
                    }
                    if (attackTimer <= 0 && dist < 380f) {
                        boolean los = hasLineOfSight(getCenterX(), getCenterY(),
                                                     aimX, aimY, covers);
                        if (los) {
                            attackTimer = shootCooldown;
                            shot = new Bullet(getCenterX(), getCenterY(),
                                              dx, dy, attackDamage, false, false);
                        }
                    }
                    break;
                }

                case FLANKER: {
                    float flankTargX = targetX + flankOffsetX;
                    float flankTargY = targetY + flankOffsetY;
                    float ftdx = flankTargX - getCenterX();
                    float ftdy = flankTargY - getCenterY();
                    float ftd  = (float) Math.sqrt(ftdx * ftdx + ftdy * ftdy);
                    if (dist > SIZE * 0.9f && ftd > 5f) {
                        moveStep(ftdx / ftd, ftdy / ftd, covers, worldW, worldH);
                    }
                    break;
                }
            }
        }

        // War cry
        if (warCryIntervalTimer > 0) {
            warCryIntervalTimer--;
        } else {
            String[] pool = (subType == SubType.COMMANDER) ? COMMANDER_CRIES
                                                           : WAR_CRIES[chapterType];
            currentWarCry       = pool[RNG.nextInt(pool.length)];
            warCryDisplayTimer  = WAR_CRY_DISPLAY;
            warCryIntervalTimer = 240 + RNG.nextInt(320);
        }
        if (warCryDisplayTimer > 0) warCryDisplayTimer--;
        else currentWarCry = null;

        return shot;
    }

    private void moveStep(float dirX, float dirY,
                          List<CoverObject> covers, int worldW, int worldH) {
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

        x = Math.max(0, Math.min(worldW - SIZE, newX));
        y = Math.max(0, Math.min(worldH - SIZE, newY));
    }

    private CoverObject findNearestCover(List<CoverObject> covers) {
        CoverObject best  = null;
        float       bestD = Float.MAX_VALUE;
        for (CoverObject c : covers) {
            float cx  = c.x + c.width  / 2f - getCenterX();
            float cy  = c.y + c.height / 2f - getCenterY();
            float d   = cx * cx + cy * cy;
            if (d < bestD) { bestD = d; best = c; }
        }
        return best;
    }

    private Ally findNearestAlly(List<Ally> allies) {
        Ally  best  = null;
        float bestD = Float.MAX_VALUE;
        for (Ally a : allies) {
            if (a.isDead()) continue;
            float dx = a.getCenterX() - getCenterX();
            float dy = a.getCenterY() - getCenterY();
            float d  = dx * dx + dy * dy;
            if (d < bestD) { bestD = d; best = a; }
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

    // ── Rendering (world coords — caller applies camera transform) ─────────────
    public void draw(Graphics2D g) {
        int ex = (int) x, ey = (int) y;
        Color body = COLORS[chapterType][0];
        Color trim = COLORS[chapterType][1];

        // COMMANDER gets a larger, distinctive look
        if (subType == SubType.COMMANDER) {
            g.setColor(trim);
            g.fillRect(ex - 3, ey - 3, SIZE + 6, SIZE + 6);
            g.setColor(body.darker());
            g.setStroke(new BasicStroke(3));
            g.drawRect(ex - 3, ey - 3, SIZE + 6, SIZE + 6);
        }

        g.setColor(body);
        g.fillRect(ex, ey, SIZE, SIZE);
        g.setColor(trim);
        g.setStroke(new BasicStroke(2));
        g.drawRect(ex, ey, SIZE, SIZE);

        // Face
        g.setColor(new Color(218, 188, 158));
        g.fillOval(ex + SIZE / 4, ey + 3, SIZE / 2, SIZE / 2 - 2);

        // Weapon / role indicator
        if (subType == SubType.RANGED || (subType == SubType.COMMANDER && chapterType > 0)) {
            g.setColor(new Color(80, 60, 40));
            g.setStroke(new BasicStroke(3));
            g.drawLine(ex + SIZE / 2, ey + SIZE / 2, ex + SIZE + 12, ey + SIZE / 2);
        } else if (chapterType == 0) {
            g.setColor(new Color(185, 185, 205));
            g.setStroke(new BasicStroke(3));
            g.drawLine(ex + SIZE / 2, ey + SIZE / 2, ex + SIZE + 10, ey + SIZE / 2);
        }

        // COMMANDER label
        if (subType == SubType.COMMANDER) {
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.setColor(new Color(255, 220, 50));
            g.drawString("CMD", ex + 2, ey + SIZE + 13);
        }

        // Cover indicator
        if (inCover) {
            g.setColor(new Color(80, 200, 80, 170));
            g.fillRect(ex, ey - 12, SIZE, 4);
        }

        // Health bar
        g.setColor(new Color(55, 0, 0));
        g.fillRect(ex, ey - 8, SIZE, 4);
        int filled = Math.max(0, (int)(SIZE * ((float) health / maxHealth)));
        g.setColor(subType == SubType.COMMANDER ? new Color(220, 160, 20) : new Color(195, 48, 48));
        g.fillRect(ex, ey - 8, filled, 4);

        g.setStroke(new BasicStroke(1));

        // War cry
        if (currentWarCry != null && warCryDisplayTimer > 0) {
            Player.drawWarCry(g, ex, ey, currentWarCry);
        }
    }

    // ── Combat ────────────────────────────────────────────────────────────────
    public boolean canMeleeAttack() {
        return attackTimer <= 0 && subType != SubType.RANGED;
    }
    public void performMeleeAttack() { attackTimer = MELEE_COOLDOWN; }

    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
        if (health > 0) targetCover = null;
    }

    public boolean isOverlapping(float cx, float cy, int size) {
        float ddx    = getCenterX() - cx;
        float ddy    = getCenterY() - cy;
        float radSum = (SIZE + size) / 2f;
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

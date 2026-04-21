import java.awt.*;

/**
 * An enemy soldier whose appearance and stats depend on the chapter:
 *   0 — Norman knight  (brown, 3 HP, 1 dmg)
 *   1 — British soldier (dark blue/red, 4 HP, 1 dmg)
 *   2 — Black-and-Tan  (dark, 5 HP, 2 dmg)
 *
 * AI: moves directly toward the player, attacks on contact.
 */
public class Enemy {

    // ── Size & movement ───────────────────────────────────────────────────────
    private float x, y;
    public  static final int SIZE  = 32;
    private static final float[] SPEEDS = { 1.0f, 1.2f, 1.5f };

    // ── Stats ─────────────────────────────────────────────────────────────────
    private int health, maxHealth, attackDamage;
    private final int type;
    private final float speed;

    // ── Attack cooldown ───────────────────────────────────────────────────────
    private static final int ATTACK_COOLDOWN = 90; // ~1.5 s at 60 FPS
    private int attackTimer = 0;

    // ── Visuals ───────────────────────────────────────────────────────────────
    private static final Color[] BODY_COLORS = {
        new Color(148, 98, 48),    // Norman: earthy brown
        new Color(75,  75, 155),   // British: dark blue
        new Color(48,  48, 48)     // Black and Tan: dark grey
    };
    private static final Color[] TRIM_COLORS = {
        new Color(95, 58, 18),
        new Color(175, 38, 38),
        new Color(115, 88, 38)
    };
    private static final String[] LABELS = { "Norman", "Soldier", "Officer" };

    // ── Constructor ───────────────────────────────────────────────────────────
    public Enemy(int startX, int startY, int chapter) {
        this.x    = startX;
        this.y    = startY;
        this.type = Math.min(chapter, 2);
        this.speed = SPEEDS[this.type];
        switch (this.type) {
            case 0: health = maxHealth = 3; attackDamage = 1; break;
            case 1: health = maxHealth = 4; attackDamage = 1; break;
            default: health = maxHealth = 5; attackDamage = 2; break;
        }
    }

    // ── Update ────────────────────────────────────────────────────────────────
    public void update(float targetX, float targetY) {
        float dx = targetX - getCenterX();
        float dy = targetY - getCenterY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        // Move toward player unless already touching
        if (dist > SIZE * 0.75f) {
            x += (dx / dist) * speed;
            y += (dy / dist) * speed;
        }

        if (attackTimer > 0) attackTimer--;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    public void draw(Graphics2D g) {
        int ex = (int) x, ey = (int) y;
        Color body = BODY_COLORS[type];
        Color trim = TRIM_COLORS[type];

        // Body (square silhouette for enemies — distinguishes from player)
        g.setColor(body);
        g.fillRect(ex, ey, SIZE, SIZE);
        g.setColor(trim);
        g.setStroke(new BasicStroke(2));
        g.drawRect(ex, ey, SIZE, SIZE);

        // Face
        g.setColor(new Color(218, 188, 158));
        g.fillOval(ex + SIZE / 4, ey + 3, SIZE / 2, SIZE / 2 - 2);

        // Health bar
        int barW = SIZE, barH = 4;
        g.setColor(new Color(55, 0, 0));
        g.fillRect(ex, ey - 8, barW, barH);
        int filled = (int) (barW * ((float) health / maxHealth));
        g.setColor(new Color(195, 48, 48));
        g.fillRect(ex, ey - 8, filled, barH);

        g.setStroke(new BasicStroke(1));
    }

    // ── Combat ────────────────────────────────────────────────────────────────
    public boolean canAttack()  { return attackTimer <= 0; }
    public void performAttack() { attackTimer = ATTACK_COOLDOWN; }

    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }

    /** Returns true when the enemy's bounding circle overlaps the player's. */
    public boolean isOverlapping(float playerCX, float playerCY, int playerSize) {
        float dx = getCenterX() - playerCX;
        float dy = getCenterY() - playerCY;
        float radSum = (SIZE + playerSize) / 2f;
        return dx * dx + dy * dy < radSum * radSum;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public boolean isDead()          { return health <= 0; }
    public int     getAttackDamage() { return attackDamage; }
    public float   getCenterX()      { return x + SIZE / 2f; }
    public float   getCenterY()      { return y + SIZE / 2f; }
}

import java.awt.*;

/**
 * A single projectile fired by the player or an enemy.
 * Bullets travel in a straight line at constant speed and are
 * deactivated when they go out-of-bounds, hit a cover object,
 * or hit their intended target.
 */
public class Bullet {

    public float x, y;
    private final float dx, dy;
    private final int   damage;
    public  final boolean fromPlayer;
    private boolean active = true;

    private static final int   RADIUS = 4;
    private static final float SPEED  = 8.5f;

    private final Color mainColor;
    private final Color trailColor;

    public Bullet(float startX, float startY,
                  float aimDx,  float aimDy,
                  int damage,   boolean fromPlayer) {
        this.x          = startX;
        this.y          = startY;
        this.damage     = damage;
        this.fromPlayer = fromPlayer;

        float len = (float) Math.sqrt(aimDx * aimDx + aimDy * aimDy);
        if (len < 0.001f) len = 1f;
        this.dx = (aimDx / len) * SPEED;
        this.dy = (aimDy / len) * SPEED;

        if (fromPlayer) {
            mainColor  = new Color(255, 255, 100);
            trailColor = new Color(255, 220, 50, 110);
        } else {
            mainColor  = new Color(255, 80, 60);
            trailColor = new Color(220, 60, 40, 110);
        }
    }

    // ── Update ────────────────────────────────────────────────────────────────
    public void update() {
        x += dx;
        y += dy;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    public void draw(Graphics2D g) {
        if (!active) return;
        // Trail dot
        g.setColor(trailColor);
        g.fillOval((int)(x - dx - RADIUS / 2f), (int)(y - dy - RADIUS / 2f), RADIUS, RADIUS);
        // Main bullet
        g.setColor(mainColor);
        g.fillOval((int)(x - RADIUS), (int)(y - RADIUS), RADIUS * 2, RADIUS * 2);
    }

    // ── Collision helpers ─────────────────────────────────────────────────────

    public boolean isOutOfBounds(int w, int h) {
        return x < -20 || x > w + 20 || y < -20 || y > h + 20;
    }

    /** True if the bullet's centre is inside the given cover rectangle. */
    public boolean hitsCover(CoverObject cover) {
        return x >= cover.x && x <= cover.x + cover.width
            && y >= cover.y && y <= cover.y + cover.height;
    }

    /** True if the bullet's centre is within radius of a circular entity centre. */
    public boolean hits(float cx, float cy, float entityRadius) {
        float ddx = x - cx;
        float ddy = y - cy;
        float combined = entityRadius + RADIUS;
        return ddx * ddx + ddy * ddy < combined * combined;
    }

    // ── State ─────────────────────────────────────────────────────────────────
    public void    deactivate() { active = false; }
    public boolean isActive()   { return active; }
    public int     getDamage()  { return damage; }
}

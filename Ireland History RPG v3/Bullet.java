import java.awt.*;

/**
 * A single projectile fired by the player, an enemy, or an ally.
 * Travels in a straight line at constant speed; deactivated when it goes
 * out of world bounds, hits a cover object, or hits its target.
 */
public class Bullet {

    public float x, y;
    private final float dx, dy;
    private final int   damage;
    public  final boolean fromPlayer;
    public  final boolean fromAlly;

    private static final int   RADIUS = 4;
    private static final float SPEED  = 9f;

    private final Color mainColor;
    private final Color trailColor;

    /** Player or ally bullet. */
    public Bullet(float startX, float startY,
                  float aimDx,  float aimDy,
                  int damage,   boolean fromPlayer, boolean fromAlly) {
        this.x          = startX;
        this.y          = startY;
        this.damage     = damage;
        this.fromPlayer = fromPlayer;
        this.fromAlly   = fromAlly;

        float len = (float) Math.sqrt(aimDx * aimDx + aimDy * aimDy);
        if (len < 0.001f) len = 1f;
        this.dx = (aimDx / len) * SPEED;
        this.dy = (aimDy / len) * SPEED;

        if (fromPlayer) {
            mainColor  = new Color(255, 255, 100);
            trailColor = new Color(255, 220, 50, 110);
        } else if (fromAlly) {
            mainColor  = new Color(80, 220, 255);
            trailColor = new Color(50, 180, 220, 110);
        } else {
            mainColor  = new Color(255, 80, 60);
            trailColor = new Color(220, 60, 40, 110);
        }
    }

    /** Convenience constructor for enemy bullets (fromPlayer=false, fromAlly=false). */
    public Bullet(float startX, float startY,
                  float aimDx, float aimDy, int damage, boolean fromPlayer) {
        this(startX, startY, aimDx, aimDy, damage, fromPlayer, false);
    }

    // ── Update ────────────────────────────────────────────────────────────────
    public void update() {
        x += dx;
        y += dy;
    }

    // ── Rendering (world coordinates — caller applies camera transform) ────────
    public void draw(Graphics2D g) {
        // Trail dot
        g.setColor(trailColor);
        g.fillOval((int)(x - dx - RADIUS / 2f), (int)(y - dy - RADIUS / 2f), RADIUS, RADIUS);
        // Main bullet
        g.setColor(mainColor);
        g.fillOval((int)(x - RADIUS), (int)(y - RADIUS), RADIUS * 2, RADIUS * 2);
    }

    // ── Collision helpers ─────────────────────────────────────────────────────

    public boolean isOutOfBounds(int worldW, int worldH) {
        return x < -20 || x > worldW + 20 || y < -20 || y > worldH + 20;
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
    public int   getDamage()    { return damage; }
    public float getDx()        { return dx; }
    public float getDy()        { return dy; }
}

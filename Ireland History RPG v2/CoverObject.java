import java.awt.*;

/**
 * A static cover object on the battlefield.
 * Blocks bullets and can be used by both the player and enemies for protection.
 *
 * Types: "wall", "barrel", "tree", "crate", "rubble"
 */
public class CoverObject {

    public final int    x, y, width, height;
    private final String type;
    private final Color mainColor, borderColor, accentColor;

    public CoverObject(int x, int y, int width, int height, String type) {
        this.x = x; this.y = y;
        this.width  = width;
        this.height = height;
        this.type   = type;

        switch (type) {
            case "wall":
                mainColor   = new Color(130, 110, 85);
                borderColor = new Color(90,  70, 50);
                accentColor = new Color(110, 90, 65);
                break;
            case "barrel":
                mainColor   = new Color(100,  70, 40);
                borderColor = new Color( 60,  40, 20);
                accentColor = new Color( 80,  55, 30);
                break;
            case "tree":
                mainColor   = new Color(45, 120, 45);
                borderColor = new Color(25,  80, 25);
                accentColor = new Color(35, 100, 35);
                break;
            case "crate":
                mainColor   = new Color(160, 130, 75);
                borderColor = new Color(110,  85, 45);
                accentColor = new Color(140, 110, 60);
                break;
            case "rubble":
                mainColor   = new Color(120, 110, 100);
                borderColor = new Color( 80,  70,  60);
                accentColor = new Color(100,  90,  80);
                break;
            default:
                mainColor   = Color.GRAY;
                borderColor = Color.DARK_GRAY;
                accentColor = Color.LIGHT_GRAY;
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    public void draw(Graphics2D g) {
        g.setStroke(new BasicStroke(2));
        switch (type) {
            case "barrel":
                g.setColor(mainColor);
                g.fillOval(x, y, width, height);
                g.setColor(borderColor);
                g.drawOval(x, y, width, height);
                // metal rings
                g.setColor(new Color(borderColor.getRed(), borderColor.getGreen(),
                                     borderColor.getBlue(), 160));
                g.drawLine(x + 4, y + height / 3,     x + width - 4, y + height / 3);
                g.drawLine(x + 4, y + 2 * height / 3, x + width - 4, y + 2 * height / 3);
                break;

            case "tree":
                // trunk
                g.setColor(new Color(101, 67, 33));
                g.fillRect(x + width / 2 - 5, y + height - 18, 10, 18);
                // canopy
                g.setColor(mainColor);
                g.fillOval(x, y, width, height - 8);
                g.setColor(borderColor);
                g.drawOval(x, y, width, height - 8);
                break;

            case "wall":
                g.setColor(mainColor);
                g.fillRect(x, y, width, height);
                // brick grid
                g.setColor(accentColor);
                for (int bx = x; bx < x + width;  bx += 20) g.drawLine(bx, y, bx, y + height);
                for (int by = y; by < y + height; by += 12) g.drawLine(x, by, x + width, by);
                g.setColor(borderColor);
                g.drawRect(x, y, width, height);
                break;

            case "rubble":
                g.setColor(mainColor);
                g.fillOval(x, y + height / 4, width, height * 3 / 4);
                g.fillRect(x + width / 4, y, width / 2, height / 2);
                g.setColor(borderColor);
                g.drawOval(x, y + height / 4, width, height * 3 / 4);
                g.drawRect(x + width / 4, y, width / 2, height / 2);
                break;

            default: // crate and fallback
                g.setColor(mainColor);
                g.fillRect(x, y, width, height);
                // cross brace
                g.setColor(accentColor);
                g.drawLine(x, y, x + width, y + height);
                g.drawLine(x + width, y, x, y + height);
                g.setColor(borderColor);
                g.drawRect(x, y, width, height);
        }
        g.setStroke(new BasicStroke(1));
    }

    // ── Collision helpers ─────────────────────────────────────────────────────

    /** True if a point (bx,by) lies inside this cover rectangle. */
    public boolean blocksPoint(float bx, float by) {
        return bx >= x && bx <= x + width && by >= y && by <= y + height;
    }

    /**
     * AABB overlap: true if an entity whose upper-left corner is at (ex,ey)
     * with the given size overlaps this cover.
     */
    public boolean overlapsEntity(float ex, float ey, int entitySize) {
        return ex + entitySize > x && ex < x + width
            && ey + entitySize > y && ey < y + height;
    }

    /**
     * True if this cover's rectangle intersects the line segment from
     * (attackerX, attackerY) to (protectedX, protectedY), meaning
     * the cover is shielding the protected entity from the attacker.
     * Uses the Liang–Barsky algorithm.
     */
    public boolean isProtecting(float protectedX, float protectedY,
                                 float attackerX,  float attackerY) {
        return lineIntersectsRect(attackerX, attackerY, protectedX, protectedY);
    }

    private boolean lineIntersectsRect(float x1, float y1, float x2, float y2) {
        float rx1 = x, ry1 = y, rx2 = x + width, ry2 = y + height;
        float segDx = x2 - x1, segDy = y2 - y1;

        float tMin = 0f, tMax = 1f;
        float[] p = { -segDx,  segDx, -segDy,  segDy };
        float[] q = { x1 - rx1, rx2 - x1, y1 - ry1, ry2 - y1 };

        for (int i = 0; i < 4; i++) {
            float pi = p[i], qi = q[i];
            if (Math.abs(pi) < 1e-6f) {
                if (qi < 0) return false;   // parallel and outside
            } else {
                float t = qi / pi;
                if (pi < 0) tMin = Math.max(tMin, t);
                else        tMax = Math.min(tMax, t);
                if (tMin > tMax) return false;
            }
        }
        return true;
    }
}

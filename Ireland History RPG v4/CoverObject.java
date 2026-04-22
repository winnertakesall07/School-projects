import java.awt.*;

/**
 * A static cover object on the battlefield (world coordinates).
 * Blocks bullets and can be used by both the player and enemies for protection.
 *
 * Standard types  : "wall", "barrel", "tree", "crate", "rubble"
 * Building types  : "castle_wall", "church", "house", "barracks", "gpo"
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
            case "castle_wall":
                mainColor   = new Color(130, 110, 85);
                borderColor = new Color( 90,  70, 50);
                accentColor = new Color(110,  90, 65);
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
            case "church":
                mainColor   = new Color(170, 165, 155);
                borderColor = new Color(100,  95,  85);
                accentColor = new Color(140, 135, 125);
                break;
            case "house":
                mainColor   = new Color(175, 135, 90);
                borderColor = new Color(110,  80, 50);
                accentColor = new Color(150, 115, 75);
                break;
            case "barracks":
                mainColor   = new Color( 80, 110,  75);
                borderColor = new Color( 50,  70,  45);
                accentColor = new Color( 65,  90,  60);
                break;
            case "gpo":
                mainColor   = new Color(200, 195, 180);
                borderColor = new Color(120, 115, 100);
                accentColor = new Color(165, 160, 145);
                break;
            default:
                mainColor   = Color.GRAY;
                borderColor = Color.DARK_GRAY;
                accentColor = Color.LIGHT_GRAY;
        }
    }

    // ── Rendering (world coordinates — caller applies camera transform) ────────
    public void draw(Graphics2D g) {
        g.setStroke(new BasicStroke(2));
        switch (type) {
            case "barrel":      drawBarrel(g);      break;
            case "tree":        drawTree(g);        break;
            case "wall":        drawWall(g);        break;
            case "castle_wall": drawCastleWall(g);  break;
            case "rubble":      drawRubble(g);      break;
            case "church":      drawChurch(g);      break;
            case "house":       drawHouse(g);       break;
            case "barracks":    drawBarracks(g);    break;
            case "gpo":         drawGPO(g);         break;
            default:            drawCrate(g);       break;
        }
        g.setStroke(new BasicStroke(1));
    }

    private void drawBarrel(Graphics2D g) {
        g.setColor(mainColor);
        g.fillOval(x, y, width, height);
        g.setColor(borderColor);
        g.drawOval(x, y, width, height);
        g.setColor(new Color(borderColor.getRed(), borderColor.getGreen(),
                             borderColor.getBlue(), 160));
        g.drawLine(x + 4, y + height / 3,     x + width - 4, y + height / 3);
        g.drawLine(x + 4, y + 2 * height / 3, x + width - 4, y + 2 * height / 3);
    }

    private void drawTree(Graphics2D g) {
        g.setColor(new Color(101, 67, 33));
        g.fillRect(x + width / 2 - 5, y + height - 18, 10, 18);
        g.setColor(mainColor);
        g.fillOval(x, y, width, height - 8);
        g.setColor(borderColor);
        g.drawOval(x, y, width, height - 8);
    }

    private void drawWall(Graphics2D g) {
        g.setColor(mainColor);
        g.fillRect(x, y, width, height);
        g.setColor(accentColor);
        for (int bx = x; bx < x + width;  bx += 20) g.drawLine(bx, y, bx, y + height);
        for (int by = y; by < y + height; by += 12) g.drawLine(x, by, x + width, by);
        g.setColor(borderColor);
        g.drawRect(x, y, width, height);
    }

    private void drawCastleWall(Graphics2D g) {
        // Thick stone wall with crenellations
        g.setColor(mainColor);
        g.fillRect(x, y, width, height);
        // Stone block pattern
        g.setColor(accentColor);
        int brickH = 18, brickW = 24;
        for (int row = 0, by = y; by < y + height; by += brickH, row++) {
            int offset = (row % 2 == 0) ? 0 : brickW / 2;
            for (int bx = x - offset; bx < x + width; bx += brickW) {
                g.drawRect(bx, by, brickW, brickH);
            }
        }
        // Crenellations on top (merlons) — only if tall enough
        if (height >= 40) {
            int merlonW = 18, gapW = 12, merlonH = 16;
            g.setColor(mainColor);
            for (int mx = x; mx < x + width; mx += merlonW + gapW) {
                g.fillRect(mx, y - merlonH, merlonW, merlonH);
                g.setColor(accentColor);
                g.drawRect(mx, y - merlonH, merlonW, merlonH);
                g.setColor(mainColor);
            }
        }
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(2));
        g.drawRect(x, y, width, height);
    }

    private void drawRubble(Graphics2D g) {
        g.setColor(mainColor);
        g.fillOval(x, y + height / 4, width, height * 3 / 4);
        g.fillRect(x + width / 4, y, width / 2, height / 2);
        g.setColor(borderColor);
        g.drawOval(x, y + height / 4, width, height * 3 / 4);
        g.drawRect(x + width / 4, y, width / 2, height / 2);
    }

    private void drawCrate(Graphics2D g) {
        g.setColor(mainColor);
        g.fillRect(x, y, width, height);
        g.setColor(accentColor);
        g.drawLine(x, y, x + width, y + height);
        g.drawLine(x + width, y, x, y + height);
        g.setColor(borderColor);
        g.drawRect(x, y, width, height);
    }

    private void drawChurch(Graphics2D g) {
        // Main body
        g.setColor(mainColor);
        g.fillRect(x, y + height / 4, width, height * 3 / 4);
        // Stone blocks
        g.setColor(accentColor);
        for (int bx = x; bx < x + width; bx += 22) g.drawLine(bx, y + height / 4, bx, y + height);
        for (int by = y + height / 4; by < y + height; by += 14) g.drawLine(x, by, x + width, by);
        // Tower / spire
        int spireBaseX = x + width / 2 - 14;
        g.setColor(mainColor);
        g.fillRect(spireBaseX, y, 28, height / 4 + 10);
        g.setColor(accentColor);
        // Cross on tower
        g.setColor(new Color(200, 200, 210));
        g.setStroke(new BasicStroke(3));
        int crossX = x + width / 2;
        int crossY = y + height / 8;
        g.drawLine(crossX - 8, crossY, crossX + 8, crossY);
        g.drawLine(crossX, crossY - 12, crossX, crossY + 8);
        g.setStroke(new BasicStroke(2));
        // Gothic arch windows
        g.setColor(new Color(160, 190, 220, 180));
        g.fillArc(x + 10, y + height / 2, 20, 28, 0, 180);
        if (width > 60) g.fillArc(x + width - 30, y + height / 2, 20, 28, 0, 180);
        // Border
        g.setColor(borderColor);
        g.drawRect(x, y + height / 4, width, height * 3 / 4);
        g.drawRect(spireBaseX, y, 28, height / 4 + 10);
    }

    private void drawHouse(Graphics2D g) {
        // Walls
        g.setColor(mainColor);
        g.fillRect(x, y + height / 3, width, height * 2 / 3);
        // Roof (triangle)
        int[] roofX = { x - 4, x + width / 2, x + width + 4 };
        int[] roofY = { y + height / 3 + 4, y, y + height / 3 + 4 };
        g.setColor(new Color(140, 60, 40));   // red-brown roof tiles
        g.fillPolygon(roofX, roofY, 3);
        g.setColor(new Color(100, 40, 25));
        g.drawPolygon(roofX, roofY, 3);
        // Door
        int doorW = width / 4, doorH = height / 3;
        int doorX = x + width / 2 - doorW / 2;
        int doorY = y + height - doorH;
        g.setColor(new Color(100, 65, 30));
        g.fillRect(doorX, doorY, doorW, doorH);
        g.setColor(new Color(70, 45, 20));
        g.drawRect(doorX, doorY, doorW, doorH);
        // Windows
        int winW = 14, winH = 14;
        g.setColor(new Color(160, 200, 230, 180));
        g.fillRect(x + 8, y + height / 2 - 2, winW, winH);
        if (width > 60) g.fillRect(x + width - 8 - winW, y + height / 2 - 2, winW, winH);
        g.setColor(borderColor);
        g.drawRect(x, y + height / 3, width, height * 2 / 3);
    }

    private void drawBarracks(Graphics2D g) {
        // Long rectangular military barracks
        g.setColor(mainColor);
        g.fillRect(x, y, width, height);
        // Horizontal course lines
        g.setColor(accentColor);
        for (int by = y + 15; by < y + height; by += 15) g.drawLine(x, by, x + width, by);
        // Multiple windows in a row
        int winW = 14, winH = 20, winY = y + height / 3;
        int numWins = Math.max(2, width / 35);
        int spacing = width / (numWins + 1);
        for (int i = 1; i <= numWins; i++) {
            int wx = x + i * spacing - winW / 2;
            g.setColor(new Color(160, 200, 230, 180));
            g.fillRect(wx, winY, winW, winH);
            g.setColor(borderColor);
            g.drawRect(wx, winY, winW, winH);
        }
        // Flag pole + Union Jack suggestion
        int poleX = x + width - 12;
        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(2));
        g.drawLine(poleX, y - 30, poleX, y + 2);
        // Mini flag
        g.setColor(new Color(200, 30, 30));
        g.fillRect(poleX + 2, y - 30, 20, 12);
        g.setColor(new Color(220, 220, 255));
        g.drawLine(poleX + 2, y - 30, poleX + 22, y - 18); // diagonal
        g.drawLine(poleX + 2, y - 18, poleX + 22, y - 30);
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(2));
        g.drawRect(x, y, width, height);
    }

    private void drawGPO(Graphics2D g) {
        // General Post Office — neoclassical Dublin building
        // Main facade
        g.setColor(mainColor);
        g.fillRect(x, y + height / 5, width, height * 4 / 5);
        // Portico columns
        int colW = 14, colH = height * 3 / 5;
        int colY = y + height / 5;
        int numCols = Math.max(4, width / 40);
        int colSpacing = (width - 20) / (numCols - 1);
        g.setColor(new Color(215, 210, 195));
        for (int i = 0; i < numCols; i++) {
            int cx = x + 10 + i * colSpacing;
            g.fillRect(cx, colY, colW, colH);
            // Capital
            g.setColor(new Color(185, 180, 165));
            g.fillRect(cx - 3, colY, colW + 6, 10);
            g.setColor(new Color(215, 210, 195));
        }
        // Pediment (triangular roof section)
        int[] px = { x + 5, x + width / 2, x + width - 5 };
        int[] py = { y + height / 5 + 4, y, y + height / 5 + 4 };
        g.setColor(mainColor);
        g.fillPolygon(px, py, 3);
        g.setColor(borderColor);
        g.drawPolygon(px, py, 3);
        // Large door
        int doorW = width / 6, doorH = height / 3;
        int doorX = x + width / 2 - doorW / 2;
        g.setColor(new Color(60, 50, 35));
        g.fillArc(doorX, y + height - doorH, doorW, doorH, 0, 180);
        g.fillRect(doorX, y + height - doorH / 2, doorW, doorH / 2);
        // Smoke/battle damage effects (some black marks)
        g.setColor(new Color(30, 30, 30, 100));
        g.fillOval(x + 20, y + height / 3, 30, 20);
        g.fillOval(x + width - 50, y + height / 2, 25, 18);
        // Border
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(2));
        g.drawRect(x, y + height / 5, width, height * 4 / 5);
        // Label
        g.setFont(new Font("Serif", Font.BOLD, 10));
        g.setColor(new Color(50, 50, 50));
        FontMetrics fm = g.getFontMetrics();
        String label = "G.P.O.";
        g.drawString(label, x + (width - fm.stringWidth(label)) / 2, y + height - 5);
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
     * (attackerX, attackerY) to (protectedX, protectedY) — Liang–Barsky algorithm.
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
                if (qi < 0) return false;
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

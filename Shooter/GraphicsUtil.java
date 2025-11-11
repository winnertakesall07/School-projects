import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * GraphicsUtil provides helper methods for drawing sprites with transformations.
 */
public class GraphicsUtil {
    
    /**
     * Draw a sprite centered at the given position.
     * 
     * @param g2 Graphics2D context
     * @param sprite The sprite image
     * @param x Center x position
     * @param y Center y position
     * @param width Width to draw
     * @param height Height to draw
     */
    public static void drawSpriteCentered(Graphics2D g2, BufferedImage sprite, int x, int y, int width, int height) {
        if (sprite == null) return;
        g2.drawImage(sprite, x, y, width, height, null);
    }
    
    /**
     * Draw a sprite rotated around its center.
     * 
     * @param g2 Graphics2D context
     * @param sprite The sprite image
     * @param x Center x position
     * @param y Center y position
     * @param width Width to draw
     * @param height Height to draw
     * @param angle Rotation angle in radians
     */
    public static void drawSpriteRotated(Graphics2D g2, BufferedImage sprite, int x, int y, int width, int height, double angle) {
        if (sprite == null) return;
        
        AffineTransform old = g2.getTransform();
        AffineTransform at = new AffineTransform();
        at.translate(x + width / 2, y + height / 2);
        at.rotate(angle);
        at.translate(-width / 2, -height / 2);
        g2.setTransform(at);
        g2.drawImage(sprite, 0, 0, width, height, null);
        g2.setTransform(old);
    }
    
    /**
     * Calculate the angle for a direction vector.
     * 
     * @param dx Direction x component
     * @param dy Direction y component
     * @return Angle in radians
     */
    public static double getAngle(int dx, int dy) {
        return Math.atan2(dy, dx);
    }
}

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * SpriteLoader - Utility for loading and caching sprite images.
 * Loads PNG files from Shooter/assets/ directory with graceful fallback.
 */
public class SpriteLoader {
    private static final String ASSETS_DIR = "assets/";
    private static final ConcurrentHashMap<String, BufferedImage> cache = new ConcurrentHashMap<>();
    private static final Set<String> missingSprites = new HashSet<>();
    
    /**
     * Get a sprite by key. Returns null if not found (graceful fallback).
     * @param key The sprite filename without extension (e.g., "player", "bullet")
     * @return BufferedImage or null if not found
     */
    public static BufferedImage get(String key) {
        if (key == null) return null;
        
        // Check if we already know this sprite is missing
        if (missingSprites.contains(key)) {
            return null;
        }
        
        // Check cache first
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        
        // Try to load from file
        String filename = ASSETS_DIR + key + ".png";
        File file = new File(filename);
        
        if (!file.exists()) {
            // Graceful fallback: remember this sprite is missing
            missingSprites.add(key);
            return null;
        }
        
        try {
            BufferedImage img = ImageIO.read(file);
            cache.put(key, img);
            return img;
        } catch (IOException e) {
            // Graceful fallback
            missingSprites.add(key);
            return null;
        }
    }
    
    /**
     * Get an animated frame by base key and frame index.
     * E.g., getFrame("player", 0) loads "player_0.png"
     * @param baseKey The base sprite name
     * @param frameIndex The frame number
     * @return BufferedImage or null if not found
     */
    public static BufferedImage getFrame(String baseKey, int frameIndex) {
        if (baseKey == null) return null;
        return get(baseKey + "_" + frameIndex);
    }
    
    /**
     * Draw a sprite scaled and centered at the given position.
     * @param g2 Graphics2D context
     * @param img Image to draw (can be null)
     * @param cx Center X coordinate
     * @param cy Center Y coordinate
     * @param w Width to draw
     * @param h Height to draw
     */
    public static void drawScaledCentered(Graphics2D g2, Image img, int cx, int cy, int w, int h) {
        if (g2 == null || img == null) return;
        
        int x = cx - w / 2;
        int y = cy - h / 2;
        g2.drawImage(img, x, y, w, h, null);
    }
    
    /**
     * Clear the sprite cache (useful for testing or reloading assets).
     */
    public static void clearCache() {
        cache.clear();
        missingSprites.clear();
    }
}

import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

/**
 * SpriteLoader manages loading and caching of sprite images for the game.
 * It provides graceful fallbacks when images are missing.
 */
public class SpriteLoader {
    private static final ConcurrentHashMap<String, BufferedImage> cache = new ConcurrentHashMap<>();
    private static final String ASSET_PATH = "assets/";
    
    /**
     * Get a sprite by key. Returns null if the sprite is not found.
     * The sprite is cached after first load.
     * 
     * @param key The sprite key (e.g., "player", "enemy_basic")
     * @return The BufferedImage or null if not found
     */
    public static BufferedImage get(String key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        
        BufferedImage image = loadImage(key + ".png");
        if (image != null) {
            cache.put(key, image);
        }
        return image;
    }
    
    /**
     * Get a scaled version of a sprite. The scaled version is cached separately.
     * 
     * @param key The sprite key
     * @param width The target width
     * @param height The target height
     * @return The scaled BufferedImage or null if original not found
     */
    public static BufferedImage getScaled(String key, int width, int height) {
        String cacheKey = key + "_" + width + "x" + height;
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        
        BufferedImage original = get(key);
        if (original == null) {
            return null;
        }
        
        Image scaled = original.getScaledInstance(width, height, Image.SCALE_FAST);
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        scaledImage.getGraphics().drawImage(scaled, 0, 0, null);
        
        cache.put(cacheKey, scaledImage);
        return scaledImage;
    }
    
    /**
     * Load an image from the assets directory or classpath.
     * 
     * @param filename The filename (e.g., "player.png")
     * @return The BufferedImage or null if not found
     */
    private static BufferedImage loadImage(String filename) {
        try {
            // Try loading from file system first (for development)
            File file = new File(ASSET_PATH + filename);
            if (file.exists()) {
                return ImageIO.read(file);
            }
            
            // Try loading from classpath (for packaged JAR)
            InputStream is = SpriteLoader.class.getResourceAsStream("/" + ASSET_PATH + filename);
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                is.close();
                return img;
            }
        } catch (IOException e) {
            // Silently fail - this is expected for missing assets
        }
        return null;
    }
    
    /**
     * Clear the cache (useful for testing or if assets are updated)
     */
    public static void clearCache() {
        cache.clear();
    }
}

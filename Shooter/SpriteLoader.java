import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Utility class for loading and caching sprite images.
 * Provides a centralized sprite management system with automatic fallback.
 */
public class SpriteLoader {
    private static final Map<String, BufferedImage> spriteCache = new HashMap<>();
    private static final String SPRITE_PATH = "/sprites/";
    
    /**
     * Attempts to load a sprite by name from the sprites directory.
     * Returns null if the sprite cannot be found, allowing for fallback rendering.
     * 
     * @param spriteName The name of the sprite file (without extension)
     * @return BufferedImage if found, null otherwise
     */
    public static BufferedImage get(String spriteName) {
        // Check cache first
        if (spriteCache.containsKey(spriteName)) {
            return spriteCache.get(spriteName);
        }
        
        // Try to load the sprite
        try {
            String path = SPRITE_PATH + spriteName + ".png";
            BufferedImage image = ImageIO.read(SpriteLoader.class.getResourceAsStream(path));
            
            if (image != null) {
                spriteCache.put(spriteName, image);
                return image;
            }
        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            // Sprite not found or couldn't be loaded - this is expected behavior
            // We'll cache null so we don't keep trying to load missing sprites
            spriteCache.put(spriteName, null);
        }
        
        return null;
    }
    
    /**
     * Clears the sprite cache. Useful for reloading sprites during development.
     */
    public static void clearCache() {
        spriteCache.clear();
    }
}

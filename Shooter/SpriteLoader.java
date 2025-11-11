import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * SpriteLoader manages loading and caching of sprite images for the game.
 * It provides graceful fallbacks when images are missing.
 * Now supports loading from sprite sheet atlas with configurable mappings.
 */
public class SpriteLoader {
    private static final ConcurrentHashMap<String, BufferedImage> cache = new ConcurrentHashMap<>();
    private static final String ASSET_PATH = "assets/";
    
    // Atlas configuration: key -> AtlasRegion
    private static final Map<String, AtlasRegion> atlasMap = new HashMap<>();
    private static boolean atlasInitialized = false;
    
    /**
     * Represents a region in a sprite sheet
     */
    private static class AtlasRegion {
        String sheetName;
        int col;
        int row;
        int width;
        int height;
        
        AtlasRegion(String sheetName, int col, int row, int width, int height) {
            this.sheetName = sheetName;
            this.col = col;
            this.row = row;
            this.width = width;
            this.height = height;
        }
        
        AtlasRegion(String sheetName, int col, int row) {
            this(sheetName, col, row, 48, 48);
        }
    }
    
    /**
     * Initialize atlas mappings (lazy initialization)
     */
    private static void initializeAtlas() {
        if (atlasInitialized) return;
        atlasInitialized = true;
        
        // Use default mappings (atlas.json support can be added later if needed)
        setupDefaultAtlasMappings();
    }
    
    /**
     * Setup default atlas mappings for sprites
     */
    private static void setupDefaultAtlasMappings() {
        // Player frames (Rogues.png, first row)
        atlasMap.put("player_0", new AtlasRegion("rogues", 0, 0));
        atlasMap.put("player_1", new AtlasRegion("rogues", 1, 0));
        atlasMap.put("player_2", new AtlasRegion("rogues", 2, 0));
        atlasMap.put("player_3", new AtlasRegion("rogues", 3, 0));
        atlasMap.put("player", new AtlasRegion("rogues", 0, 0)); // alias
        
        // Projectiles & melee (Items.png)
        atlasMap.put("bullet", new AtlasRegion("items", 0, 0));
        atlasMap.put("shotgun_pellet", new AtlasRegion("items", 1, 0));
        atlasMap.put("laser", new AtlasRegion("items", 2, 0));
        atlasMap.put("rocket", new AtlasRegion("items", 3, 0));
        atlasMap.put("melee_arc", new AtlasRegion("items", 0, 1, 64, 64)); // 2x2 cells
        atlasMap.put("spear_thrust", new AtlasRegion("items", 2, 1, 96, 48)); // 2x1 cells
        
        // Enemies (Monsters.png) - 8 columns, row-major layout
        // Row 0
        atlasMap.put("enemy_basic", new AtlasRegion("monsters", 0, 0));
        atlasMap.put("enemy_fast", new AtlasRegion("monsters", 1, 0));
        atlasMap.put("enemy_tank", new AtlasRegion("monsters", 2, 0));
        atlasMap.put("enemy_jumper", new AtlasRegion("monsters", 3, 0));
        atlasMap.put("enemy_assassin", new AtlasRegion("monsters", 4, 0));
        atlasMap.put("enemy_engineer", new AtlasRegion("monsters", 5, 0));
        atlasMap.put("enemy_turret", new AtlasRegion("monsters", 6, 0));
        atlasMap.put("enemy_miniboss", new AtlasRegion("monsters", 7, 0));
        
        // Row 1 (continuing row-major)
        atlasMap.put("enemy_healer", new AtlasRegion("monsters", 0, 1));
        atlasMap.put("enemy_chemist", new AtlasRegion("monsters", 1, 1));
        atlasMap.put("enemy_charger", new AtlasRegion("monsters", 2, 1));
        atlasMap.put("enemy_shooter", new AtlasRegion("monsters", 3, 1));
        atlasMap.put("enemy_boss", new AtlasRegion("monsters", 4, 1));
        
        // Animated enemies (2-frame animations)
        // Jumper animation (assuming frames at cols 3-4, row 0)
        atlasMap.put("enemy_jumper_0", new AtlasRegion("monsters", 3, 0));
        atlasMap.put("enemy_jumper_1", new AtlasRegion("monsters", 3, 0)); // Default to same if no separate frame
        
        // Assassin animation (assuming frames at cols 4-5, row 0)
        atlasMap.put("enemy_assassin_0", new AtlasRegion("monsters", 4, 0));
        atlasMap.put("enemy_assassin_1", new AtlasRegion("monsters", 4, 0)); // Default to same if no separate frame
    }
    
    /**
     * Get a sprite by key. Returns null if the sprite is not found.
     * The sprite is cached after first load.
     * Checks atlas first, then falls back to individual PNG files.
     * 
     * @param key The sprite key (e.g., "player", "enemy_basic")
     * @return The BufferedImage or null if not found
     */
    public static BufferedImage get(String key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        
        // Initialize atlas if not already done
        initializeAtlas();
        
        // Try to load from atlas first
        if (atlasMap.containsKey(key)) {
            BufferedImage image = loadFromAtlas(key);
            if (image != null) {
                cache.put(key, image);
                return image;
            }
        }
        
        // Fallback to loading individual PNG file
        BufferedImage image = loadImage(key + ".png");
        if (image != null) {
            cache.put(key, image);
        }
        return image;
    }
    
    /**
     * Load sprite from atlas using the mapping
     */
    private static BufferedImage loadFromAtlas(String key) {
        AtlasRegion region = atlasMap.get(key);
        if (region == null) return null;
        
        return SheetAtlas.getFrame(region.sheetName, region.col, region.row, 
                                   region.width, region.height);
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
     * Get a specific animation frame by base key and frame index.
     * E.g., getFrame("player", 0) returns "player_0"
     * 
     * @param baseKey The base sprite key (e.g., "player")
     * @param frameIndex The frame index (e.g., 0, 1, 2, 3)
     * @return The BufferedImage for that frame or null if not found
     */
    public static BufferedImage getFrame(String baseKey, int frameIndex) {
        return get(baseKey + "_" + frameIndex);
    }
    
    /**
     * Draw a sprite scaled and centered at the given position.
     * 
     * @param g2 Graphics2D context
     * @param key Sprite key
     * @param x Center x position
     * @param y Center y position
     * @param width Width to draw
     * @param height Height to draw
     */
    public static void drawScaledCentered(Graphics2D g2, String key, int x, int y, int width, int height) {
        BufferedImage sprite = get(key);
        if (sprite != null) {
            int drawX = x - width / 2;
            int drawY = y - height / 2;
            g2.drawImage(sprite, drawX, drawY, width, height, null);
        }
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

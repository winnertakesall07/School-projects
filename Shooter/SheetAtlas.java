import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * SheetAtlas loads PNG sprite sheets and slices them into grid cells on demand.
 * Default assumptions: tileSize = 48px, no margin, no spacing.
 */
public class SheetAtlas {
    private static final int DEFAULT_TILE_SIZE = 48;
    private static final String ASSET_PATH = "assets/";
    
    // Cache loaded sheets: sheetName -> BufferedImage
    private static final Map<String, BufferedImage> sheets = new HashMap<>();
    
    // Cache sliced frames: cacheKey -> BufferedImage
    private static final Map<String, BufferedImage> frameCache = new HashMap<>();
    
    /**
     * Get a frame from a sprite sheet with custom dimensions.
     * 
     * @param sheetName Name of the sheet (e.g., "Rogues", "Monsters", "Items")
     * @param col Column index (0-based)
     * @param row Row index (0-based)
     * @param width Width in pixels
     * @param height Height in pixels
     * @return The extracted frame or null if not found
     */
    public static BufferedImage getFrame(String sheetName, int col, int row, int width, int height) {
        // Create cache key
        String cacheKey = sheetName + "_" + col + "_" + row + "_" + width + "x" + height;
        
        // Check cache first
        if (frameCache.containsKey(cacheKey)) {
            return frameCache.get(cacheKey);
        }
        
        // Load sheet if not already loaded
        BufferedImage sheet = loadSheet(sheetName);
        if (sheet == null) {
            return null;
        }
        
        // Extract the frame
        try {
            int x = col * DEFAULT_TILE_SIZE;
            int y = row * DEFAULT_TILE_SIZE;
            
            // Validate bounds
            if (x + width > sheet.getWidth() || y + height > sheet.getHeight()) {
                return null;
            }
            
            BufferedImage frame = sheet.getSubimage(x, y, width, height);
            frameCache.put(cacheKey, frame);
            return frame;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get a cell from a sprite sheet using default 48x48 size.
     * 
     * @param sheetName Name of the sheet (e.g., "Rogues", "Monsters", "Items")
     * @param col Column index (0-based)
     * @param row Row index (0-based)
     * @return The extracted cell or null if not found
     */
    public static BufferedImage getCell(String sheetName, int col, int row) {
        return getFrame(sheetName, col, row, DEFAULT_TILE_SIZE, DEFAULT_TILE_SIZE);
    }
    
    /**
     * Load a sprite sheet from file system.
     * Tries both Shooter/assets/<file>.png and repo root <file>.png.
     * 
     * @param sheetName Name of the sheet without extension (e.g., "Rogues")
     * @return The loaded BufferedImage or null if not found
     */
    private static BufferedImage loadSheet(String sheetName) {
        // Check cache first
        if (sheets.containsKey(sheetName)) {
            return sheets.get(sheetName);
        }
        
        // Try different file name variations (case-insensitive)
        String[] namesToTry = {
            sheetName + ".png",
            sheetName.toLowerCase() + ".png",
            sheetName.toUpperCase() + ".png"
        };
        
        BufferedImage image = null;
        
        // Try loading from assets directory
        for (String filename : namesToTry) {
            try {
                File file = new File(ASSET_PATH + filename);
                if (file.exists()) {
                    image = ImageIO.read(file);
                    break;
                }
            } catch (IOException e) {
                // Try next option
            }
        }
        
        // Try loading from parent directory (repo root)
        if (image == null) {
            for (String filename : namesToTry) {
                try {
                    File file = new File("../" + filename);
                    if (file.exists()) {
                        image = ImageIO.read(file);
                        break;
                    }
                } catch (IOException e) {
                    // Try next option
                }
            }
        }
        
        // Try loading from classpath
        if (image == null) {
            for (String filename : namesToTry) {
                try {
                    InputStream is = SheetAtlas.class.getResourceAsStream("/" + filename);
                    if (is != null) {
                        image = ImageIO.read(is);
                        is.close();
                        break;
                    }
                } catch (IOException e) {
                    // Try next option
                }
            }
        }
        
        // Cache the result (even if null, to avoid repeated lookups)
        if (image != null) {
            sheets.put(sheetName, image);
        }
        
        return image;
    }
    
    /**
     * Clear all caches (useful for testing)
     */
    public static void clearCache() {
        sheets.clear();
        frameCache.clear();
    }
}

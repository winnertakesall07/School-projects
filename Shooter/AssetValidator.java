import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * AssetValidator checks if required assets are available.
 * It first checks the sprite sheet atlas, then falls back to individual PNG files.
 * Warnings are only issued once per missing asset.
 */
public class AssetValidator {
    private static final Set<String> warnedKeys = new HashSet<>();
    private static final String ASSET_PATH = "assets/";
    
    /**
     * Verify that an asset exists either in the atlas or as a PNG file.
     * 
     * @param key The asset key to check
     * @return true if the asset exists, false otherwise
     */
    public static boolean verify(String key) {
        // Try to load via SpriteLoader (which checks atlas first)
        if (SpriteLoader.get(key) != null) {
            return true;
        }
        
        // Not found - warn if we haven't already
        if (!warnedKeys.contains(key)) {
            warnedKeys.add(key);
            System.err.println("Warning: Asset not found: " + key);
        }
        
        return false;
    }
    
    /**
     * Verify multiple assets at once.
     * 
     * @param keys Array of asset keys to check
     * @return true if all assets exist, false otherwise
     */
    public static boolean verifyAll(String... keys) {
        boolean allFound = true;
        for (String key : keys) {
            if (!verify(key)) {
                allFound = false;
            }
        }
        return allFound;
    }
    
    /**
     * Clear the warning history (useful for testing)
     */
    public static void clearWarnings() {
        warnedKeys.clear();
    }
}

/**
 * World-space camera that keeps the player centred in the viewport
 * and clamps to world boundaries.
 */
public class Camera {

    public static final int WORLD_WIDTH  = 2400;
    public static final int WORLD_HEIGHT = 1800;

    /** Top-left corner of the viewport in world coordinates. */
    public float x, y;

    /**
     * Update the camera to centre on the given world-space position.
     *
     * @param playerCX  player centre X in world coordinates
     * @param playerCY  player centre Y in world coordinates
     * @param screenW   viewport width  in pixels
     * @param screenH   viewport height in pixels
     */
    public void update(float playerCX, float playerCY, int screenW, int screenH) {
        x = playerCX - screenW / 2f;
        y = playerCY - screenH / 2f;
        // Clamp so we never show outside the world
        x = Math.max(0, Math.min(WORLD_WIDTH  - screenW, x));
        y = Math.max(0, Math.min(WORLD_HEIGHT - screenH, y));
    }

    /** Convert a world X coordinate to a screen X coordinate. */
    public int toScreenX(float worldX) { return Math.round(worldX - x); }

    /** Convert a world Y coordinate to a screen Y coordinate. */
    public int toScreenY(float worldY) { return Math.round(worldY - y); }

    /** Convert a screen X coordinate to a world X coordinate. */
    public float toWorldX(int screenX) { return screenX + x; }

    /** Convert a screen Y coordinate to a world Y coordinate. */
    public float toWorldY(int screenY) { return screenY + y; }
}

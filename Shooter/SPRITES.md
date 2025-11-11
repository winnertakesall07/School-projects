# Sprite Assets Documentation

## Asset Directory Structure

All sprite assets should be placed in the `Shooter/assets/` directory as PNG files.

## Naming Conventions

### Player Sprites
- `player.png` - Static player sprite (fallback)
- `player_0.png` - Player animation frame 0
- `player_1.png` - Player animation frame 1

The player animation toggles between frames 0 and 1 every 10 ticks while moving.

### Enemy Sprites
- `enemy_basic.png` - Basic enemy sprite
- `enemy_fast.png` - Fast enemy sprite
- `enemy_tank.png` - Tank enemy sprite
- `enemy_jumper.png` - Jumper enemy sprite
- `enemy_assassin.png` - Assassin enemy sprite
- `enemy_engineer.png` - Engineer enemy sprite
- `enemy_turret.png` - Turret enemy sprite
- `enemy_miniboss.png` - Mini-boss enemy sprite
- `enemy_healer.png` - Healer enemy sprite
- `enemy_chemist.png` - Chemist enemy sprite
- `enemy_charger.png` - Charger enemy sprite

### Projectile Sprites
- `bullet.png` - Standard bullet (used by Pistol, MachineGun, Sniper)
- `shotgun_pellet.png` - Shotgun pellet
- `laser.png` - Laser beam projectile
- `rocket.png` - Rocket projectile

### Melee Weapon Sprites
- `melee_arc.png` - Melee arc overlay (used by Sword and Axe)
- `spear_thrust.png` - Spear thrust overlay (used by Spear)

## Sprite Specifications

### Recommended Dimensions
- Player sprites: 32x32 pixels
- Enemy sprites: 32-48 pixels (depending on enemy type)
- Projectile sprites: 6-12 pixels
- Melee overlays: 48x48 pixels

### Format
- File format: PNG with transparency (RGBA)
- Pixel art style recommended
- Transparent backgrounds

## Graceful Fallback

The sprite system is designed to gracefully handle missing assets:
- If a sprite file is not found, the game will fall back to the original rectangle/shape rendering
- No runtime exceptions will occur if assets are missing
- The SpriteLoader caches loaded sprites for performance

## Usage Example

To add new sprites:
1. Create your PNG file following the naming convention
2. Place it in the `Shooter/assets/` directory
3. The game will automatically load and use it on next run

To test without sprites:
- Simply remove or rename sprite files
- The game will fall back to rectangle rendering

## Implementation Details

The `SpriteLoader` class provides:
- `get(key)` - Load a sprite by key (without .png extension)
- `getFrame(baseKey, frameIndex)` - Load an animated frame (e.g., "player", 0)
- `drawScaledCentered(g2, img, cx, cy, w, h)` - Draw a sprite centered at a position

Sprites are cached in a `ConcurrentHashMap` for thread-safe access and performance.

## Testing

The sprite system has been thoroughly tested:
- All sprites load correctly from the assets directory
- Missing sprites are handled gracefully without exceptions
- Cache performance is optimized with ConcurrentHashMap
- No runtime errors occur when assets are missing

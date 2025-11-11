# Enemy Sprites Directory

This directory contains sprite images for enemy entities in the game.

## File Naming Convention

Sprite files should be named according to the following pattern:
- `enemy_<type>.png` where `<type>` is the enemy class name in lowercase

## Required Sprite Files

The following sprite files are expected by the SpriteLoader system:

1. `enemy_basic.png` - Basic enemy sprite
2. `enemy_fast.png` - Fast enemy sprite
3. `enemy_tank.png` - Tank enemy sprite
4. `enemy_jumper.png` - Jumper enemy sprite
5. `enemy_assassin.png` - Assassin enemy sprite
6. `enemy_engineer.png` - Engineer enemy sprite
7. `enemy_turret.png` - Turret enemy sprite
8. `enemy_miniboss.png` - Mini Boss enemy sprite
9. `enemy_healer.png` - Healer enemy sprite
10. `enemy_chemist.png` - Chemist enemy sprite
11. `enemy_charger.png` - Charger enemy sprite
12. `enemy_shooter.png` - Shooter enemy sprite
13. `enemy_boss.png` - Boss enemy sprite

## Sprite Requirements

- **Format**: PNG with transparency support
- **Size**: Images will be scaled to fit the tile size (48x48 pixels by default)
- **Recommendation**: Create sprites at 48x48 pixels or larger for best quality

## Fallback Behavior

If a sprite file is not found, the game will automatically fall back to the original colored rectangle rendering for that enemy type. This allows for gradual sprite implementation without breaking existing functionality.

## Implementation Notes

The SpriteLoader class:
- Caches loaded sprites for performance
- Returns `null` for missing sprites, triggering fallback rendering
- Expects sprites in the `/sprites/` resource directory
- Supports PNG format images

To add a new sprite:
1. Create a PNG image with the appropriate dimensions
2. Name it according to the convention above
3. Place it in this directory
4. The game will automatically use it when the enemy is drawn

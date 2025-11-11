import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyH;

    public int level;
    public int xp;
    public int nextLevelXp;
    public Weapon currentWeapon;
    
    // Animation
    private int animationFrame = 0;
    private int animationCounter = 0;

    // Simple cooldown to rate-limit contact damage
    private int contactDamageCooldown = 0;
    
    // Player status effect timers
    private int poisonTimer = 0;
    private int poisonTick = 0;
    private int slowTimer = 0;
    private int freezeTimer = 0;
    private int confusionTimer = 0;

    // Inventory data
    public List<Weapon> unlockedWeapons = new ArrayList<>();
    public List<String> acquiredUpgrades = new ArrayList<>();

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        solidArea = new Rectangle(8, 8, 32, 32);
        setDefaultValues();
    }

    public void setDefaultValues() {
        x = gp.screenWidth / 2 - gp.tileSize / 2;
        y = gp.screenHeight / 2 - gp.tileSize / 2;
        speed = 4;
        maxHp = 100;
        hp = maxHp;
        level = 1;
        xp = 0;
        nextLevelXp = 10;

        // Reset inventory and add default pistol
        unlockedWeapons.clear();
        acquiredUpgrades.clear();
        addWeaponIfNew(new Pistol(gp));
        currentWeapon = unlockedWeapons.get(0);

        contactDamageCooldown = 0;
        poisonTimer = 0;
        poisonTick = 0;
        slowTimer = 0;
        freezeTimer = 0;
        confusionTimer = 0;
        alive = true;
    }

    public void addWeaponIfNew(Weapon w) {
        for (Weapon uw : unlockedWeapons) {
            if (uw.getClass() == w.getClass()) return;
        }
        unlockedWeapons.add(w);
    }

    public void equipWeaponIndex(int idx) {
        if (idx >= 0 && idx < unlockedWeapons.size()) {
            currentWeapon = unlockedWeapons.get(idx);
        }
    }

    public void gainXP(int gainedXp) {
        xp += gainedXp;
        if (xp >= nextLevelXp) {
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        xp = xp - nextLevelXp;
        nextLevelXp = (int)(nextLevelXp * 1.5);
        maxHp += 10;
        hp = maxHp; // Full heal on level up
        gp.upgradeSystem.generateUpgrades();
        gp.gameState = GamePanel.GameState.LEVEL_UP;
    }

    @Override
    public void update() {
        // Update status effects
        updateStatusEffects();
        
        // Don't move if frozen
        if (freezeTimer > 0) {
            // Still allow weapon to fire
            currentWeapon.update();
            return;
        }
        
        // Calculate effective speed
        int effectiveSpeed = speed;
        if (slowTimer > 0) {
            effectiveSpeed = speed / 2;
        }
        
        // Check if moving
        boolean isMoving = keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed;
        
        // Movement
        if (keyH.upPressed) y -= effectiveSpeed;
        if (keyH.downPressed) y += effectiveSpeed;
        if (keyH.leftPressed) x -= effectiveSpeed;
        if (keyH.rightPressed) x += effectiveSpeed;

        // Keep player within bounds
        x = Math.max(0, Math.min(x, gp.screenWidth - gp.tileSize));
        y = Math.max(0, Math.min(y, gp.screenHeight - gp.tileSize));

        // Update animation
        if (isMoving) {
            animationCounter++;
            if (animationCounter >= 10) {
                animationFrame = (animationFrame + 1) % 2;
                animationCounter = 0;
            }
        } else {
            animationFrame = 0;
            animationCounter = 0;
        }

        // Shooting or swinging
        currentWeapon.update();

        // Tick down contact damage cooldown
        if (contactDamageCooldown > 0) {
            contactDamageCooldown--;
        }
    }

    // Called by CollisionChecker when touching enemies
    public void takeContactDamage(int damage) {
        if (contactDamageCooldown == 0) {
            takeDamage(damage);
            contactDamageCooldown = 20; // ~0.33s at 60 FPS
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Try to load sprite with animation frame
        BufferedImage sprite = SpriteLoader.get("player_" + animationFrame);
        if (sprite == null) {
            // Fallback to single player sprite
            sprite = SpriteLoader.get("player");
        }
        
        if (sprite != null) {
            // Draw sprite
            g2.drawImage(sprite, x, y, gp.tileSize, gp.tileSize, null);
        } else {
            // Fallback to rectangle if sprite not found
            g2.setColor(Color.WHITE);
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
        }
    }
    
    @Override
    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            alive = false;
            gp.gameState = GamePanel.GameState.GAME_OVER;
        }
    }
    
    private void updateStatusEffects() {
        // Poison: periodic damage
        if (poisonTimer > 0) {
            poisonTimer--;
            poisonTick++;
            if (poisonTick >= 60) { // Every 1 second
                takeDamage(1);
                poisonTick = 0;
            }
        }
        
        // Freeze: countdown
        if (freezeTimer > 0) freezeTimer--;
        
        // Slow: countdown
        if (slowTimer > 0) slowTimer--;
        
        // Confusion: countdown (affects movement jitter if we implement it)
        if (confusionTimer > 0) confusionTimer--;
    }
    
    public void applyStatusEffect(StatusEffect effect, int duration) {
        switch (effect) {
            case POISON:
                poisonTimer = duration;
                poisonTick = 0;
                break;
            case FREEZE:
                freezeTimer = Math.min(duration, 120); // Max 2 seconds
                break;
            case SLOW:
                slowTimer = duration;
                break;
            case CONFUSION:
                confusionTimer = duration;
                break;
            case FIRE:
            case WEAKNESS:
            case NONE:
            default:
                // Fire and weakness don't affect player
                break;
        }
    }
}
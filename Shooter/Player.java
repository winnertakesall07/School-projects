import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyH;

    public int level;
    public int xp;
    public int nextLevelXp;
    public Weapon currentWeapon;

    // Simple cooldown to rate-limit contact damage
    private int contactDamageCooldown = 0;

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
        currentWeapon = new Pistol(gp);
        contactDamageCooldown = 0;
        alive = true;
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
        // Movement
        if (keyH.upPressed) y -= speed;
        if (keyH.downPressed) y += speed;
        if (keyH.leftPressed) x -= speed;
        if (keyH.rightPressed) x += speed;

        // Keep player within bounds
        x = Math.max(0, Math.min(x, gp.screenWidth - gp.tileSize));
        y = Math.max(0, Math.min(y, gp.screenHeight - gp.tileSize));

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
        g2.setColor(Color.WHITE);
        g2.fillRect(x, y, gp.tileSize, gp.tileSize);
    }
    
    @Override
    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            alive = false;
            gp.gameState = GamePanel.GameState.GAME_OVER;
        }
    }
}
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Jumper extends Enemy {
    private int jumpCooldown = 0;
    private int jumpDx = 0;
    private int jumpDy = 0;
    private int jumpDuration = 0;

    public Jumper(GamePanel gp, int x, int y) {
        super(gp);
        this.x = x;
        this.y = y;
        speed = 2;
        maxHp = 25;
        hp = maxHp;
        xpValue = 7;
        jumpCooldown = 90; // Initial jump after 1.5 seconds
        hasAnimation = true; // Enable animation
    }

    @Override
    public void update() {
        // Update status effects
        updateStatusEffects();
        
        // Handle jumping behavior
        if (jumpDuration > 0) {
            // Currently jumping
            x += jumpDx;
            y += jumpDy;
            jumpDuration--;
            // Animate during jump
            animationCounter++;
            if (animationCounter >= 5) {
                animationFrame = (animationFrame + 1) % 2;
                animationCounter = 0;
            }
        } else if (jumpCooldown > 0) {
            // Waiting to jump
            jumpCooldown--;
        } else {
            // Start a new jump toward player
            int dx = gp.player.x - x;
            int dy = gp.player.y - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                jumpDx = (int) (dx / dist * 10);
                jumpDy = (int) (dy / dist * 10);
                jumpDuration = 15; // Jump lasts 15 frames
                jumpCooldown = 60; // Jump every 1 second
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Try to load animated sprite
        BufferedImage sprite = SpriteLoader.get("enemy_jumper_" + animationFrame);
        if (sprite == null) {
            sprite = SpriteLoader.get("enemy_jumper");
        }
        
        if (sprite != null) {
            g2.drawImage(sprite, x, y, gp.tileSize, gp.tileSize, null);
        } else {
            // Fallback to rectangle - orange
            g2.setColor(new Color(255, 165, 0));
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
        }
    }
}

import java.awt.Color;
import java.awt.Graphics2D;

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
        // Jumper is orange
        g2.setColor(new Color(255, 165, 0));
        g2.fillRect(x, y, gp.tileSize, gp.tileSize);
    }
}

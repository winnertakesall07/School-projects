import java.awt.Rectangle;

public abstract class Enemy extends Entity {
    protected GamePanel gp;
    public int xpValue;

    public Enemy(GamePanel gp) {
        this.gp = gp;
        solidArea = new Rectangle(4, 4, 40, 40);
    }
    
    // Simple AI: Move towards the player
    @Override
    public void update() {
        if (playerIsToTheLeft()) {
            x -= speed;
        }
        if (playerIsToTheRight()) {
            x += speed;
        }
        if (playerIsAbove()) {
            y -= speed;
        }
        if (playerIsBelow()) {
            y += speed;
        }
    }

    private boolean playerIsToTheLeft() {
        return gp.player.x < this.x;
    }

    private boolean playerIsToTheRight() {
        return gp.player.x > this.x;
    }

    private boolean playerIsAbove() {
        return gp.player.y < this.y;
    }

    private boolean playerIsBelow() {
        return gp.player.y > this.y;
    }
}
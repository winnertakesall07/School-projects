import java.awt.Rectangle;
import java.util.Random;

public abstract class Enemy extends Entity {
    protected GamePanel gp;
    public int xpValue;
    
    // Status effect timers
    private int fireTimer = 0;
    private int fireTick = 0;
    private int poisonTimer = 0;
    private int poisonTick = 0;
    private int freezeTimer = 0;
    private int slowTimer = 0;
    private int weaknessTimer = 0;
    private int confusionTimer = 0;
    
    private Random random = new Random();

    public Enemy(GamePanel gp) {
        this.gp = gp;
        solidArea = new Rectangle(4, 4, 40, 40);
    }
    
    // Simple AI: Move towards the player
    @Override
    public void update() {
        // Update status effect timers
        updateStatusEffects();
        
        // Don't move if frozen
        if (freezeTimer > 0) {
            return;
        }
        
        // Calculate base speed with slow applied
        int effectiveSpeed = speed;
        if (slowTimer > 0) {
            effectiveSpeed = speed / 2;
        }
        
        // Target position (player, with confusion jitter)
        int targetX = gp.player.x;
        int targetY = gp.player.y;
        if (confusionTimer > 0) {
            targetX += random.nextInt(100) - 50;
            targetY += random.nextInt(100) - 50;
        }
        
        if (targetX < this.x) {
            x -= effectiveSpeed;
        }
        if (targetX > this.x) {
            x += effectiveSpeed;
        }
        if (targetY < this.y) {
            y -= effectiveSpeed;
        }
        if (targetY > this.y) {
            y += effectiveSpeed;
        }
    }    
    private void updateStatusEffects() {
        // Fire: periodic damage
        if (fireTimer > 0) {
            fireTimer--;
            fireTick++;
            if (fireTick >= 30) { // Every 0.5 seconds
                takeDamage(2);
                fireTick = 0;
            }
        }
        
        // Poison: lighter periodic damage over longer time
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
        
        // Weakness: countdown
        if (weaknessTimer > 0) weaknessTimer--;
        
        // Confusion: countdown
        if (confusionTimer > 0) confusionTimer--;
    }
    
    public void applyStatusEffect(StatusEffect effect, int duration) {
        switch (effect) {
            case FIRE:
                fireTimer = duration;
                fireTick = 0;
                break;
            case POISON:
                poisonTimer = duration;
                poisonTick = 0;
                break;
            case FREEZE:
                freezeTimer = Math.min(duration, 120); // Max 2 seconds freeze
                break;
            case SLOW:
                slowTimer = duration;
                break;
            case WEAKNESS:
                weaknessTimer = duration;
                break;
            case CONFUSION:
                confusionTimer = duration;
                break;
            case NONE:
            default:
                break;
        }
    }
    
    public int getContactDamage() {
        int baseDamage = 5;
        if (weaknessTimer > 0) {
            return baseDamage / 2;
        }
        return baseDamage;
    }
}

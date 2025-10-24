import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class ParticleEffect {
    private int x, y;
    private double vx, vy;
    private Color color;
    private int life;
    private int maxLife;
    private int size;
    private static Random random = new Random();
    
    public ParticleEffect(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.maxLife = 30 + random.nextInt(20);
        this.life = maxLife;
        this.size = 3 + random.nextInt(5);
        
        // Random velocity
        double angle = random.nextDouble() * 2 * Math.PI;
        double speed = 2 + random.nextDouble() * 3;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
    }
    
    public void update() {
        x += vx;
        y += vy;
        vy += 0.2; // Gravity
        life--;
    }
    
    public boolean isAlive() {
        return life > 0;
    }
    
    public void draw(Graphics2D g2) {
        float alpha = (float) life / maxLife;
        Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255));
        g2.setColor(c);
        int currentSize = (int)(size * alpha);
        g2.fillOval(x - currentSize/2, y - currentSize/2, currentSize, currentSize);
    }
}
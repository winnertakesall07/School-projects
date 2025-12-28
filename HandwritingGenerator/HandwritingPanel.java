import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * A custom JPanel that renders text with simulated handwriting effects.
 * Each time text is rendered, random variations are applied to make it unique.
 * Uses AffineTransform to apply shear, rotation, position jitter, and scale variations.
 * 
 * @author School Project
 * @version 1.0
 */
public class HandwritingPanel extends JPanel {
    private String text;
    private Random random;
    
    /**
     * Constructor for HandwritingPanel.
     * Initializes the random number generator with a seed based on current time
     * to ensure different results each time.
     */
    public HandwritingPanel() {
        this.text = "";
        this.random = new Random();
    }
    
    /**
     * Sets the text to be displayed and triggers a repaint.
     * Reseeds the random generator to ensure unique output.
     * 
     * @param text The text to display in handwriting style
     */
    public void setText(String text) {
        this.text = text;
        // Reseed random with current time to ensure uniqueness
        this.random = new Random(System.nanoTime());
        repaint();
    }
    
    /**
     * Paints the component with handwritten text.
     * Applies random transformations to each character for a handwritten look.
     * 
     * @param g The Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (text == null || text.isEmpty()) {
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable antialiasing for smoother text
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Use a standard font as base
        Font baseFont = new Font("SansSerif", Font.PLAIN, 36);
        
        // Starting position
        double x = 50;
        double y = 100;
        double lineHeight = 60;
        double maxWidth = getWidth() - 100;
        
        // Draw some random "paper texture" lines for added effect
        drawPaperTexture(g2d);
        
        // Process each character
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // Handle line breaks
            if (c == '\n' || x > maxWidth) {
                x = 50;
                y += lineHeight;
                if (c == '\n') {
                    continue;
                }
            }
            
            // Skip if we're too far down
            if (y > getHeight() - 50) {
                break;
            }
            
            // Save the original transform
            AffineTransform originalTransform = g2d.getTransform();
            
            // Apply random transformations for handwriting effect
            AffineTransform transform = new AffineTransform();
            
            // 1. Random position jitter (slight wobble in position)
            double xJitter = (random.nextDouble() - 0.5) * 6;
            double yJitter = (random.nextDouble() - 0.5) * 8;
            transform.translate(x + xJitter, y + yJitter);
            
            // 2. Random rotation (slight tilt)
            double rotation = (random.nextDouble() - 0.5) * 0.15; // ~8 degrees max
            transform.rotate(rotation);
            
            // 3. Random shear (slant)
            double shearX = (random.nextDouble() - 0.5) * 0.2;
            double shearY = (random.nextDouble() - 0.5) * 0.1;
            transform.shear(shearX, shearY);
            
            // 4. Random scale (slightly different sizes)
            double scale = 0.9 + random.nextDouble() * 0.2; // 0.9 to 1.1
            transform.scale(scale, scale);
            
            g2d.setTransform(transform);
            
            // Apply random font variations
            int fontStyle = random.nextBoolean() ? Font.PLAIN : Font.PLAIN;
            Font charFont = baseFont.deriveFont(fontStyle);
            g2d.setFont(charFont);
            
            // Add slight color variation (darker/lighter gray-black)
            int colorVariation = 200 + random.nextInt(56); // 200-255
            g2d.setColor(new Color(
                Math.max(0, colorVariation - 180 + random.nextInt(20)),
                Math.max(0, colorVariation - 180 + random.nextInt(20)),
                Math.max(0, colorVariation - 180 + random.nextInt(20))
            ));
            
            // Draw the character
            g2d.drawString(String.valueOf(c), 0, 0);
            
            // Restore original transform
            g2d.setTransform(originalTransform);
            
            // Calculate character width for next position
            FontMetrics fm = g2d.getFontMetrics(charFont);
            int charWidth = fm.charWidth(c);
            
            // Add random spacing variation
            double spacingJitter = (random.nextDouble() - 0.5) * 4;
            x += charWidth * scale + spacingJitter;
        }
        
        // Add some random "ink spots" or imperfections
        drawInkSpots(g2d);
    }
    
    /**
     * Draws subtle paper texture lines in the background.
     * 
     * @param g2d The Graphics2D context
     */
    private void drawPaperTexture(Graphics2D g2d) {
        g2d.setColor(new Color(240, 240, 245));
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(getWidth());
            int y1 = random.nextInt(getHeight());
            int x2 = x1 + random.nextInt(100) - 50;
            int y2 = y1 + random.nextInt(100) - 50;
            g2d.setStroke(new BasicStroke(0.5f));
            g2d.drawLine(x1, y1, x2, y2);
        }
    }
    
    /**
     * Draws random small ink spots to simulate pen imperfections.
     * 
     * @param g2d The Graphics2D context
     */
    private void drawInkSpots(Graphics2D g2d) {
        g2d.setColor(new Color(50, 50, 50, 30)); // Semi-transparent dark gray
        for (int i = 0; i < 3; i++) {
            int x = random.nextInt(getWidth());
            int y = random.nextInt(getHeight());
            int size = 1 + random.nextInt(2);
            g2d.fillOval(x, y, size, size);
        }
    }
}

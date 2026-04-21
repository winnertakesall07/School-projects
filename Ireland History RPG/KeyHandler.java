import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Tracks keyboard input for the game.
 * Movement keys (WASD / arrows) are raw held-state booleans.
 * Action keys (Enter, Esc, P) are set true on press and consumed
 * (set false) by GamePanel after being acted upon.
 */
public class KeyHandler implements KeyListener {

    // Movement — true while held
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    // Attack — true while held (cooldown system prevents spam)
    public boolean spaceHeld;

    // One-shot menu keys — consumed by GamePanel after use
    public boolean enterPressed;
    public boolean escPressed;
    public boolean pausePressed;

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP    || code == KeyEvent.VK_W) upPressed    = true;
        if (code == KeyEvent.VK_DOWN  || code == KeyEvent.VK_S) downPressed  = true;
        if (code == KeyEvent.VK_LEFT  || code == KeyEvent.VK_A) leftPressed  = true;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) rightPressed = true;
        if (code == KeyEvent.VK_SPACE)  spaceHeld    = true;
        if (code == KeyEvent.VK_ENTER)  enterPressed = true;
        if (code == KeyEvent.VK_ESCAPE) escPressed   = true;
        if (code == KeyEvent.VK_P)      pausePressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP    || code == KeyEvent.VK_W) upPressed    = false;
        if (code == KeyEvent.VK_DOWN  || code == KeyEvent.VK_S) downPressed  = false;
        if (code == KeyEvent.VK_LEFT  || code == KeyEvent.VK_A) leftPressed  = false;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) rightPressed = false;
        if (code == KeyEvent.VK_SPACE)  spaceHeld    = false;
        if (code == KeyEvent.VK_ENTER)  enterPressed = false;
        if (code == KeyEvent.VK_ESCAPE) escPressed   = false;
        if (code == KeyEvent.VK_P)      pausePressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}

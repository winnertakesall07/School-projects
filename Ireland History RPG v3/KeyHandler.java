import java.awt.event.*;

/**
 * Tracks keyboard and mouse input for the game.
 * Movement keys (WASD / arrows) are raw held-state booleans.
 * Action keys (Enter, Esc, P, M) are set true on press and consumed
 * (set false) by GamePanel after being acted upon.
 * Mouse position drives the aim direction; left-click fires.
 */
public class KeyHandler implements KeyListener, MouseMotionListener, MouseListener {

    // Movement — true while held
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    // Attack — true while held (cooldown system prevents spam)
    public boolean spaceHeld;

    // Mouse aim and fire
    public int     mouseX = 480, mouseY = 360; // default to screen centre
    public boolean mouseFireHeld;              // left mouse button held

    // One-shot menu keys — consumed by GamePanel after use
    public boolean enterPressed;
    public boolean escPressed;
    public boolean pausePressed;
    public boolean mutePressed;  // M key — toggle sound mute

    // ── Keyboard ─────────────────────────────────────────────────────────────
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
        if (code == KeyEvent.VK_M)      mutePressed  = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP    || code == KeyEvent.VK_W) upPressed    = false;
        if (code == KeyEvent.VK_DOWN  || code == KeyEvent.VK_S) downPressed  = false;
        if (code == KeyEvent.VK_LEFT  || code == KeyEvent.VK_A) leftPressed  = false;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) rightPressed = false;
        if (code == KeyEvent.VK_SPACE)  spaceHeld    = false;
        // enterPressed, escPressed, pausePressed, mutePressed are one-shot flags
        // consumed by GamePanel after being acted upon; do not clear on key-release.
    }

    @Override public void keyTyped(KeyEvent e) {}

    // ── Mouse ─────────────────────────────────────────────────────────────────
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) mouseFireHeld = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) mouseFireHeld = false;
    }

    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
}

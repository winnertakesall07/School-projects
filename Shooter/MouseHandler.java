import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseHandler implements MouseListener, MouseMotionListener {
    private final GamePanel gp;

    public int mouseX;
    public int mouseY;

    private boolean leftDown = false;
    private boolean leftClickedEdge = false;

    public MouseHandler(GamePanel gp) {
        this.gp = gp;
    }

    public boolean consumeLeftClick() {
        if (leftClickedEdge) {
            leftClickedEdge = false;
            return true;
        }
        return false;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && !leftDown) {
            leftDown = true;
            leftClickedEdge = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftDown = false;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
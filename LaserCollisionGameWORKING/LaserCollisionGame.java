import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class LaserCollisionGame extends JFrame {
    private enum Tool { INPUT, CONSTANT, BULB, BLOCK, REDIRECTOR, ERASE, TOGGLE_INPUT }
    private enum PartType { INPUT, CONSTANT, BULB, BLOCK, REDIRECTOR }

    private static final int[] DX = { 1, 1, 0, -1, -1, -1, 0, 1 };
    private static final int[] DY = { 0, -1, -1, -1, 0, 1, 1, 1 };
    private static final String[] ARROWS = { "→", "↗", "↑", "↖", "←", "↙", "↓", "↘" };
    private static final int MAX_BEAM_LENGTH = 15;
    private final Set<Point> collisionBlocks = new HashSet<>();

    private final Map<Point, Part> parts = new HashMap<>();
    private final Set<Point> beamCells = new HashSet<>();
    private final Set<Point> litBulbs = new HashSet<>();

    private Tool selectedTool = Tool.INPUT;
    private int selectedDirection = 0;

    private final JLabel statusLabel = new JLabel();
    private final BoardPanel board = new BoardPanel();
    private static boolean segmentsIntersect(
        double x1, double y1,
        double x2, double y2,
        double x3, double y3,
        double x4, double y4) {

    double denominator =
            (x1 - x2) * (y3 - y4)
            - (y1 - y2) * (x3 - x4);

    if (Math.abs(denominator) < 1e-9)
        return false;

    double t =
            ((x1 - x3) * (y3 - y4)
            - (y1 - y3) * (x3 - x4))
            / denominator;

    double u =
            ((x1 - x3) * (y1 - y2)
            - (y1 - y3) * (x1 - x2))
            / denominator;

    return t >= 0 && t <= 1 && u >= 0 && u <= 1;
}

    private static class Part {
        final PartType type;
        int direction;
        boolean enabled;

        Part(PartType type, int direction, boolean enabled) {
            this.type = type;
            this.direction = direction;
            this.enabled = enabled;
        }
    }

    private static class Beam {
    final int sourceX;
    final int sourceY;
    final int direction;

    Beam(int sourceX, int sourceY, int direction) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.direction = direction;
    }
}

    private static class Candidate {
    final Beam from;
    final Beam to;
    final int index;

    Candidate(int index, Beam from, Beam to) {
        this.index = index;
        this.from = from;
        this.to = to;
    }
}

    public LaserCollisionGame() {
        super("Laser Collision Computing Sandbox");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        toolbar.add(makeToolButton("Input Laser", Tool.INPUT));
        toolbar.add(makeToolButton("Constant Laser", Tool.CONSTANT));
        toolbar.add(makeToolButton("Light Bulb", Tool.BULB));
        toolbar.add(makeToolButton("Block", Tool.BLOCK));
        toolbar.add(makeToolButton("Redirector", Tool.REDIRECTOR));
        toolbar.add(makeToolButton("Toggle Input", Tool.TOGGLE_INPUT));
        toolbar.add(makeToolButton("Eraser", Tool.ERASE));

        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> {
            parts.clear();
            recomputeLasers();
            board.repaint();
        });
        toolbar.add(clear);

        add(toolbar, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(statusLabel, BorderLayout.WEST);
        JLabel hint = new JLabel("LMB: place/use tool | RMB drag: pan | Wheel: zoom | R: rotate (45°)");
        bottom.add(hint, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        updateStatus();
        setSize(1200, 760);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LaserCollisionGame().setVisible(true));
    }

    private JButton makeToolButton(String label, Tool tool) {
        JButton button = new JButton(label);
        button.addActionListener(e -> {
            selectedTool = tool;
            updateStatus();
            board.requestFocusInWindow();
        });
        return button;
    }

    private void updateStatus() {
        statusLabel.setText("Tool: " + selectedTool + " | Rotation: " + (selectedDirection * 45) + "° " + ARROWS[selectedDirection]);
    }

    private void placeAt(int cellX, int cellY) {
        Point key = new Point(cellX, cellY);

        if (selectedTool == Tool.ERASE) {
            parts.remove(key);
        } else if (selectedTool == Tool.TOGGLE_INPUT) {
            Part existing = parts.get(key);
            if (existing != null && existing.type == PartType.INPUT) {
                existing.enabled = !existing.enabled;
            }
        } else {
            Part part;
            switch (selectedTool) {
                case INPUT:
                    part = new Part(PartType.INPUT, selectedDirection, true);
                    break;
                case CONSTANT:
                    part = new Part(PartType.CONSTANT, selectedDirection, true);
                    break;
                case REDIRECTOR:
                    part = new Part(PartType.REDIRECTOR, selectedDirection, true);
                    break;
                case BLOCK:
                    part = new Part(PartType.BLOCK, 0, true);
                    break;
                case BULB:
                    part = new Part(PartType.BULB, 0, true);
                    break;
                default:
                    return;
            }
            parts.put(key, part);
        }

        recomputeLasers();
        board.repaint();
    }

  private void recomputeLasers() {

    beamCells.clear();
    litBulbs.clear();

    List<Beam> beams = new ArrayList<>();

    // collect sources
    for (Map.Entry<Point, Part> entry : parts.entrySet()) {

        Part p = entry.getValue();

        if (p.type == PartType.CONSTANT ||
            (p.type == PartType.INPUT && p.enabled)) {

            Point pos = entry.getKey();

            beams.add(new Beam(
                    pos.x,
                    pos.y,
                    p.direction));
        }
    }

    Map<Point, List<Integer>> occupancy = new HashMap<>();
    List<List<Point>> beamPaths = new ArrayList<>();


    // trace every beam
    for (int i = 0; i < beams.size(); i++) {

        Beam beam = beams.get(i);

        List<Point> path = new ArrayList<>();

        int x = beam.sourceX;
        int y = beam.sourceY;
        int dir = beam.direction;

        for (int step = 0; step < MAX_BEAM_LENGTH; step++) {

            x += DX[dir];
            y += DY[dir];

            Point pos = new Point(x, y);

            path.add(pos);

            occupancy
                .computeIfAbsent(pos, k -> new ArrayList<>())
                .add(i);

            Part part = parts.get(pos);

            if (part == null)
                continue;

            switch (part.type) {

                case BLOCK:
                case INPUT:
                case CONSTANT:
                    step = MAX_BEAM_LENGTH;
                    break;

                case REDIRECTOR:
                    dir = part.direction;
                    break;

                case BULB:
                    litBulbs.add(pos);
                    break;
            }
        }

        beamPaths.add(path);
    }
// ----------------------------
// BUILD NEW COLLISION BLOCKS
// ----------------------------
Set<Point> newCollisionBlocks = new HashSet<>();

for (Map.Entry<Point, List<Integer>> e : occupancy.entrySet()) {
    if (e.getValue().size() >= 2) {
        newCollisionBlocks.add(e.getKey());
    }
}

// ----------------------------
// REMOVE OLD GENERATED BLOCKS
// ----------------------------
for (Point p : collisionBlocks) {
    // only remove if it is still a generated block (not user placed)
    Part part = parts.get(p);
    if (part != null && part.type == PartType.BLOCK) {
        // We assume collision blocks are not user-placed
        parts.remove(p);
    }
}

// ----------------------------
// ADD NEW GENERATED BLOCKS
// ----------------------------
for (Point p : newCollisionBlocks) {
    // don't overwrite user blocks
    if (!parts.containsKey(p)) {
        parts.put(p, new Part(PartType.BLOCK, 0, true));
    }
}

// update tracking set
collisionBlocks.clear();
collisionBlocks.addAll(newCollisionBlocks);

    // Find where each beam first dies
Map<Integer, Integer> deathStep = new HashMap<>();

for (List<Integer> list : occupancy.values()) {

    if (list.size() < 2)
        continue;

    // for every beam involved in this collision
    for (int beamIndex : list) {

        List<Point> path = beamPaths.get(beamIndex);

        // determine where along its path this collision happened
        for (int step = 0; step < path.size(); step++) {

            Point p = path.get(step);

            if (occupancy.get(p).size() > 1) {

                Integer existing = deathStep.get(beamIndex);

                if (existing == null || step < existing) {
                    deathStep.put(beamIndex, step);
                }

                break;
            }
        }
    }
}


// draw beams only up to collision
for (int i = 0; i < beamPaths.size(); i++) {

    List<Point> path = beamPaths.get(i);

    Integer stop = deathStep.get(i);

    int limit;

    if (stop == null)
        limit = path.size();
    else
        limit = stop; // collision cell itself disappears

    for (int step = 0; step < limit; step++) {

        beamCells.add(path.get(step));
    }
}
new javax.swing.Timer(1000, e -> {
    recomputeLasers();
    board.repaint();
}).start();
}

    private static long pack(int x, int y) {
        return (((long) x) << 32) | (y & 0xffffffffL);
    }

    private static String stateKey(int x, int y, int d) {
        return x + ":" + y + ":" + d;
    }

    private static String edgeKey(int x1, int y1, int x2, int y2) {
        long a = pack(x1, y1);
        long b = pack(x2, y2);
        if (a <= b) {
            return a + "|" + b;
        }
        return b + "|" + a;
    }

    private class BoardPanel extends JPanel {
        private double cellSize = 40.0;
        private final double minCellSize = 4.0;
        private final double maxCellSize = 180.0;

        private double cameraX = 0.0;
        private double cameraY = 0.0;

        private boolean panning = false;
        private int lastMouseX;
        private int lastMouseY;

        BoardPanel() {
            setBackground(Color.WHITE);
            setFocusable(true);

            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    if (SwingUtilities.isRightMouseButton(e)) {
                        panning = true;
                        lastMouseX = e.getX();
                        lastMouseY = e.getY();
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        Point cell = screenToCell(e.getX(), e.getY());
                        placeAt(cell.x, cell.y);
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (panning) {
                        int dx = e.getX() - lastMouseX;
                        int dy = e.getY() - lastMouseY;
                        cameraX -= dx / cellSize;
                        cameraY -= dy / cellSize;
                        lastMouseX = e.getX();
                        lastMouseY = e.getY();
                        repaint();
                        return;
                    }

                    if (SwingUtilities.isLeftMouseButton(e)) {
                        Point cell = screenToCell(e.getX(), e.getY());
                        placeAt(cell.x, cell.y);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        panning = false;
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    double oldSize = cellSize;
                    double factor = e.getPreciseWheelRotation() < 0 ? 1.15 : 1.0 / 1.15;
                    cellSize = Math.max(minCellSize, Math.min(maxCellSize, cellSize * factor));

                    double worldX = cameraX + (e.getX() - getWidth() / 2.0) / oldSize;
                    double worldY = cameraY + (e.getY() - getHeight() / 2.0) / oldSize;
                    cameraX = worldX - (e.getX() - getWidth() / 2.0) / cellSize;
                    cameraY = worldY - (e.getY() - getHeight() / 2.0) / cellSize;
                    repaint();
                }
            };

            addMouseListener(mouse);
            addMouseMotionListener(mouse);
            addMouseWheelListener(mouse);

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_R) {
                        selectedDirection = (selectedDirection + 1) % 8;
                        updateStatus();
                        repaint();
                    }
                }
            });
        }

        private Point screenToCell(int sx, int sy) {
            double wx = cameraX + (sx - getWidth() / 2.0) / cellSize;
            double wy = cameraY + (sy - getHeight() / 2.0) / cellSize;
            return new Point((int) Math.floor(wx), (int) Math.floor(wy));
        }

        private double cellToScreenX(int x) {
            return (x - cameraX) * cellSize + getWidth() / 2.0;
        }

        private double cellToScreenY(int y) {
            return (y - cameraY) * cellSize + getHeight() / 2.0;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (cellSize >= 16.0) {
                g2.setColor(new Color(240, 240, 240));
                int startX = (int) Math.floor(cameraX - getWidth() / (2.0 * cellSize)) - 1;
                int endX = (int) Math.ceil(cameraX + getWidth() / (2.0 * cellSize)) + 1;
                int startY = (int) Math.floor(cameraY - getHeight() / (2.0 * cellSize)) - 1;
                int endY = (int) Math.ceil(cameraY + getHeight() / (2.0 * cellSize)) + 1;
                for (int x = startX; x <= endX; x++) {
                    int sx = (int) Math.round(cellToScreenX(x));
                    g2.drawLine(sx, 0, sx, getHeight());
                }
                for (int y = startY; y <= endY; y++) {
                    int sy = (int) Math.round(cellToScreenY(y));
                    g2.drawLine(0, sy, getWidth(), sy);
                }
            }

            g2.setColor(new Color(255, 80, 80, 180));
            for (Point p : beamCells) {
                double sx = cellToScreenX(p.x);
                double sy = cellToScreenY(p.y);
                double r = Math.max(2.0, cellSize * 0.28);
                g2.fill(new Ellipse2D.Double(sx + cellSize / 2.0 - r, sy + cellSize / 2.0 - r, r * 2, r * 2));
            }

            for (Map.Entry<Point, Part> entry : parts.entrySet()) {
                Point p = entry.getKey();
                Part part = entry.getValue();

                double sx = cellToScreenX(p.x);
                double sy = cellToScreenY(p.y);
                double pad = Math.max(1.5, cellSize * 0.1);
                Rectangle2D rect = new Rectangle2D.Double(sx + pad, sy + pad, Math.max(2, cellSize - 2 * pad), Math.max(2, cellSize - 2 * pad));

                switch (part.type) {
                    case BLOCK:
                        g2.setColor(new Color(60, 60, 60));
                        g2.fill(rect);
                        break;
                    case INPUT:
                        g2.setColor(part.enabled ? new Color(60, 180, 60) : new Color(180, 180, 180));
                        g2.fill(rect);
                        drawArrowText(g2, sx, sy, part.direction, "I");
                        break;
                    case CONSTANT:
                        g2.setColor(new Color(80, 140, 255));
                        g2.fill(rect);
                        drawArrowText(g2, sx, sy, part.direction, "C");
                        break;
                    case BULB:
                        g2.setColor(litBulbs.contains(p) ? new Color(255, 220, 70) : new Color(235, 235, 235));
                        g2.fill(new Ellipse2D.Double(sx + pad, sy + pad, Math.max(2, cellSize - 2 * pad), Math.max(2, cellSize - 2 * pad)));
                        g2.setColor(new Color(90, 90, 90));
                        g2.draw(new Ellipse2D.Double(sx + pad, sy + pad, Math.max(2, cellSize - 2 * pad), Math.max(2, cellSize - 2 * pad)));
                        break;
                    case REDIRECTOR:
                        g2.setColor(new Color(255, 170, 80));
                        g2.fill(rect);
                        drawArrowText(g2, sx, sy, part.direction, "R");
                        break;
                    default:
                        break;
                }

                g2.setColor(new Color(40, 40, 40));
                g2.draw(rect);
            }

            g2.dispose();
        }

        private void drawArrowText(Graphics2D g2, double sx, double sy, int dir, String centerText) {
            if (cellSize < 12) {
                return;
            }
            int fontSize = (int) Math.max(10, Math.min(22, cellSize * 0.36));
            g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            g2.setColor(Color.BLACK);
            String txt = centerText + ARROWS[dir];
            FontMetrics fm = g2.getFontMetrics();
            int tx = (int) (sx + (cellSize - fm.stringWidth(txt)) / 2.0);
            int ty = (int) (sy + (cellSize + fm.getAscent() - fm.getDescent()) / 2.0);
            g2.drawString(txt, tx, ty);
        }
    }
}

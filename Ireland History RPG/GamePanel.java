import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Core game panel: game loop, state machine, and rendering.
 *
 * Game states:
 *   TITLE    — splash screen
 *   STORY    — narrative pages between chapters
 *   PLAYING  — action battle
 *   PAUSED   — overlay pause
 *   GAME_OVER — player died
 *   VICTORY   — all chapters cleared
 */
public class GamePanel extends JPanel implements Runnable {

    // ── Screen constants ──────────────────────────────────────────────────────
    public static final int TILE_SIZE    = 48;
    public static final int SCREEN_COLS  = 16;
    public static final int SCREEN_ROWS  = 12;
    public static final int SCREEN_WIDTH  = TILE_SIZE * SCREEN_COLS; // 768
    public static final int SCREEN_HEIGHT = TILE_SIZE * SCREEN_ROWS; // 576

    // ── Game state ────────────────────────────────────────────────────────────
    public enum State { TITLE, STORY, PLAYING, PAUSED, GAME_OVER, VICTORY }
    private State state = State.TITLE;

    // ── Core objects ──────────────────────────────────────────────────────────
    private final KeyHandler    keys         = new KeyHandler();
    private final StoryManager  storyManager = new StoryManager();

    // ── Entities ──────────────────────────────────────────────────────────────
    private Player       player;
    private List<Enemy>  enemies = new ArrayList<>();
    private int          currentChapter = 0;
    private static final int TOTAL_CHAPTERS = 3;

    // ── Loop ──────────────────────────────────────────────────────────────────
    private static final int FPS = 60;
    private Thread gameThread;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color BG_TITLE = new Color(18, 38, 18);
    private static final Color BG_STORY = new Color(14, 14, 28);

    // ── Constructor ───────────────────────────────────────────────────────────
    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        addKeyListener(keys);
        setFocusable(true);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // ── Game loop ─────────────────────────────────────────────────────────────
    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (gameThread != null) {
            long now = System.nanoTime();
            delta += (now - lastTime) / drawInterval;
            lastTime = now;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    // ── Update ────────────────────────────────────────────────────────────────
    private void update() {
        switch (state) {
            case TITLE:    updateTitle();    break;
            case STORY:    updateStory();    break;
            case PLAYING:  updatePlaying();  break;
            case PAUSED:   updatePaused();   break;
            case GAME_OVER:
            case VICTORY:
                if (keys.enterPressed) {
                    keys.enterPressed = false;
                    resetGame();
                }
                break;
        }
    }

    private void updateTitle() {
        if (keys.enterPressed) {
            keys.enterPressed = false;
            startChapter(0);
        }
    }

    private void updateStory() {
        if (keys.enterPressed) {
            keys.enterPressed = false;
            if (storyManager.hasMorePages()) {
                storyManager.nextPage();
            } else {
                state = State.PLAYING;
                spawnEnemies(currentChapter);
            }
        }
    }

    private void updatePlaying() {
        // Pause toggle
        if (keys.pausePressed || keys.escPressed) {
            keys.pausePressed = false;
            keys.escPressed   = false;
            state = State.PAUSED;
            return;
        }

        player.update(keys, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Update enemies; remove dead ones
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(player.getCenterX(), player.getCenterY());
            if (e.isDead()) enemies.remove(i);
        }

        // Player attack
        if (keys.spaceHeld && player.canAttack()) {
            player.performAttack();
            for (Enemy e : enemies) {
                if (player.isInAttackRange(e.getCenterX(), e.getCenterY())) {
                    e.takeDamage(player.getAttackDamage());
                }
            }
        }

        // Enemy contact damage
        for (Enemy e : enemies) {
            if (e.isOverlapping(player.getCenterX(), player.getCenterY(), player.getSize())
                    && e.canAttack()) {
                e.performAttack();
                player.takeDamage(e.getAttackDamage());
            }
        }

        // Win / lose
        if (!player.isAlive()) {
            state = State.GAME_OVER;
        } else if (enemies.isEmpty()) {
            currentChapter++;
            if (currentChapter >= TOTAL_CHAPTERS) {
                state = State.VICTORY;
            } else {
                startChapter(currentChapter);
            }
        }
    }

    private void updatePaused() {
        if (keys.pausePressed || keys.escPressed || keys.enterPressed) {
            keys.pausePressed = false;
            keys.escPressed   = false;
            keys.enterPressed = false;
            state = State.PLAYING;
        }
    }

    // ── Chapter management ────────────────────────────────────────────────────
    private void startChapter(int chapter) {
        currentChapter = chapter;
        storyManager.loadChapter(chapter);
        player = new Player(SCREEN_WIDTH / 4, SCREEN_HEIGHT / 2);
        state  = State.STORY;
    }

    private void spawnEnemies(int chapter) {
        enemies.clear();
        int count = 3 + chapter * 2; // 3, 5, 7
        for (int i = 0; i < count; i++) {
            int ex = SCREEN_WIDTH / 2 + 80 + (i % 3) * 110;
            int ey = 90 + (i / 3) * 140;
            enemies.add(new Enemy(ex, ey, chapter));
        }
    }

    private void resetGame() {
        currentChapter = 0;
        enemies.clear();
        player = null;
        state  = State.TITLE;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        switch (state) {
            case TITLE:    drawTitle(g2);                              break;
            case STORY:    drawStory(g2);                              break;
            case PLAYING:  drawPlaying(g2);                            break;
            case PAUSED:   drawPlaying(g2); drawPauseOverlay(g2);      break;
            case GAME_OVER: drawGameOver(g2);                          break;
            case VICTORY:   drawVictory(g2);                           break;
        }
    }

    // Title screen
    private void drawTitle(Graphics2D g) {
        g.setColor(BG_TITLE);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(new Color(180, 150, 60));
        g.setStroke(new BasicStroke(3));
        g.drawRect(20, 20, SCREEN_WIDTH - 40, SCREEN_HEIGHT - 40);

        g.setFont(new Font("Serif", Font.BOLD, 44));
        g.setColor(new Color(220, 200, 100));
        drawCentered(g, "\u00c9ire\u2019s Stand", SCREEN_HEIGHT / 2 - 80);

        g.setFont(new Font("Serif", Font.ITALIC, 23));
        g.setColor(new Color(175, 210, 175));
        drawCentered(g, "Ireland\u2019s Struggle for Freedom", SCREEN_HEIGHT / 2 - 28);

        g.setFont(new Font("Serif", Font.PLAIN, 48));
        g.setColor(new Color(55, 155, 55));
        drawCentered(g, "\u2618 \u2618 \u2618", SCREEN_HEIGHT / 2 + 32);

        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.setColor(new Color(200, 200, 200));
        drawCentered(g, "Press ENTER to begin", SCREEN_HEIGHT / 2 + 100);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(150, 150, 150));
        drawCentered(g, "WASD / Arrows \u2014 Move   |   SPACE \u2014 Attack   |   P / ESC \u2014 Pause", SCREEN_HEIGHT - 40);
    }

    // Story/narrative screen
    private void drawStory(Graphics2D g) {
        StoryPage page = storyManager.getCurrentPage();
        if (page == null) return;

        g.setColor(BG_STORY);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(new Color(100, 80, 40));
        g.setStroke(new BasicStroke(2));
        g.drawRect(15, 15, SCREEN_WIDTH - 30, SCREEN_HEIGHT - 30);

        // Chapter label
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(new Color(160, 140, 80));
        drawCentered(g, page.chapterLabel, 56);

        // Title
        g.setFont(new Font("Serif", Font.BOLD, 30));
        g.setColor(new Color(220, 200, 120));
        drawCentered(g, page.title, 105);

        // Divider
        g.setColor(new Color(100, 80, 40));
        g.setStroke(new BasicStroke(1));
        g.drawLine(80, 122, SCREEN_WIDTH - 80, 122);

        // Body text
        g.setFont(new Font("Serif", Font.PLAIN, 17));
        g.setColor(new Color(210, 210, 210));
        drawWrapped(g, page.text, 60, 155, SCREEN_WIDTH - 120, 25);

        // Vocab note box
        int boxY = SCREEN_HEIGHT - 108;
        g.setColor(new Color(38, 33, 18));
        g.fillRoundRect(40, boxY, SCREEN_WIDTH - 80, 62, 10, 10);
        g.setColor(new Color(180, 150, 60));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(40, boxY, SCREEN_WIDTH - 80, 62, 10, 10);
        g.setFont(new Font("SansSerif", Font.ITALIC, 13));
        g.setColor(new Color(220, 200, 140));
        drawWrapped(g, page.vocabNote, 56, boxY + 22, SCREEN_WIDTH - 112, 18);

        // Prompt
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(160, 160, 180));
        String prompt = storyManager.hasMorePages()
                ? "Press ENTER to continue \u25ba"
                : "Press ENTER to begin the battle \u2694";
        drawCentered(g, prompt, SCREEN_HEIGHT - 20);
    }

    // Battle screen
    private void drawPlaying(Graphics2D g) {
        drawBackground(g);
        for (Enemy e : enemies) e.draw(g);
        if (player != null) player.draw(g);
        drawHUD(g);
    }

    private void drawBackground(Graphics2D g) {
        // Sky
        g.setColor(new Color(90, 140, 195));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT / 4);
        // Ground
        g.setColor(new Color(55, 115, 45));
        g.fillRect(0, SCREEN_HEIGHT / 4, SCREEN_WIDTH, SCREEN_HEIGHT * 3 / 4);
        // Tile grid (subtle)
        g.setColor(new Color(45, 100, 35, 70));
        for (int x = 0; x <= SCREEN_WIDTH;  x += TILE_SIZE) g.drawLine(x, SCREEN_HEIGHT / 4, x, SCREEN_HEIGHT);
        for (int y = SCREEN_HEIGHT / 4; y <= SCREEN_HEIGHT; y += TILE_SIZE) g.drawLine(0, y, SCREEN_WIDTH, y);
        // Chapter banner
        String[] banners = { "Norman Conquest \u2014 1169 A.D.", "British Rule \u2014 1800s", "Easter Rising \u2014 1916" };
        g.setFont(new Font("Serif", Font.ITALIC, 14));
        g.setColor(new Color(230, 225, 200, 200));
        g.drawString(banners[Math.min(currentChapter, 2)], 10, 20);
    }

    private void drawHUD(Graphics2D g) {
        if (player == null) return;
        // Health label
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(Color.WHITE);
        g.drawString("HP:", 10, SCREEN_HEIGHT - 18);
        // Heart icons
        for (int i = 0; i < player.getMaxHealth(); i++) {
            g.setColor(i < player.getHealth() ? new Color(220, 50, 50) : new Color(75, 75, 75));
            g.fillOval(42 + i * 22, SCREEN_HEIGHT - 30, 16, 16);
            g.setColor(new Color(140, 25, 25));
            g.drawOval(42 + i * 22, SCREEN_HEIGHT - 30, 16, 16);
        }
        // Enemy count
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(Color.WHITE);
        g.drawString("Enemies: " + enemies.size(), SCREEN_WIDTH - 115, SCREEN_HEIGHT - 18);
        // Chapter indicator
        g.drawString("Chapter " + (currentChapter + 1) + "/3", SCREEN_WIDTH / 2 - 35, SCREEN_HEIGHT - 18);
    }

    private void drawPauseOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g.setFont(new Font("Serif", Font.BOLD, 42));
        g.setColor(Color.WHITE);
        drawCentered(g, "PAUSED", SCREEN_HEIGHT / 2 - 22);
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(new Color(200, 200, 200));
        drawCentered(g, "Press P / ESC / ENTER to resume", SCREEN_HEIGHT / 2 + 30);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(28, 0, 0));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g.setFont(new Font("Serif", Font.BOLD, 50));
        g.setColor(new Color(200, 50, 50));
        drawCentered(g, "IRELAND FALLS", SCREEN_HEIGHT / 2 - 44);
        g.setFont(new Font("Serif", Font.ITALIC, 20));
        g.setColor(new Color(175, 175, 175));
        drawCentered(g, "The struggle for freedom continues\u2026", SCREEN_HEIGHT / 2 + 18);
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.setColor(new Color(150, 150, 150));
        drawCentered(g, "Press ENTER to return to title", SCREEN_HEIGHT / 2 + 80);
    }

    private void drawVictory(Graphics2D g) {
        g.setColor(new Color(0, 28, 10));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g.setFont(new Font("Serif", Font.BOLD, 42));
        g.setColor(new Color(75, 200, 75));
        drawCentered(g, "\u00c9ire go Br\u00e1gh!", SCREEN_HEIGHT / 2 - 90);
        g.setFont(new Font("Serif", Font.ITALIC, 24));
        g.setColor(new Color(200, 200, 150));
        drawCentered(g, "Ireland Forever!", SCREEN_HEIGHT / 2 - 45);

        g.setFont(new Font("Serif", Font.PLAIN, 16));
        g.setColor(new Color(180, 210, 180));
        drawWrapped(g,
            "From the Norman Conquest to the Easter Rising, Ireland\u2019s people endured " +
            "innumerable struggles. Their rebellion against flagrant oppression, their " +
            "refusal to be forcibly merged into another nation \u2014 these are the starting " +
            "points of a free Ireland.",
            80, SCREEN_HEIGHT / 2 + 8, SCREEN_WIDTH - 160, 24);

        g.setFont(new Font("Serif", Font.PLAIN, 44));
        g.setColor(new Color(55, 155, 55));
        drawCentered(g, "\u2618", SCREEN_HEIGHT / 2 + 130);

        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.setColor(new Color(150, 150, 150));
        drawCentered(g, "Press ENTER to return to title", SCREEN_HEIGHT - 36);
    }

    // ── Drawing utilities ─────────────────────────────────────────────────────
    private void drawCentered(Graphics2D g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (SCREEN_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private void drawWrapped(Graphics2D g, String text, int x, int y, int maxW, int lineH) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = y;
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxW && line.length() > 0) {
                g.drawString(line.toString(), x, curY);
                curY += lineH;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) g.drawString(line.toString(), x, curY);
    }
}

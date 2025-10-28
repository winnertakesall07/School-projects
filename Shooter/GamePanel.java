import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {

    // Screen Settings
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale; // 48x48
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol; // 768
    public final int screenHeight = tileSize * maxScreenRow; // 576

    // Game State
    public enum GameState { PLAY, PAUSE, LEVEL_UP, GAME_OVER }
    public GameState gameState;

    // System
    Thread gameThread;
    KeyHandler keyH = new KeyHandler(this);
    public CollisionChecker cChecker = new CollisionChecker(this);
    public UI ui = new UI(this);
    public UpgradeSystem upgradeSystem = new UpgradeSystem(this);
    public WaveManager waveManager = new WaveManager(this);

    // Entities
    public Player player = new Player(this, keyH);
    public List<Enemy> enemies = new ArrayList<>();
    public List<Projectile> projectiles = new ArrayList<>();
    // NEW: enemy projectiles list
    public List<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    // Alias for compatibility (some code may reference lower-case 'p')
    public List<EnemyProjectile> enemyprojectiles = enemyProjectiles;

    int FPS = 60;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.gameState = GameState.PLAY;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        if (gameState == GameState.PLAY) {
            player.update();
            waveManager.update();

            // Update player projectiles
            Iterator<Projectile> pIterator = projectiles.iterator();
            while (pIterator.hasNext()) {
                Projectile p = pIterator.next();
                if (p.isAlive()) {
                    p.update();
                } else {
                    pIterator.remove();
                }
            }

            // Update enemy projectiles (NEW)
            Iterator<EnemyProjectile> epIterator = enemyProjectiles.iterator();
            while (epIterator.hasNext()) {
                EnemyProjectile ep = epIterator.next();
                if (ep.isAlive()) {
                    ep.update();
                } else {
                    epIterator.remove();
                }
            }

            // Update enemies
            Iterator<Enemy> eIterator = enemies.iterator();
            while (eIterator.hasNext()) {
                Enemy e = eIterator.next();
                if (e.isAlive()) {
                    e.update();
                } else {
                    player.gainXP(e.xpValue);
                    eIterator.remove();
                }
            }

            // All collisions (player <-> enemy bullets, player bullets <-> enemies, explosions, pierce)
            cChecker.checkCollisions();

        } else if (gameState == GameState.LEVEL_UP) {
            // Game is paused, waiting for player to choose an upgrade
            if (keyH.enterPressed) {
                upgradeSystem.selectUpgrade();
                gameState = GameState.PLAY;
                keyH.enterPressed = false; // Consume the press
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw Player
        player.draw(g2);

        // Draw Enemies
        for (Enemy enemy : enemies) {
            enemy.draw(g2);
        }

        // Draw Player Projectiles
        for (Projectile p : projectiles) {
            p.draw(g2);
        }

        // Draw Enemy Projectiles (NEW)
        for (EnemyProjectile ep : enemyProjectiles) {
            ep.draw(g2);
        }

        // Draw UI
        ui.draw(g2);

        g2.dispose();
    }

    public void resetGame() {
        player.setDefaultValues();
        enemies.clear();
        projectiles.clear();
        enemyProjectiles.clear(); // NEW
        waveManager.reset();
        gameState = GameState.PLAY;
    }
}
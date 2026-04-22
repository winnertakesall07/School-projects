import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Core game panel for Éire's Stand v3.
 *
 * New in v3:
 *  - World / camera system  — battlefield is 2400×1800 world units; a camera
 *    follows the player; the viewport is 960×720.
 *  - Larger maps with period-appropriate buildings (castle, church, house,
 *    barracks, GPO) built via the dedicated map-layout methods.
 *  - Allies  — Irish fighters who battle alongside the player but need help.
 *  - Battle-intro animation  — cinematic pan + chapter context before every fight.
 *  - War cries  — enemy and ally characters shout period-correct phrases.
 *  - Sound  — MIDI chapter music + synthesised sound effects (M = mute toggle).
 *  - Commander enemies  — always hunt the player; player must deal with them.
 *
 * Game states:
 *   TITLE        — splash screen
 *   STORY        — narrative pages
 *   BATTLE_INTRO — cinematic intro animation before the fight
 *   PLAYING      — active battle
 *   PAUSED       — overlay pause
 *   GAME_OVER    — player died
 *   VICTORY      — all chapters cleared
 */
public class GamePanel extends JPanel implements Runnable {

    // ── Screen / world constants ───────────────────────────────────────────────
    public static final int TILE_SIZE    = 48;
    public static final int SCREEN_COLS  = 20;
    public static final int SCREEN_ROWS  = 15;
    public static final int SCREEN_WIDTH  = TILE_SIZE * SCREEN_COLS; // 960
    public static final int SCREEN_HEIGHT = TILE_SIZE * SCREEN_ROWS; // 720

    // ── Game-state enum ───────────────────────────────────────────────────────
    public enum State { TITLE, STORY, BATTLE_INTRO, PLAYING, PAUSED, GAME_OVER, VICTORY }
    private State state = State.TITLE;

    // ── Core objects ──────────────────────────────────────────────────────────
    private final KeyHandler   keys         = new KeyHandler();
    private final StoryManager storyManager = new StoryManager();
    private final Camera       camera       = new Camera();
    private final SoundManager sound        = new SoundManager();
    private final Random       rng          = new Random();

    // ── Entities ──────────────────────────────────────────────────────────────
    private Player            player;
    private List<Enemy>       enemies       = new ArrayList<>();
    private List<Ally>        allies        = new ArrayList<>();
    private List<Bullet>      playerBullets = new ArrayList<>();
    private List<Bullet>      enemyBullets  = new ArrayList<>();
    private List<Bullet>      allyBullets   = new ArrayList<>();
    private List<CoverObject> covers        = new ArrayList<>();

    private int currentChapter = 0;
    private static final int TOTAL_CHAPTERS = 3;

    // ── Battle-intro state ────────────────────────────────────────────────────
    private static final int INTRO_DURATION = 360; // 6 s at 60 FPS
    private int introTimer = 0;
    // Pre-intro camera pan waypoints (world X)
    private float introCamX = 0;

    // ── Loop ──────────────────────────────────────────────────────────────────
    private static final int FPS = 60;
    private Thread gameThread;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color BG_TITLE  = new Color(18, 38, 18);
    private static final Color BG_STORY  = new Color(14, 14, 28);
    private static final Color[] BG_SKIES = {
        new Color( 80, 130, 185),   // Ch0 — pale daylight
        new Color( 95, 120, 175),   // Ch1 — overcast
        new Color( 55,  55,  95)    // Ch2 — dusk / smoke
    };
    private static final Color[] BG_GROUNDS = {
        new Color(55, 115, 45),   // Ch0 — green Irish field
        new Color(70,  90, 55),   // Ch1 — muddied ground
        new Color(65,  60, 55)    // Ch2 — rubble-strewn Dublin
    };

    // ── Constructor ───────────────────────────────────────────────────────────
    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        addKeyListener(keys);
        addMouseMotionListener(keys);
        addMouseListener(keys);
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

    // ── Update dispatch ───────────────────────────────────────────────────────
    private void update() {
        // Mute toggle (any state)
        if (keys.mutePressed) {
            keys.mutePressed = false;
            sound.setMuted(!sound.isMuted());
        }

        switch (state) {
            case TITLE:        updateTitle();       break;
            case STORY:        updateStory();       break;
            case BATTLE_INTRO: updateBattleIntro(); break;
            case PLAYING:      updatePlaying();     break;
            case PAUSED:       updatePaused();      break;
            case GAME_OVER:
            case VICTORY:
                if (keys.enterPressed) { keys.enterPressed = false; resetGame(); }
                break;
        }
    }

    private void updateTitle() {
        if (keys.enterPressed) { keys.enterPressed = false; startChapter(0); }
    }

    private void updateStory() {
        if (keys.enterPressed) {
            keys.enterPressed = false;
            if (storyManager.hasMorePages()) {
                storyManager.nextPage();
            } else {
                beginBattleIntro();
            }
        }
    }

    // ── Battle intro ──────────────────────────────────────────────────────────
    private void beginBattleIntro() {
        spawnCover(currentChapter);
        spawnEnemies(currentChapter);
        spawnAllies(currentChapter);
        // Place player at their start position
        float[] startPos = playerStartPos(currentChapter);
        player = new Player(startPos[0], startPos[1], currentChapter);
        camera.update(player.getCenterX(), player.getCenterY(), SCREEN_WIDTH, SCREEN_HEIGHT);
        // Pan the camera from enemy side to player side during intro
        introCamX = Camera.WORLD_WIDTH - SCREEN_WIDTH; // start far right (enemy territory)
        introTimer = INTRO_DURATION;
        state = State.BATTLE_INTRO;
        sound.playChapterMusic(currentChapter);
    }

    private void updateBattleIntro() {
        introTimer--;

        // Animate camera pan from enemy territory toward player
        float progress = 1f - (introTimer / (float) INTRO_DURATION);
        float targetCamX = camera.x; // final resting cam (centred on player)
        camera.update(player.getCenterX(), player.getCenterY(), SCREEN_WIDTH, SCREEN_HEIGHT);
        float finalCamX = camera.x;
        // During first 60% of intro, blend from enemy side to player side
        if (progress < 0.6f) {
            float blend = progress / 0.6f;
            // Ease in-out: smooth step
            blend = blend * blend * (3 - 2 * blend);
            camera.x = introCamX + (finalCamX - introCamX) * blend;
            camera.y = camera.y; // keep vertical centred on player
        }

        // Skip with ENTER
        if (keys.enterPressed) { keys.enterPressed = false; introTimer = 0; }

        if (introTimer <= 0) {
            // Snap camera to player and begin battle
            camera.update(player.getCenterX(), player.getCenterY(), SCREEN_WIDTH, SCREEN_HEIGHT);
            state = State.PLAYING;
        }
    }

    // ── Main gameplay update ──────────────────────────────────────────────────
    private void updatePlaying() {
        // Pause toggle
        if (keys.pausePressed || keys.escPressed) {
            keys.pausePressed = false;
            keys.escPressed   = false;
            state = State.PAUSED;
            return;
        }

        // Mouse → world coordinates
        float mouseWorldX = camera.toWorldX(keys.mouseX);
        float mouseWorldY = camera.toWorldY(keys.mouseY);

        // ── Player update ─────────────────────────────────────────────────────
        player.update(keys, Camera.WORLD_WIDTH, Camera.WORLD_HEIGHT, covers,
                      mouseWorldX, mouseWorldY);
        camera.update(player.getCenterX(), player.getCenterY(), SCREEN_WIDTH, SCREEN_HEIGHT);

        // ── Player attack ─────────────────────────────────────────────────────
        if ((keys.spaceHeld || keys.mouseFireHeld) && player.canAttack()) {
            Bullet b = player.performAttack();
            if (b != null) {
                playerBullets.add(b);
                sound.playSound("shoot");
            } else {
                // Melee swing
                sound.playSound("sword");
                for (Enemy e : enemies) {
                    if (player.isMeleeHitting(e.getCenterX(), e.getCenterY())) {
                        e.takeDamage(player.getAttackDamage());
                        sound.playSound("hit");
                    }
                }
                // Can also hit-melee on ally-adjacent but allies are friendly — skip
            }
        }

        // ── Enemy update ──────────────────────────────────────────────────────
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            Bullet shot = e.update(player.getCenterX(), player.getCenterY(),
                                   covers, allies,
                                   Camera.WORLD_WIDTH, Camera.WORLD_HEIGHT);
            if (shot != null) {
                enemyBullets.add(shot);
                sound.playSound("shoot");
            }
            if (e.isDead()) { enemies.remove(i); continue; }

            // Melee vs player
            if (e.isOverlapping(player.getCenterX(), player.getCenterY(), player.getSize())
                    && e.canMeleeAttack()) {
                e.performMeleeAttack();
                player.takeDamage(e.getAttackDamage());
                sound.playSound("hit");
            }

            // Melee vs allies
            for (int j = allies.size() - 1; j >= 0; j--) {
                Ally a = allies.get(j);
                if (a.isDead()) continue;
                if (e.isOverlapping(a.getCenterX(), a.getCenterY(), Ally.SIZE)
                        && e.canMeleeAttack()) {
                    e.performMeleeAttack();
                    a.takeDamage(e.getAttackDamage());
                }
            }
        }

        // ── Ally update ───────────────────────────────────────────────────────
        for (int i = allies.size() - 1; i >= 0; i--) {
            Ally a = allies.get(i);
            Bullet shot = a.update(enemies, covers, Camera.WORLD_WIDTH, Camera.WORLD_HEIGHT);
            if (shot != null) {
                allyBullets.add(shot);
            }
            if (a.isDead()) { allies.remove(i); continue; }

            // Ally melee vs enemy
            for (Enemy e : enemies) {
                if (e.isDead()) continue;
                if (a.isOverlapping(e.getCenterX(), e.getCenterY(), Enemy.SIZE)
                        && a.canMeleeAttack()) {
                    a.performMeleeAttack();
                    e.takeDamage(a.getAttackDamage());
                }
            }
        }

        // ── Player bullets ────────────────────────────────────────────────────
        updateBulletList(playerBullets, true, false);

        // ── Ally bullets ──────────────────────────────────────────────────────
        updateBulletList(allyBullets, false, true);

        // ── Enemy bullets ─────────────────────────────────────────────────────
        updateBulletList(enemyBullets, false, false);

        // ── Win / lose checks ─────────────────────────────────────────────────
        if (!player.isAlive()) {
            state = State.GAME_OVER;
            sound.stopMusic();
        } else if (enemies.isEmpty()) {
            playerBullets.clear();
            enemyBullets.clear();
            allyBullets.clear();
            sound.playSound("chapter_win");
            currentChapter++;
            if (currentChapter >= TOTAL_CHAPTERS) {
                state = State.VICTORY;
                sound.stopMusic();
            } else {
                startChapter(currentChapter);
            }
        }
    }

    /** Process one list of bullets (friendly or enemy). */
    private void updateBulletList(List<Bullet> bullets, boolean isPlayer, boolean isAlly) {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();

            if (b.isOutOfBounds(Camera.WORLD_WIDTH, Camera.WORLD_HEIGHT)) {
                bullets.remove(i);
                continue;
            }

            // Hit cover?
            boolean hitCover = false;
            for (CoverObject c : covers) {
                if (b.hitsCover(c)) { hitCover = true; break; }
            }
            if (hitCover) { bullets.remove(i); continue; }

            if (isPlayer || isAlly) {
                // Hit an enemy?
                boolean hitEnemy = false;
                for (Enemy e : enemies) {
                    if (e.isDead()) continue;
                    if (b.hits(e.getCenterX(), e.getCenterY(), Enemy.SIZE / 2f)) {
                        e.takeDamage(b.getDamage());
                        sound.playSound("hit");
                        hitEnemy = true;
                        break;
                    }
                }
                if (hitEnemy) { bullets.remove(i); }
            } else {
                // Enemy bullet — can hit player or allies
                boolean hit = false;
                if (b.hits(player.getCenterX(), player.getCenterY(), Player.SIZE / 2f)) {
                    player.takeDamage(b.getDamage());
                    sound.playSound("hit");
                    hit = true;
                }
                if (!hit) {
                    for (Ally a : allies) {
                        if (a.isDead()) continue;
                        if (b.hits(a.getCenterX(), a.getCenterY(), Ally.SIZE / 2f)) {
                            a.takeDamage(b.getDamage());
                            hit = true;
                            break;
                        }
                    }
                }
                if (hit) { bullets.remove(i); }
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
        covers.clear();
        playerBullets.clear();
        enemyBullets.clear();
        allyBullets.clear();
        enemies.clear();
        allies.clear();
        storyManager.loadChapter(chapter);
        float[] sp = playerStartPos(chapter);
        player = new Player(sp[0], sp[1], chapter);
        camera.update(player.getCenterX(), player.getCenterY(), SCREEN_WIDTH, SCREEN_HEIGHT);
        state = State.STORY;
    }

    private float[] playerStartPos(int chapter) {
        // Player always starts on the left / bottom-left of the world
        switch (chapter) {
            case 0: return new float[]{ 200, Camera.WORLD_HEIGHT / 2f - 50 };
            case 1: return new float[]{ 220, Camera.WORLD_HEIGHT * 0.65f };
            default: return new float[]{ 250, Camera.WORLD_HEIGHT * 0.70f };
        }
    }

    // ── Spawn covers (world-scale maps with buildings) ────────────────────────

    /**
     * Chapter 0 — Norman Conquest: open Irish hillside.
     *   - Norman castle walls (top-right)
     *   - Stone church (centre)
     *   - Scattered stone-wall sections and trees
     */
    private void spawnCover(int chapter) {
        covers.clear();
        switch (chapter) {
            case 0: buildChapter0Map(); break;
            case 1: buildChapter1Map(); break;
            case 2: buildChapter2Map(); break;
        }
    }

    private void buildChapter0Map() {
        // ── Norman castle fortress (top-right quarter) ────────────────────────
        int castleX = 1850, castleY = 80;
        // Outer walls (4 sides of a rough fortification)
        covers.add(new CoverObject(castleX,        castleY,        320, 24, "castle_wall")); // north
        covers.add(new CoverObject(castleX,        castleY + 220,  320, 24, "castle_wall")); // south
        covers.add(new CoverObject(castleX,        castleY,         24, 244,"castle_wall")); // west
        covers.add(new CoverObject(castleX + 296,  castleY,         24, 244,"castle_wall")); // east
        // Gate opening (west wall has a gap — already handled by not placing one full piece)

        // ── Stone church (centre-right) ───────────────────────────────────────
        covers.add(new CoverObject(1150, 600, 130, 180, "church"));

        // ── Scattered trees (Irish bogland) ───────────────────────────────────
        int[][] trees = {
            {320, 200}, {440, 350}, {600, 140}, {700, 480},
            {900, 260}, {1000, 700}, {1300, 320}, {1500, 550},
            {1600, 200}, {1700, 750}, {400, 700}, {800, 900},
            {550, 1000},{1100, 1100},{1400, 950},{1700, 1200},
            {300, 1300},{700, 1450},{1000, 1400},{1600, 1500}
        };
        for (int[] t : trees) covers.add(new CoverObject(t[0], t[1], 56, 64, "tree"));

        // ── Low stone walls (field boundaries) ────────────────────────────────
        covers.add(new CoverObject( 500, 500, 180, 22, "wall"));
        covers.add(new CoverObject( 800, 400,  22, 160,"wall"));
        covers.add(new CoverObject(1050, 800, 200,  22,"wall"));
        covers.add(new CoverObject(1350, 650,  22, 180,"wall"));
        covers.add(new CoverObject( 600, 900, 160,  22,"wall"));
        covers.add(new CoverObject(1600, 900, 220,  22,"wall"));
        covers.add(new CoverObject( 350, 1200, 22, 150,"wall"));
        covers.add(new CoverObject( 900, 1300,190,  22,"wall"));
        covers.add(new CoverObject(1250,1200, 22, 200,"wall"));
    }

    private void buildChapter1Map() {
        // ── British barracks (top-right) ──────────────────────────────────────
        covers.add(new CoverObject(1700, 100, 450, 250, "barracks"));

        // ── Georgian-style houses (left / centre) ─────────────────────────────
        covers.add(new CoverObject( 350,  250, 120, 140, "house"));
        covers.add(new CoverObject( 600,  250, 120, 140, "house"));
        covers.add(new CoverObject( 900,  150, 120, 140, "house"));
        covers.add(new CoverObject(1150,  300, 120, 140, "house"));
        covers.add(new CoverObject( 450,  700, 120, 140, "house"));
        covers.add(new CoverObject( 750,  750, 120, 140, "house"));
        covers.add(new CoverObject(1000, 1000, 120, 140, "house"));
        covers.add(new CoverObject(1300,  900, 120, 140, "house"));
        covers.add(new CoverObject( 300, 1200, 120, 140, "house"));
        covers.add(new CoverObject( 600, 1300, 120, 140, "house"));
        covers.add(new CoverObject( 900, 1400, 120, 140, "house"));
        covers.add(new CoverObject(1500, 1200, 120, 140, "house"));

        // ── Stone walls (fortification lines) ────────────────────────────────
        covers.add(new CoverObject(1200, 200, 280,  24, "wall"));
        covers.add(new CoverObject(1200, 200,  24, 300, "wall"));
        covers.add(new CoverObject(1550, 300,  24, 250, "wall"));
        covers.add(new CoverObject( 500, 500, 200,  24, "wall"));
        covers.add(new CoverObject( 800, 600,  24, 180, "wall"));
        covers.add(new CoverObject(1000, 800, 220,  24, "wall"));
        covers.add(new CoverObject( 300, 900,  24, 200, "wall"));
        covers.add(new CoverObject( 700,1100, 200,  24, "wall"));

        // ── Barrels and crates (supply area) ─────────────────────────────────
        covers.add(new CoverObject(1100, 500, 40, 40, "barrel"));
        covers.add(new CoverObject(1160, 500, 40, 40, "barrel"));
        covers.add(new CoverObject(1100, 560, 48, 48, "crate"));
        covers.add(new CoverObject(1200, 550, 48, 48, "crate"));
        covers.add(new CoverObject( 600, 900, 40, 40, "barrel"));
        covers.add(new CoverObject( 660, 900, 40, 40, "barrel"));
    }

    private void buildChapter2Map() {
        // ── GPO building (centre of map) ──────────────────────────────────────
        covers.add(new CoverObject(900, 500, 400, 280, "gpo"));

        // ── Row houses (Dublin city streets) ──────────────────────────────────
        int[][] houses = {
            {200, 150},{350,150},{500,150},{650,150},{800,150},
            {200,1500},{350,1500},{500,1500},{650,1500},{800,1500},
            {1500,150},{1650,150},{1800,150},{1950,150},{2100,150},
            {1500,1500},{1650,1500},{1800,1500},{1950,1500},{2100,1500},
            {100, 400},{100, 600},{100, 800},{100,1000},{100,1200},
            {2200,400},{2200,600},{2200,800},{2200,1000},{2200,1200}
        };
        for (int[] h : houses) covers.add(new CoverObject(h[0], h[1], 110, 130, "house"));

        // ── Rubble and barricades (street-level cover) ────────────────────────
        int[][] rubble = {
            { 400, 400},{ 700, 350},{ 650, 700},{1400, 400},
            {1700, 600},{1400,1000},{ 800, 900},{1200, 800},
            { 500,1100},{ 900,1200},{1500,1300},{ 600,1300}
        };
        for (int[] r : rubble) covers.add(new CoverObject(r[0], r[1], 90, 40, "rubble"));

        // ── Barricades / crates ───────────────────────────────────────────────
        int[][] crates = {
            { 500, 600},{ 600, 550},{1300, 700},{1350, 750},
            { 750, 800},{ 800, 850},{1100, 600},{1150, 650},
            { 700,1100},{ 750,1150},{1400, 900},{1450, 950}
        };
        for (int[] c : crates) covers.add(new CoverObject(c[0], c[1], 46, 46, "crate"));

        // ── Street walls (Dublin tenement blocks) ─────────────────────────────
        covers.add(new CoverObject( 300, 500, 24, 300, "wall"));
        covers.add(new CoverObject( 600, 250, 300, 24, "wall"));
        covers.add(new CoverObject(1400, 250, 400, 24, "wall"));
        covers.add(new CoverObject(2000, 400,  24, 400,"wall"));
        covers.add(new CoverObject( 400,1000, 300, 24, "wall"));
        covers.add(new CoverObject(1200, 900,  24, 350,"wall"));
        covers.add(new CoverObject(1600,1100, 350, 24, "wall"));
    }

    // ── Spawn enemies ─────────────────────────────────────────────────────────

    /**
     * Chapter 0: 4 melee, 3 flanker, 2 ranged (if applicable), 1 COMMANDER
     * Chapter 1: 5 melee, 4 ranged, 2 flanker, 1 COMMANDER
     * Chapter 2: 5 melee, 4 ranged, 4 flanker, 2 COMMANDER
     */
    private void spawnEnemies(int chapter) {
        enemies.clear();
        switch (chapter) {
            case 0:
                spawnEGroup(4, Enemy.SubType.MELEE,     chapter, 1900, 300, 100, 120);
                spawnEGroup(3, Enemy.SubType.FLANKER,   chapter, 2000, 700, 100, 130);
                spawnEGroup(2, Enemy.SubType.MELEE,     chapter, 1800,1100, 110, 130);
                spawnEGroup(2, Enemy.SubType.FLANKER,   chapter, 2100,1300, 110, 130);
                spawnEGroup(1, Enemy.SubType.COMMANDER, chapter, 2050, 550,   0,   0);
                break;
            case 1:
                spawnEGroup(5, Enemy.SubType.MELEE,     chapter, 2000, 200, 100, 120);
                spawnEGroup(4, Enemy.SubType.RANGED,    chapter, 2100, 500, 100, 130);
                spawnEGroup(3, Enemy.SubType.FLANKER,   chapter, 1900, 900, 100, 130);
                spawnEGroup(3, Enemy.SubType.MELEE,     chapter, 2100,1300, 100, 130);
                spawnEGroup(1, Enemy.SubType.COMMANDER, chapter, 2150, 700,   0,   0);
                break;
            case 2:
                spawnEGroup(5, Enemy.SubType.MELEE,     chapter, 1900, 200, 100, 120);
                spawnEGroup(4, Enemy.SubType.RANGED,    chapter, 2050, 550, 100, 130);
                spawnEGroup(4, Enemy.SubType.FLANKER,   chapter, 1950,1000, 100, 130);
                spawnEGroup(4, Enemy.SubType.MELEE,     chapter, 2100,1400, 100, 130);
                spawnEGroup(2, Enemy.SubType.COMMANDER, chapter, 2200, 800, 200,   0);
                break;
        }
    }

    private void spawnEGroup(int count, Enemy.SubType sub, int chapter,
                              int baseX, int baseY, int stepX, int stepY) {
        for (int i = 0; i < count; i++) {
            int ex = baseX + (i % 3) * stepX + rng.nextInt(30) - 15;
            int ey = baseY + (i / 3) * stepY + rng.nextInt(30) - 15;
            enemies.add(new Enemy(ex, ey, chapter, sub));
        }
    }

    // ── Spawn allies ──────────────────────────────────────────────────────────

    /**
     * Chapter 0: 5 melee Irish clansmen
     * Chapter 1: 4 melee + 3 ranged United Irishmen
     * Chapter 2: 4 melee + 4 ranged IRA fighters
     */
    private void spawnAllies(int chapter) {
        allies.clear();
        float[] sp = playerStartPos(chapter);
        int ax = (int) sp[0] + 80;
        int ay = (int) sp[1];
        switch (chapter) {
            case 0:
                spawnAGroup(5, Ally.SubType.MELEE,  chapter, ax, ay - 120, 90, 120);
                break;
            case 1:
                spawnAGroup(4, Ally.SubType.MELEE,  chapter, ax, ay - 150, 90, 120);
                spawnAGroup(3, Ally.SubType.RANGED, chapter, ax, ay + 120, 90, 110);
                break;
            case 2:
                spawnAGroup(4, Ally.SubType.MELEE,  chapter, ax, ay - 150, 90, 120);
                spawnAGroup(4, Ally.SubType.RANGED, chapter, ax, ay + 130, 90, 110);
                break;
        }
    }

    private void spawnAGroup(int count, Ally.SubType sub, int chapter,
                              int baseX, int baseY, int stepX, int stepY) {
        for (int i = 0; i < count; i++) {
            int ax = baseX + (i % 3) * stepX + rng.nextInt(20) - 10;
            int ay = baseY + (i / 3) * stepY + rng.nextInt(20) - 10;
            allies.add(new Ally(ax, ay, chapter, sub));
        }
    }

    private void resetGame() {
        currentChapter = 0;
        enemies.clear();
        allies.clear();
        playerBullets.clear();
        enemyBullets.clear();
        allyBullets.clear();
        covers.clear();
        player = null;
        sound.stopMusic();
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
            case TITLE:        drawTitle(g2);                                  break;
            case STORY:        drawStory(g2);                                  break;
            case BATTLE_INTRO: drawBattle(g2); drawBattleIntroOverlay(g2);     break;
            case PLAYING:      drawBattle(g2);                                 break;
            case PAUSED:       drawBattle(g2); drawPauseOverlay(g2);           break;
            case GAME_OVER:    drawGameOver(g2);                               break;
            case VICTORY:      drawVictory(g2);                                break;
        }
    }

    // ── Title screen ──────────────────────────────────────────────────────────
    private void drawTitle(Graphics2D g) {
        g.setColor(BG_TITLE);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Decorative border
        g.setColor(new Color(180, 150, 60));
        g.setStroke(new BasicStroke(3));
        g.drawRect(20, 20, SCREEN_WIDTH - 40, SCREEN_HEIGHT - 40);

        g.setFont(new Font("Serif", Font.BOLD, 52));
        g.setColor(new Color(220, 200, 100));
        drawCentered(g, "\u00c9ire\u2019s Stand", SCREEN_HEIGHT / 2 - 110);

        g.setFont(new Font("Serif", Font.ITALIC, 26));
        g.setColor(new Color(175, 210, 175));
        drawCentered(g, "Ireland\u2019s Struggle for Freedom", SCREEN_HEIGHT / 2 - 52);

        g.setFont(new Font("Serif", Font.PLAIN, 52));
        g.setColor(new Color(55, 155, 55));
        drawCentered(g, "\u2618 \u2618 \u2618", SCREEN_HEIGHT / 2 + 18);

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(new Color(200, 200, 200));
        drawCentered(g, "Press ENTER to begin", SCREEN_HEIGHT / 2 + 110);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(150, 150, 150));
        drawCentered(g, "WASD / Arrows \u2014 Move   |   Mouse \u2014 Aim   |   SPACE / Click \u2014 Attack   |   P / ESC \u2014 Pause   |   M \u2014 Mute",
                     SCREEN_HEIGHT - 45);
        g.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g.setColor(new Color(110, 160, 110));
        drawCentered(g, "v3: Larger maps \u2022 Allied soldiers \u2022 Battle intro \u2022 War cries \u2022 Sound", SCREEN_HEIGHT - 22);
    }

    // ── Story screen ──────────────────────────────────────────────────────────
    private void drawStory(Graphics2D g) {
        StoryPage page = storyManager.getCurrentPage();
        if (page == null) return;

        g.setColor(BG_STORY);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(new Color(100, 80, 40));
        g.setStroke(new BasicStroke(2));
        g.drawRect(15, 15, SCREEN_WIDTH - 30, SCREEN_HEIGHT - 30);

        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(new Color(160, 140, 80));
        drawCentered(g, page.chapterLabel, 58);

        g.setFont(new Font("Serif", Font.BOLD, 32));
        g.setColor(new Color(220, 200, 120));
        drawCentered(g, page.title, 112);

        g.setColor(new Color(100, 80, 40));
        g.setStroke(new BasicStroke(1));
        g.drawLine(80, 130, SCREEN_WIDTH - 80, 130);

        g.setFont(new Font("Serif", Font.PLAIN, 18));
        g.setColor(new Color(210, 210, 210));
        drawWrapped(g, page.text, 65, 165, SCREEN_WIDTH - 130, 27);

        // Vocab / tip note box
        int boxY = SCREEN_HEIGHT - 120;
        g.setColor(new Color(38, 33, 18));
        g.fillRoundRect(40, boxY, SCREEN_WIDTH - 80, 70, 10, 10);
        g.setColor(new Color(180, 150, 60));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(40, boxY, SCREEN_WIDTH - 80, 70, 10, 10);
        g.setFont(new Font("SansSerif", Font.ITALIC, 13));
        g.setColor(new Color(220, 200, 140));
        drawWrapped(g, page.vocabNote, 56, boxY + 24, SCREEN_WIDTH - 112, 20);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(160, 160, 180));
        String prompt = storyManager.hasMorePages()
                ? "Press ENTER to continue \u25ba"
                : "Press ENTER to begin the battle \u2694";
        drawCentered(g, prompt, SCREEN_HEIGHT - 18);
    }

    // ── Battle screen (used for PLAYING, PAUSED, and BATTLE_INTRO) ───────────
    private void drawBattle(Graphics2D g) {
        // Apply camera transform — everything inside draws in world coordinates
        AffineTransform savedTransform = g.getTransform();
        g.translate(-camera.x, -camera.y);

        drawBackground(g);
        for (CoverObject c : covers)   c.draw(g);
        for (Ally        a : allies)   a.draw(g);
        for (Bullet      b : enemyBullets)  b.draw(g);
        for (Bullet      b : playerBullets) b.draw(g);
        for (Bullet      b : allyBullets)   b.draw(g);
        for (Enemy       e : enemies)  e.draw(g);
        if (player != null) player.draw(g);

        g.setTransform(savedTransform);

        // HUD drawn in screen coordinates (no camera offset)
        drawHUD(g);
    }

    private void drawBackground(Graphics2D g) {
        int ch = Math.min(currentChapter, 2);
        Color sky    = BG_SKIES[ch];
        Color ground = BG_GROUNDS[ch];

        // Sky strip (top quarter of world)
        g.setColor(sky);
        g.fillRect(0, 0, Camera.WORLD_WIDTH, Camera.WORLD_HEIGHT / 5);

        // Ground
        g.setColor(ground);
        g.fillRect(0, Camera.WORLD_HEIGHT / 5, Camera.WORLD_WIDTH, Camera.WORLD_HEIGHT * 4 / 5);

        // Subtle tile grid
        g.setColor(new Color(ground.getRed() - 10, ground.getGreen() - 12,
                             ground.getBlue() - 10, 50));
        for (int x = 0; x <= Camera.WORLD_WIDTH;  x += TILE_SIZE)
            g.drawLine(x, Camera.WORLD_HEIGHT / 5, x, Camera.WORLD_HEIGHT);
        for (int y = Camera.WORLD_HEIGHT / 5; y <= Camera.WORLD_HEIGHT; y += TILE_SIZE)
            g.drawLine(0, y, Camera.WORLD_WIDTH, y);

        // Chapter scene label (world coord, always top-left of world)
        String[] banners = { "Norman Conquest \u2014 1169 A.D.",
                             "British Rule \u2014 1800s",
                             "Easter Rising \u2014 1916" };
        g.setFont(new Font("Serif", Font.ITALIC, 16));
        g.setColor(new Color(230, 225, 200, 200));
        g.drawString(banners[ch], 15, 22);
    }

    // ── Battle intro overlay ──────────────────────────────────────────────────
    private void drawBattleIntroOverlay(Graphics2D g) {
        float progress = 1f - (introTimer / (float) INTRO_DURATION);

        // Fade-in black at start, fade-out at end
        int alpha;
        if (progress < 0.12f) {
            alpha = (int)(255 * (1f - progress / 0.12f));
        } else if (progress > 0.85f) {
            alpha = (int)(255 * ((progress - 0.85f) / 0.15f));
        } else {
            alpha = 0;
        }
        if (alpha > 0) {
            g.setColor(new Color(0, 0, 0, Math.min(255, alpha)));
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        // Central info panel (visible in the middle portion)
        if (progress > 0.1f && progress < 0.88f) {
            float panelAlpha = Math.min(1f, (progress - 0.1f) / 0.12f)
                             * Math.min(1f, (0.88f - progress) / 0.08f);
            int pa = (int)(panelAlpha * 220);
            if (pa > 0) {
                // Background panel
                g.setColor(new Color(0, 0, 0, pa));
                g.fillRoundRect(SCREEN_WIDTH / 2 - 300, SCREEN_HEIGHT / 2 - 130,
                                600, 270, 18, 18);
                g.setColor(new Color(180, 150, 60, pa));
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(SCREEN_WIDTH / 2 - 300, SCREEN_HEIGHT / 2 - 130,
                                600, 270, 18, 18);

                // Chapter label
                g.setFont(new Font("SansSerif", Font.BOLD, 13));
                g.setColor(new Color(160, 140, 80, pa));
                drawCentered(g, getChapterLabel(currentChapter), SCREEN_HEIGHT / 2 - 95);

                // Big title
                g.setFont(new Font("Serif", Font.BOLD, 34));
                g.setColor(new Color(255, 240, 120, pa));
                drawCentered(g, getBattleTitle(currentChapter), SCREEN_HEIGHT / 2 - 50);

                // Separator
                g.setColor(new Color(160, 130, 60, pa / 2));
                g.drawLine(SCREEN_WIDTH / 2 - 220, SCREEN_HEIGHT / 2 - 30,
                           SCREEN_WIDTH / 2 + 220, SCREEN_HEIGHT / 2 - 30);

                // Force description
                g.setFont(new Font("Serif", Font.PLAIN, 15));
                g.setColor(new Color(200, 215, 200, pa));
                String[] lines = getBattleDescription(currentChapter);
                for (int i = 0; i < lines.length; i++) {
                    drawCentered(g, lines[i], SCREEN_HEIGHT / 2 + 5 + i * 22);
                }

                // Skip prompt
                g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g.setColor(new Color(160, 160, 160, pa));
                drawCentered(g, "Press ENTER to skip", SCREEN_HEIGHT / 2 + 112);
            }
        }

        // BATTLE BEGINS! flash at end
        if (progress > 0.80f) {
            float flashAlpha = (progress - 0.80f) / 0.20f;
            if ((int)(introTimer / 8) % 2 == 0) {
                g.setFont(new Font("Serif", Font.BOLD, 44));
                g.setColor(new Color(255, 60, 60, (int)(flashAlpha * 240)));
                drawCentered(g, "BATTLE BEGINS!", SCREEN_HEIGHT / 2);
            }
        }
    }

    private String   getChapterLabel(int ch) {
        String[] labels = { "CHAPTER I \u2022 1169 A.D.", "CHAPTER II \u2022 1803 A.D.", "CHAPTER III \u2022 1916 A.D." };
        return labels[Math.min(ch, 2)];
    }
    private String   getBattleTitle(int ch) {
        String[] titles = { "The Battle of the Green Hills", "Emmet\u2019s Rebellion", "The Battle for the GPO" };
        return titles[Math.min(ch, 2)];
    }
    private String[] getBattleDescription(int ch) {
        switch (ch) {
            case 0: return new String[]{
                "Your side: Irish Clan Warriors  \u2694  Enemy: Norman Knights",
                "The Norman COMMANDER leads the charge \u2014 only you can stop him.",
                "Your clan brothers fight at your flanks. Lead them to victory!"
            };
            case 1: return new String[]{
                "Your side: United Irishmen  \u2694  Enemy: British Redcoats",
                "The British SERGEANT commands the redcoats \u2014 he targets you.",
                "Rebels hold the streets. Break through to the barracks!"
            };
            default: return new String[]{
                "Your side: IRA Fighters  \u2694  Enemy: Black-and-Tans",
                "Two OFFICERS direct the Tans \u2014 both hunt you relentlessly.",
                "Hold the GPO! Ireland\u2019s freedom hangs in the balance."
            };
        }
    }

    // ── HUD ───────────────────────────────────────────────────────────────────
    private void drawHUD(Graphics2D g) {
        if (player == null) return;

        // ── Health bar ────────────────────────────────────────────────────────
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(Color.WHITE);
        g.drawString("HP:", 10, SCREEN_HEIGHT - 18);
        for (int i = 0; i < player.getMaxHealth(); i++) {
            g.setColor(i < player.getHealth() ? new Color(220, 50, 50) : new Color(75, 75, 75));
            g.fillOval(42 + i * 22, SCREEN_HEIGHT - 30, 16, 16);
            g.setColor(new Color(140, 25, 25));
            g.drawOval(42 + i * 22, SCREEN_HEIGHT - 30, 16, 16);
        }

        // ── Weapon label ──────────────────────────────────────────────────────
        String[] weaponNames = { "Claymore Sword", "Flintlock Musket", "Lee-Enfield Rifle" };
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(new Color(220, 200, 140));
        g.drawString(weaponNames[player.getChapter()], 10, SCREEN_HEIGHT - 36);

        // ── Reload / cooldown bar ─────────────────────────────────────────────
        int barFullW = 130, barH = 8, barX = 10, barY = SCREEN_HEIGHT - 52;
        int cooldown  = player.getAttackCooldown();
        int remaining = player.getAttackTimer();
        int filled    = cooldown > 0
                ? (int)((1f - (float) remaining / cooldown) * barFullW)
                : barFullW;
        g.setColor(new Color(60, 60, 60));
        g.fillRect(barX, barY, barFullW, barH);
        Color barColor = player.getChapter() == 0 ? new Color(185, 185, 205)
                       : (player.getChapter() == 1 ? new Color(180, 140, 60)
                                                   : new Color(80, 200, 120));
        g.setColor(barColor);
        g.fillRect(barX, barY, filled, barH);
        g.setColor(Color.GRAY);
        g.drawRect(barX, barY, barFullW, barH);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(new Color(180, 180, 180));
        g.drawString(player.getChapter() == 0 ? "SWING" : "RELOAD", barX + barFullW + 4, barY + barH);

        // ── Enemy & ally counts ───────────────────────────────────────────────
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(220, 80, 80));
        g.drawString("Enemies: " + enemies.size(), SCREEN_WIDTH - 130, SCREEN_HEIGHT - 36);
        g.setColor(new Color(80, 220, 100));
        g.drawString("Allies: "  + allies.size(),  SCREEN_WIDTH - 130, SCREEN_HEIGHT - 18);

        // ── Chapter indicator ─────────────────────────────────────────────────
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(Color.WHITE);
        g.drawString("Chapter " + (currentChapter + 1) + " / " + TOTAL_CHAPTERS,
                     SCREEN_WIDTH / 2 - 45, SCREEN_HEIGHT - 18);

        // ── Mute indicator ────────────────────────────────────────────────────
        if (sound.isMuted()) {
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.setColor(new Color(200, 80, 80));
            g.drawString("[MUTED] (M)", SCREEN_WIDTH / 2 - 50, 20);
        }

        // ── Commander warning ─────────────────────────────────────────────────
        boolean cmdAlive = enemies.stream()
                               .anyMatch(e -> e.getSubType() == Enemy.SubType.COMMANDER);
        if (cmdAlive) {
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.setColor(new Color(255, 220, 50));
            drawCentered(g, "\u26A0 COMMANDER on the field \u2014 only YOU can stop them! \u26A0",
                         SCREEN_HEIGHT - 52);
        }

        // ── In-cover indicator ────────────────────────────────────────────────
        boolean inCover = false;
        for (CoverObject c : covers) {
            if (player.getCenterX() >= c.x - 8 && player.getCenterX() <= c.x + c.width  + 8 &&
                player.getCenterY() >= c.y - 8 && player.getCenterY() <= c.y + c.height + 8) {
                inCover = true;
                break;
            }
        }
        if (inCover) {
            g.setColor(new Color(80, 200, 80));
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            drawCentered(g, "[ IN COVER ]", 20);
        }
    }

    // ── Pause overlay ─────────────────────────────────────────────────────────
    private void drawPauseOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g.setFont(new Font("Serif", Font.BOLD, 46));
        g.setColor(Color.WHITE);
        drawCentered(g, "PAUSED", SCREEN_HEIGHT / 2 - 30);
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(new Color(200, 200, 200));
        drawCentered(g, "Press P / ESC / ENTER to resume   |   M to toggle sound", SCREEN_HEIGHT / 2 + 30);
    }

    // ── Game over ─────────────────────────────────────────────────────────────
    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(28, 0, 0));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g.setFont(new Font("Serif", Font.BOLD, 54));
        g.setColor(new Color(200, 50, 50));
        drawCentered(g, "IRELAND FALLS", SCREEN_HEIGHT / 2 - 60);
        g.setFont(new Font("Serif", Font.ITALIC, 22));
        g.setColor(new Color(175, 175, 175));
        drawCentered(g, "The struggle for freedom continues\u2026", SCREEN_HEIGHT / 2 + 10);
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.setColor(new Color(150, 150, 150));
        drawCentered(g, "Press ENTER to return to title", SCREEN_HEIGHT / 2 + 80);
    }

    // ── Victory screen ────────────────────────────────────────────────────────
    private void drawVictory(Graphics2D g) {
        g.setColor(new Color(0, 28, 10));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g.setFont(new Font("Serif", Font.BOLD, 46));
        g.setColor(new Color(75, 200, 75));
        drawCentered(g, "\u00c9ire go Br\u00e1gh!", SCREEN_HEIGHT / 2 - 110);
        g.setFont(new Font("Serif", Font.ITALIC, 26));
        g.setColor(new Color(200, 200, 150));
        drawCentered(g, "Ireland Forever!", SCREEN_HEIGHT / 2 - 58);

        g.setFont(new Font("Serif", Font.PLAIN, 17));
        g.setColor(new Color(180, 210, 180));
        drawWrapped(g,
            "From the Norman Conquest to the Easter Rising, Ireland\u2019s people endured " +
            "innumerable struggles. Their rebellion against flagrant oppression, their " +
            "refusal to be forcibly merged into another nation \u2014 these are the starting " +
            "points of a free Ireland. And it was your blade that turned the tide.",
            90, SCREEN_HEIGHT / 2 - 12, SCREEN_WIDTH - 180, 26);

        g.setFont(new Font("Serif", Font.PLAIN, 48));
        g.setColor(new Color(55, 155, 55));
        drawCentered(g, "\u2618 \u2618 \u2618", SCREEN_HEIGHT / 2 + 130);

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
        FontMetrics   fm    = g.getFontMetrics();
        String[]      words = text.split(" ");
        StringBuilder line  = new StringBuilder();
        int curY = y;
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxW && line.length() > 0) {
                g.drawString(line.toString(), x, curY);
                curY += lineH;
                line  = new StringBuilder(word);
            } else {
                line  = new StringBuilder(test);
            }
        }
        if (line.length() > 0) g.drawString(line.toString(), x, curY);
    }
}

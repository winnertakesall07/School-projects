import java.util.Random;

public class WaveManager {
    GamePanel gp;
    public int waveNumber;
    private int spawnCooldown;
    private int enemiesToSpawn;
    private boolean bossWave = false;
    private Random random = new Random();

    public WaveManager(GamePanel gp) {
        this.gp = gp;
        reset();
    }
    
    public void reset() {
        this.waveNumber = 0;
        this.spawnCooldown = 120; // 2 seconds
        this.enemiesToSpawn = 0;
        this.bossWave = false;
        startNextWave();
    }

    public void startNextWave() {
        waveNumber++;
        // Every 10th wave is a boss wave
        if (waveNumber % 10 == 0) {
            bossWave = true;
            enemiesToSpawn = 1;
            spawnCooldown = 30;
        } else {
            bossWave = false;
            enemiesToSpawn = 6 + waveNumber * 2; // Increase enemies each wave
            spawnCooldown = Math.max(10, 60 - waveNumber); // faster spawns later
        }
    }

    public void update() {
        if (gp.enemies.isEmpty() && enemiesToSpawn == 0) {
            startNextWave();
        }

        if (enemiesToSpawn > 0) {
            spawnCooldown--;
            if (spawnCooldown <= 0) {
                spawnEnemy();
                enemiesToSpawn--;
                spawnCooldown = bossWave ? 9999 : Math.max(10, 60 - waveNumber);
            }
        }
    }

    private void spawnEnemy() {
        if (bossWave) {
            // Spawn a boss near the edges
            int side = random.nextInt(4);
            int x = 0, y = 0;
            switch (side) {
                case 0: x = random.nextInt(gp.screenWidth); y = -gp.tileSize * 2; break;
                case 1: x = random.nextInt(gp.screenWidth); y = gp.screenHeight + gp.tileSize; break;
                case 2: x = -gp.tileSize * 2; y = random.nextInt(gp.screenHeight); break;
                case 3: x = gp.screenWidth + gp.tileSize; y = random.nextInt(gp.screenHeight); break;
            }
            gp.enemies.add(new BossEnemy(gp, x, y));
            return;
        }

        // Choose a spawn side
        int side = random.nextInt(4);
        int x = 0, y = 0;
        switch (side) {
            case 0: x = random.nextInt(gp.screenWidth); y = -gp.tileSize; break;
            case 1: x = random.nextInt(gp.screenWidth); y = gp.screenHeight; break;
            case 2: x = -gp.tileSize; y = random.nextInt(gp.screenHeight); break;
            case 3: x = gp.screenWidth; y = random.nextInt(gp.screenHeight); break;
        }

        // Select enemy type based on waveNumber and random chance
        int roll = random.nextInt(100);
        if (waveNumber >= 4 && roll < 20) {
            gp.enemies.add(new ShooterEnemy(gp, x, y));
        } else if (waveNumber >= 3 && roll < 40) {
            gp.enemies.add(new TankEnemy(gp, x, y));
        } else if (waveNumber >= 2 && roll < 70) {
            gp.enemies.add(new FastEnemy(gp, x, y));
        } else {
            gp.enemies.add(new BasicEnemy(gp, x, y));
        }
    }
}
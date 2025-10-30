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
        
        // Every 5th wave spawns a MiniBoss
        if (waveNumber % 5 == 0) {
            bossWave = true;
            enemiesToSpawn = 1;
            spawnCooldown = 30;
        } else {
            bossWave = false;
            // Faster wave scaling
            enemiesToSpawn = 8 + waveNumber * 3;
            spawnCooldown = Math.max(6, 50 - waveNumber * 2);
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
                spawnCooldown = bossWave ? 9999 : Math.max(6, 50 - waveNumber * 2);
            }
        }
    }

    private void spawnEnemy() {
        if (bossWave) {
            // Spawn a MiniBoss near the edges
            int side = random.nextInt(4);
            int x = 0, y = 0;
            switch (side) {
                case 0: x = random.nextInt(gp.screenWidth); y = -gp.tileSize * 2; break;
                case 1: x = random.nextInt(gp.screenWidth); y = gp.screenHeight + gp.tileSize; break;
                case 2: x = -gp.tileSize * 2; y = random.nextInt(gp.screenHeight); break;
                case 3: x = gp.screenWidth + gp.tileSize; y = random.nextInt(gp.screenHeight); break;
            }
            gp.enemies.add(new MiniBoss(gp, x, y));
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

        // Weighted spawn table based on wave number
        int roll = random.nextInt(100);
        
        // Always available
        if (roll < 15) {
            gp.enemies.add(new BasicEnemy(gp, x, y));
            return;
        }
        
        // Wave 2+
        if (waveNumber >= 2) {
            if (roll < 30) {
                gp.enemies.add(new FastEnemy(gp, x, y));
                return;
            }
            if (roll < 45) {
                gp.enemies.add(new Jumper(gp, x, y));
                return;
            }
        }
        
        // Wave 3+
        if (waveNumber >= 3) {
            if (roll < 55) {
                gp.enemies.add(new TankEnemy(gp, x, y));
                return;
            }
            if (roll < 65) {
                gp.enemies.add(new Assassin(gp, x, y));
                return;
            }
        }
        
        // Wave 4+
        if (waveNumber >= 4) {
            if (roll < 72) {
                gp.enemies.add(new ShooterEnemy(gp, x, y));
                return;
            }
            if (roll < 79) {
                gp.enemies.add(new Charger(gp, x, y));
                return;
            }
        }
        
        // Wave 5+
        if (waveNumber >= 5) {
            if (roll < 85) {
                gp.enemies.add(new Chemist(gp, x, y));
                return;
            }
            if (roll < 90) {
                gp.enemies.add(new Healer(gp, x, y));
                return;
            }
        }
        
        // Wave 6+
        if (waveNumber >= 6) {
            if (roll < 95) {
                gp.enemies.add(new Engineer(gp, x, y));
                return;
            }
        }
        
        // Default fallback
        gp.enemies.add(new BasicEnemy(gp, x, y));
    }
}
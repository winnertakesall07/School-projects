import java.util.Random;

public class WaveManager {
    GamePanel gp;
    public int waveNumber;
    private int spawnCooldown;
    private int enemiesToSpawn;
    private Random random = new Random();

    public WaveManager(GamePanel gp) {
        this.gp = gp;
        reset();
    }
    
    public void reset() {
        this.waveNumber = 0;
        this.spawnCooldown = 120; // 2 seconds
        this.enemiesToSpawn = 0;
        startNextWave();
    }

    public void startNextWave() {
        waveNumber++;
        enemiesToSpawn = 5 + waveNumber * 2; // Increase enemies each wave
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
                spawnCooldown = 60 - waveNumber; // Spawn faster in later waves
                if (spawnCooldown < 10) spawnCooldown = 10;
            }
        }
    }

    private void spawnEnemy() {
        int side = random.nextInt(4);
        int x = 0, y = 0;
        
        switch(side) {
            case 0: // Top
                x = random.nextInt(gp.screenWidth);
                y = -gp.tileSize;
                break;
            case 1: // Bottom
                x = random.nextInt(gp.screenWidth);
                y = gp.screenHeight;
                break;
            case 2: // Left
                x = -gp.tileSize;
                y = random.nextInt(gp.screenHeight);
                break;
            case 3: // Right
                x = gp.screenWidth;
                y = random.nextInt(gp.screenHeight);
                break;
        }
        gp.enemies.add(new BasicEnemy(gp, x, y));
    }
}

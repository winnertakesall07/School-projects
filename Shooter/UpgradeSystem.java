import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpgradeSystem {
    GamePanel gp;
    List<Upgrade> availableUpgrades;
    List<Upgrade> currentChoices;
    int selectedChoice = 0;

    public UpgradeSystem(GamePanel gp) {
        this.gp = gp;
        initUpgrades();
    }

    private void initUpgrades() {
        availableUpgrades = new ArrayList<>();
        availableUpgrades.add(new Upgrade("Max HP +20", "HP_UP"));
        availableUpgrades.add(new Upgrade("Speed +1", "SPEED_UP"));
        availableUpgrades.add(new Upgrade("Unlock Shotgun", "WEAPON_SHOTGUN"));
        availableUpgrades.add(new Upgrade("Faster Shooting", "FIRERATE_UP"));
    }

    public void generateUpgrades() {
        currentChoices = new ArrayList<>();
        List<Upgrade> pool = new ArrayList<>(availableUpgrades);
        Collections.shuffle(pool);

        for (int i = 0; i < 3 && i < pool.size(); i++) {
            currentChoices.add(pool.get(i));
        }
        selectedChoice = 0;
    }

    public void selectUpgrade() {
        if (currentChoices != null && !currentChoices.isEmpty()) {
            Upgrade choice = currentChoices.get(selectedChoice);
            applyUpgrade(choice);
        }
    }

    private void applyUpgrade(Upgrade upgrade) {
        switch (upgrade.id) {
            case "HP_UP":
                gp.player.maxHp += 20;
                gp.player.hp = gp.player.maxHp; // Heal to full
                break;
            case "SPEED_UP":
                gp.player.speed += 1;
                break;
            case "WEAPON_SHOTGUN":
                gp.player.currentWeapon = new Shotgun(gp);
                // Remove this upgrade from the pool so it's not offered again
                availableUpgrades.removeIf(u -> u.id.equals("WEAPON_SHOTGUN"));
                break;
            case "FIRERATE_UP":
                gp.player.currentWeapon.fireRate = Math.max(5, gp.player.currentWeapon.fireRate - 5);
                break;
        }
    }

    public void changeSelection(int delta) {
        selectedChoice += delta;
        if (selectedChoice < 0) {
            selectedChoice = currentChoices.size() - 1;
        }
        if (selectedChoice >= currentChoices.size()) {
            selectedChoice = 0;
        }
    }

    public void draw(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        int y = gp.screenHeight / 2 - 50;
        for (int i = 0; i < currentChoices.size(); i++) {
            if (i == selectedChoice) {
                g2.setColor(Color.YELLOW);
            } else {
                g2.setColor(Color.WHITE);
            }
            String text = currentChoices.get(i).description;
            int x = gp.ui.getXforCenteredText(text, g2);
            g2.drawString(text, x, y + i * 50);
        }
    }
}

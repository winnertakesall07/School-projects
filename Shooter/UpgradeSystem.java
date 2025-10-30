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
        // Core stats
        availableUpgrades.add(new Upgrade("Max HP +20", "HP_UP"));
        availableUpgrades.add(new Upgrade("Speed +1", "SPEED_UP"));
        availableUpgrades.add(new Upgrade("Faster Shooting", "FIRERATE_UP"));

        // Ranged weapon unlocks
        availableUpgrades.add(new Upgrade("Unlock Shotgun", "WEAPON_SHOTGUN"));
        availableUpgrades.add(new Upgrade("Unlock Machine Gun", "WEAPON_MACHINEGUN"));
        availableUpgrades.add(new Upgrade("Unlock Sniper", "WEAPON_SNIPER"));
        availableUpgrades.add(new Upgrade("Unlock Rocket Launcher", "WEAPON_ROCKET"));
        availableUpgrades.add(new Upgrade("Unlock Laser Beam", "WEAPON_LASER"));

        // Melee weapon unlocks
        availableUpgrades.add(new Upgrade("Unlock Spear (melee)", "WEAPON_SPEAR"));
        availableUpgrades.add(new Upgrade("Unlock Sword (melee)", "WEAPON_SWORD"));
        availableUpgrades.add(new Upgrade("Unlock Axe (melee)", "WEAPON_AXE"));
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

    private void removeFromPool(String id) {
        availableUpgrades.removeIf(u -> u.id.equals(id));
    }

    private void record(String description) {
        gp.player.acquiredUpgrades.add(description);
    }

    private void applyUpgrade(Upgrade upgrade) {
        switch (upgrade.id) {
            case "HP_UP":
                gp.player.maxHp += 20;
                gp.player.hp = gp.player.maxHp; // Heal to full
                record(upgrade.description);
                break;
            case "SPEED_UP":
                gp.player.speed += 1;
                record(upgrade.description);
                break;
            case "FIRERATE_UP":
                gp.player.currentWeapon.fireRate = Math.max(5, gp.player.currentWeapon.fireRate - 5);
                record(upgrade.description);
                break;

            // Ranged weapons
            case "WEAPON_SHOTGUN": {
                Weapon w = new Shotgun(gp);
                gp.player.addWeaponIfNew(w);
                gp.player.currentWeapon = w;
                removeFromPool("WEAPON_SHOTGUN");
                record(upgrade.description);
                break;
            }
            case "WEAPON_MACHINEGUN": {
                Weapon w = new MachineGun(gp);
                gp.player.addWeaponIfNew(w);
                gp.player.currentWeapon = w;
                removeFromPool("WEAPON_MACHINEGUN");
                record(upgrade.description);
                break;
            }
            case "WEAPON_SNIPER": {
                Weapon w = new Sniper(gp);
                gp.player.addWeaponIfNew(w);
                gp.player.currentWeapon = w;
                removeFromPool("WEAPON_SNIPER");
                record(upgrade.description);
                break;
            }
            case "WEAPON_ROCKET": {
                Weapon w = new RocketLauncher(gp);
                gp.player.addWeaponIfNew(w);
                gp.player.currentWeapon = w;
                removeFromPool("WEAPON_ROCKET");
                record(upgrade.description);
                break;
            }
            case "WEAPON_LASER": {
                Weapon w = new LaserBeam(gp);
                gp.player.addWeaponIfNew(w);
                gp.player.currentWeapon = w;
                removeFromPool("WEAPON_LASER");
                record(upgrade.description);
                break;
            }

            // Melee weapons
            case "WEAPON_SPEAR": {
                Weapon w = new Spear(gp);
                gp.player.addWeaponIfNew(w);
                gp.player.currentWeapon = w;
                removeFromPool("WEAPON_SPEAR");
                record(upgrade.description);
                break;
            }
            case "WEAPON_SWORD": {
                Weapon w = new Sword(gp);
                gp.player.addWeaponIfNew(w);
                gp.player.currentWeapon = w;
                removeFromPool("WEAPON_SWORD");
                record(upgrade.description);
                break;
            }
            case "WEAPON_AXE": {
                Weapon w = new Axe(gp);
                gp.player.addWeaponIfNew(w);
                gp.player.currentWeapon = w;
                removeFromPool("WEAPON_AXE");
                record(upgrade.description);
                break;
            }
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
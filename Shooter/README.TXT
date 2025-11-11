------------------------------------------------------------------------
Dies ist die README-Datei des Projekts. Hier sollten Sie Ihr Projekt
beschreiben.
Erzählen Sie dem Leser (jemand, der nichts über dieses Projekt weiss),
alles, was er/sie wissen muss. Üblicherweise sollte der Kommentar 
zumindest die folgenden Angaben umfassen:
------------------------------------------------------------------------

PROJEKTBEZEICHNUNG: Java Roguelike Shooter
PROJEKTZWECK: Wave-based shooter game with upgrades and multiple enemy types
VERSION oder DATUM: 2025
WIE IST DAS PROJEKT ZU STARTEN: Compile and run Game.java
AUTOR(EN): winnertakesall07
BENUTZERHINWEISE:

------------------------------------------------------------------------
CUSTOM ASSETS
------------------------------------------------------------------------
The game uses sprite-based graphics for all entities. Sprites are loaded
from the /assets/ directory. If you want to customize the game's appearance,
you can replace the PNG files in this directory.

Asset Directory: Shooter/assets/

Required Sprite Files:
  Player:
    - player.png (default/idle)
    - player_0.png, player_1.png (optional animation frames)
  
  Enemies:
    - enemy_basic.png
    - enemy_fast.png
    - enemy_tank.png
    - enemy_jumper.png (with optional enemy_jumper_0.png, enemy_jumper_1.png)
    - enemy_assassin.png (with optional enemy_assassin_0.png, enemy_assassin_1.png)
    - enemy_engineer.png
    - enemy_turret.png
    - enemy_miniboss.png
    - enemy_healer.png
    - enemy_chemist.png
    - enemy_charger.png
  
  Projectiles:
    - bullet.png (default pistol/machinegun)
    - shotgun_pellet.png
    - laser.png
    - rocket.png
  
  Melee Weapons:
    - melee_arc.png (sword/axe swing overlay)
    - spear_thrust.png (spear thrust overlay)

Sprite Guidelines:
  - Recommended size: 32-48 pixels
  - Format: PNG with transparency (RGBA)
  - The game scales sprites to fit the tile size (48x48)
  - If a sprite is missing, the game will fall back to simple colored rectangles

To Add Custom Art:
  1. Create or download PNG images for the entities you want to customize
  2. Place them in the Shooter/assets/ directory
  3. Name them according to the list above
  4. Restart the game to see your changes

The game automatically loads and caches sprites at runtime.


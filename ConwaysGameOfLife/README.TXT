Conway's Game of Life & Tic-Tac-Toe — BlueJ Project
=====================================================

HOW TO RUN IN BLUEJ:
1. Open BlueJ
2. File → Open Project → select this folder
3. Right-click the "Main" class → void main(String[] args) → OK
   (This opens the launcher to choose a game)

   OR right-click "GameOfLife" class → void main(String[] args) → OK
   OR right-click "TicTacToeGoL" class → void main(String[] args) → OK

HOW TO COMPILE FROM TERMINAL:
  cd ConwaysGameOfLife
  javac *.java
  java Main           # launcher
  java GameOfLife     # directly run GoL
  java TicTacToeGoL   # directly run GoL Tic-Tac-Toe

GAME CONTROLS — Conway's Game of Life:
  ▶ Start     — start the simulation
  ⏸ Stop      — pause the simulation
  ⏭ Step      — advance one generation
  ↺ Reset     — clear the board
  ⚡ Random   — fill with random cells
  Speed slider — control simulation speed (FPS)
  Pattern selector + Place — place a preset pattern
  Mouse click/drag — toggle cells on/off

PRESET PATTERNS:
  • Glider          — moves diagonally across the board
  • Gosper Glider Gun — generates gliders continuously
  • Pulsar          — period-3 oscillator
  • Blinker         — simplest period-2 oscillator
  • Beacon          — period-2 oscillator

GAME CONTROLS — Tic-Tac-Toe on GoL:
  Two human players (X and O) take turns clicking squares
  X pieces appear as a coral cross pattern of GoL cells
  O pieces appear as a gold ring pattern of GoL cells
  GoL evolves freely in all empty squares (living background)
  Win/draw is detected and shown in the status bar
  New Game button resets the grid and randomises the background

CLASSES:
  Main.java             — Launcher window
  GameOfLife.java       — GoL main entry point
  GameOfLifeBoard.java  — Grid logic & GoL rules (B3/S23)
  GameOfLifeGUI.java    — Swing GUI for GoL
  Pattern.java          — Preset pattern definitions
  TicTacToeGoL.java     — Tic-Tac-Toe (GoL edition) entry point
  TicTacToeGoLGUI.java  — 63×63 GoL grid with Tic-Tac-Toe overlay

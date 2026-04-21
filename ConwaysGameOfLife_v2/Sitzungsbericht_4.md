# Sitzungsbericht 4 – Conway's Game of Life V2 (Abschluss)

**Datum:** 09.04.2025  
**Projekt:** Conway's Game of Life V2  
**Teilnehmer:** Gruppe 3  
**Entwicklungsumgebung:** BlueJ

---

## Ziel der Sitzung

In der letzten Sitzung wollten wir das Projekt abschließen. Geplant waren: eine vollständige Musterbibliothek mit Ghost-Vorschau, das Bonus-Feature Tic-Tac-Toe auf dem GoL-Gitter, und ein gemeinsames Launcher-Fenster, das beide Spiele startet.

---

## Was haben wir gemacht?

### Klasse `Pattern` – Musterbibliothek

Wir haben eine separate Klasse `Pattern` erstellt, die ein benanntes Muster als Array von `(x, y)`-Koordinaten speichert. Über statische Factory-Methoden stellt die Klasse vorgefertigte Muster bereit, unterteilt in fünf Kategorien:

| Kategorie | Beispiele |
|-----------|-----------|
| Still Lifes | Block, Beehive, Loaf, Boat, Tub |
| Oszillatoren | Blinker (P2), Toad (P2), Pulsar (P3), Pentadecathlon (P15) |
| Raumschiffe | Glider, LWSS |
| Kanonen | Gosper Glider Gun |
| Methuselahs | R-Pentomino, Diehard, Acorn |

Insgesamt haben wir 18 Muster implementiert. Jedes Muster hat einen sprechenden Namen (der in der GUI angezeigt wird) und eine `getBounds()`-Methode, die die Ausdehnung des Musters zurückgibt.

In der `GameOfLifeBoard`-Klasse gibt es die Methode `setCellPattern(Pattern p, int offsetX, int offsetY)`, die das Muster mit einem Versatz in das Board einfügt – ohne das restliche Board zu löschen.

### Ghost-Vorschau beim Platzieren

In der GUI haben wir einen Platzierungsmodus implementiert. Wenn der Nutzer ein Muster aus der Dropdown-Liste auswählt und auf „Place ▸" klickt, wechselt die GUI in den Platzierungsmodus:
- Der Cursor zeigt die ausgewählten Zellen als transparentes, hellblaues Geisterbild an, das der Maus folgt.
- Ein gelbes Label oben zeigt an, welches Muster gerade platziert wird.
- Ein Linksklick stempelt das Muster an der aktuellen Position auf das Board.
- Die `Escape`-Taste oder ein Klick auf „Abbrechen" beendet den Modus.

Die Ghost-Darstellung wird direkt in `paintComponent()` gezeichnet, indem wir die aktuellen Mauskoordinaten (in Board-Koordinaten umgerechnet) nehmen und die Zellen des Musters mit einer halbtransparenten Farbe zeichnen:

```java
if (pendingPattern != null && mouseBoard != null) {
    g2.setColor(COLOR_GHOST); // rgba(0, 180, 255, 120)
    for (int[] coord : pendingPattern.getCoordinates()) {
        int px = boardToScreenX(mouseBoard[0] + coord[0]);
        int py = boardToScreenY(mouseBoard[1] + coord[1]);
        g2.fillRect(px, py, (int)cellSize, (int)cellSize);
    }
}
```

### Tic-Tac-Toe auf Conway's Game of Life

Als kreatives Bonus-Feature haben wir Tic-Tac-Toe implementiert, das auf einem Conway's-Game-of-Life-Gitter läuft. Die Idee: Das klassische 3×3-Tic-Tac-Toe-Brett wird auf einem 63×63-Zellen-GoL-Gitter dargestellt. Jedes der neun Felder ist eine 19×19-Zellen-Region; die Trennlinien zwischen den Feldern bestehen aus "Wand"-Zellen, die bei jeder Generation fixiert bleiben und die GoL-Regeln ignorieren.

Die X- und O-Symbole sind als 11×11-Muster aus lebenden Zellen vordefiniert (als `boolean[][]`-Array in der Klasse `TicTacToeGoLGUI`). Wenn ein Spieler ein Feld auswählt, werden die entsprechenden Zellen als "Spieler-X"- oder "Spieler-O"-Typ markiert und in ihrer jeweiligen Farbe (Rot für X, Gelb für O) dargestellt – unabhängig von den GoL-Regeln, die weiterhin für alle anderen Zellen laufen.

Das Ergebnis: Während man Tic-Tac-Toe spielt, "lebt" das Spielfeld im Hintergrund weiter – lebende Zellen erscheinen und verschwinden, aber die Spielfiguren und Wände bleiben stabil. Das sieht sehr interessant aus.

Die Gewinnprüfung erfolgt über alle 8 Linien (3 Zeilen, 3 Spalten, 2 Diagonalen):

```java
int[][] lines = {
    {0,1,2},{3,4,5},{6,7,8},
    {0,3,6},{1,4,7},{2,5,8},
    {0,4,8},{2,4,6}
};
```

### Launcher-Fenster (`Main.java`)

Um beiden Spielen einen gemeinsamen Einstiegspunkt zu geben, haben wir die Klasse `Main` erstellt. Sie zeigt ein Launcher-Fenster im gleichen dunklen Grün-Schwarz-Design, in dem der Nutzer wählen kann zwischen:
1. **Conway's Game of Life (V2)** – öffnet `GameOfLifeGUI`
2. **Tic-Tac-Toe (inside GoL)** – öffnet `TicTacToeGoLGUI`
3. **Beide starten** – öffnet beide Fenster gleichzeitig

Das ASCII-Art-Logo und das Gittermuster im Hintergrund sind rein dekorativ, geben dem Launcher aber einen charakteristischen Look.

---

## Probleme / Schwierigkeiten

- **Fixierte Zellen im TicTacToe:** Anfangs wurden Wand- und Spielerzellen doch von den GoL-Regeln beeinflusst und verschwanden schnell. Wir haben das durch einen `cellType`-Array behoben, der für jede Zelle speichert, ob sie "normal" (GoL-Regeln gelten), "Wand" oder "Spielfigur" ist. Nicht-normale Zellen werden in `nextGeneration()` einfach übersprungen.
- **Ghost-Vorschau bei schnellem Scrolling:** Der Ghost-Cursor verzögerte sich kurz beim Schwenken. Das lag daran, dass wir die Mausposition nicht in Echtzeit abgefragt haben. Wir haben `MouseMotionListener.mouseMoved()` verwendet, um die Position laufend zu aktualisieren.
- **BlueJ-Kompatibilität:** BlueJ verwaltet den Classpath intern, daher mussten wir sicherstellen, dass alle Klassen im gleichen Paket liegen und kein zusätzliches Build-System benötigt wird.

---

## Ergebnis und Reflexion

Das Projekt ist abgeschlossen. Conway's Game of Life V2 bietet:

- Ein **unendliches Spielfeld** auf Basis eines `HashSet<Long>` (sparsame Repräsentation).
- **Zoom** (1–64 Pixel/Zelle) mit dem Mausrad, zentriert auf den Mauszeiger.
- **Schwenken** per Rechtsklick-Ziehen.
- **18 vorgefertigte Muster** in 5 Kategorien mit Ghost-Vorschau.
- **Tic-Tac-Toe**, das auf dem GoL-Gitter läuft, als kreative Erweiterung.
- Ein gemeinsames **Launcher-Fenster** für beide Spiele.

Rückblickend war die wichtigste konzeptionelle Entscheidung der Wechsel vom Array zum `HashSet` für das Board. Das hat nicht nur das unendliche Spielfeld ermöglicht, sondern auch die Simulation deutlich effizienter gemacht, weil leere Regionen gar nicht berechnet werden. Die Implementierung von Zoom und Schwenken war technisch die anspruchsvollste Aufgabe, hat aber durch das Viewport-Konzept gut funktioniert.

---

## Abschließende Bewertung

| Aspekt | Bewertung |
|--------|-----------|
| GoL-Regelumsetzung | ✅ Vollständig korrekt |
| Unendliches Board | ✅ Implementiert |
| Zoom / Schwenken | ✅ Implementiert |
| Musterbibliothek | ✅ 18 Muster |
| Ghost-Vorschau | ✅ Implementiert |
| Tic-Tac-Toe-Erweiterung | ✅ Implementiert |
| Launcher | ✅ Implementiert |

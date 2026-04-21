# Sitzungsbericht 2 – Conway's Game of Life

**Datum:** 12.03.2025  
**Projekt:** Conway's Game of Life  
**Teilnehmer:** Gruppe 3  
**Entwicklungsumgebung:** BlueJ

---

## Ziel der Sitzung

Aufbauend auf der letzten Sitzung wollten wir heute eine grafische Benutzeroberfläche (GUI) mit Java Swing erstellen. Die textbasierte Konsolenausgabe sollte durch ein interaktives Fenster ersetzt werden, in dem man das Spiel auch steuern kann.

---

## Was haben wir gemacht?

### GUI mit Java Swing

Wir haben eine neue Klasse `GameOfLifeGUI` erstellt, die von `JFrame` erbt. Das Spielfeld wird auf einem `JPanel` gezeichnet, indem wir die Methode `paintComponent(Graphics g)` überschreiben. Für jede lebende Zelle wird ein gefülltes Rechteck gezeichnet:

```java
for (int y = 0; y < ROWS; y++) {
    for (int x = 0; x < COLS; x++) {
        if (cells[y][x]) {
            g.setColor(Color.GREEN);
            g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }
}
```

Tote Zellen werden mit einem dunklen Hintergrund dargestellt. Das gibt dem Spiel bereits ein ansprechendes, "Matrix"-ähnliches Aussehen.

### Toolbar mit Schaltflächen

Wir haben eine Toolbar unterhalb des Spielfelds hinzugefügt mit folgenden Buttons:
- **Start / Stopp:** Startet oder pausiert die automatische Weiterentwicklung.
- **Schritt:** Führt eine einzelne Generation aus (nützlich zum Debuggen).
- **Zufällig:** Füllt das Board mit einer zufälligen Belegung (~30 % lebende Zellen).
- **Löschen:** Setzt das gesamte Board zurück.

Für die automatische Simulation haben wir einen `javax.swing.Timer` verwendet, der alle 100 ms die `nextGeneration()`-Methode aufruft und das Panel neu zeichnet.

### Mausinteraktion

Wir haben einen `MouseListener` und einen `MouseMotionListener` zum Panel hinzugefügt, sodass man durch Klicken und Ziehen Zellen setzen oder löschen kann. Dabei haben wir die Mauskoordinaten durch die Zellgröße geteilt, um die Grid-Position zu berechnen.

### Generationszähler

Unten im Fenster wird ein Label mit der aktuellen Generationsnummer und der Anzahl lebender Zellen angezeigt, das sich nach jedem Schritt aktualisiert.

---

## Probleme / Schwierigkeiten

- **Flimmern beim Zeichnen:** Anfangs flimmerte das Bild stark, da das Panel bei jedem Neuzeichnen komplett gelöscht wurde. Wir haben das durch Double-Buffering behoben, was in Swing standardmäßig über `JPanel` verfügbar ist (wir hatten `setDoubleBuffered(true)` vergessen).
- **Timer und EDT:** Wir haben zunächst den Timer von einem normalen Thread aus gestartet, was zu Problemen führte. Nach etwas Recherche haben wir gelernt, dass alle Swing-Operationen im Event Dispatch Thread (EDT) laufen müssen und den Timer entsprechend korrekt initialisiert.
- **Feste Boardgröße:** Das Board hat eine feste Größe (z. B. 80×60 Zellen). Muster, die über den Rand hinauswachsen, "verschwinden" einfach. Das ist eine bekannte Einschränkung, die wir in einem späteren Schritt beheben wollen.

---

## Ergebnis

Wir haben jetzt eine vollständig funktionierende, grafische Version von Conway's Game of Life. Das Spiel kann gestartet, pausiert und zurückgesetzt werden. Der Benutzer kann per Maus eigene Muster zeichnen. Die Anwendung sieht schon recht gut aus, und es macht Spaß, verschiedene Startmuster auszuprobieren.

Wir haben einige klassische Muster getestet:
- **Blinker** (Periode 2, Oszillator)
- **Block** (stabiles Still-Life-Muster)
- **Gleiter** (wandert diagonal über das Feld)

---

## Nächste Schritte

- Die feste Boardgröße ist eine Einschränkung – wir wollen ein theoretisch unendliches Spielfeld implementieren.
- Zoom und Schwenken wären hilfreich, um große Muster beobachten zu können.
- Eine Bibliothek bekannter Muster (Gleiter, Kanonen usw.) wäre eine nette Erweiterung.

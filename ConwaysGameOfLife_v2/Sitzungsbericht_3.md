# Sitzungsbericht 3 – Conway's Game of Life V2

**Datum:** 26.03.2025  
**Projekt:** Conway's Game of Life V2  
**Teilnehmer:** Gruppe 3  
**Entwicklungsumgebung:** BlueJ

---

## Ziel der Sitzung

Das größte Problem unserer bisherigen Version war das feste, begrenzte Spielfeld. Große Muster – vor allem Gleiter-Kanonen, die kontinuierlich neue Gleiter produzieren – laufen schnell gegen den Rand und verschwinden. In dieser Sitzung wollten wir das Spielfeld auf ein theoretisch unendliches Board umstellen. Außerdem sollten Zoom und Schwenken implementiert werden.

---

## Was haben wir gemacht?

### Neues Datenmodell: Sparse Board mit `HashSet`

Anstatt alle Zellen – lebende und tote – in einem zweidimensionalen Array zu speichern, verwenden wir jetzt nur die **lebenden Zellen** in einem `HashSet`. Jede Zelle wird durch ihre `(x, y)`-Koordinaten als einzelner `long`-Wert kodiert:

```java
public static long key(int x, int y) {
    return ((long) x << 32) | (y & 0xFFFFFFFFL);
}
```

Die x-Koordinate wird in die oberen 32 Bit gepackt, die y-Koordinate in die unteren 32 Bit. So kann man jedes beliebige Ganzzahl-Koordinatenpaar verlustfrei in einem einzigen `long` speichern. Mit `keyX()` und `keyY()` kann man die Koordinaten jederzeit wieder herausrechnen.

**Vorteile:**
- Das Board ist jetzt wirklich unendlich – Zellen können bei jeder ganzzahligen Koordinate existieren.
- Große leere Bereiche kosten keinen Speicher.
- Der Algorithmus für `nextGeneration()` wird effizienter, weil man nur lebende Zellen und deren direkte Nachbarn betrachten muss:

```java
Map<Long, Integer> counts = new HashMap<>();
for (long ck : cells) {
    int cx = keyX(ck), cy = keyY(ck);
    for (int dy = -1; dy <= 1; dy++)
        for (int dx = -1; dx <= 1; dx++) {
            if (dx == 0 && dy == 0) continue;
            counts.merge(key(cx+dx, cy+dy), 1, Integer::sum);
        }
}
```

Anschließend werden nur die Zellen mit 3 Nachbarn (Geburt) und lebende Zellen mit 2 oder 3 Nachbarn (Überleben) in das neue Set übernommen.

### Zoom und Schwenken in der GUI

Da das Board jetzt unendlich ist, brauchen wir ein Viewport-System. Wir speichern drei Werte:
- `cellSize` (double): Pixelgröße einer Zelle (1 bis 64 Pixel).
- `viewX` / `viewY` (double): Board-Koordinaten der linken oberen Ecke des sichtbaren Bereichs.

**Zoom** wird mit dem Mausrad gesteuert. Dabei zoomen wir auf den Mauszeiger, nicht auf die Bildmitte, damit der Punkt unter dem Cursor stabil bleibt:

```java
double worldX = viewX + e.getX() / cellSize;
double worldY = viewY + e.getY() / cellSize;
cellSize = Math.max(MIN_CELL_SIZE, Math.min(MAX_CELL_SIZE, cellSize * factor));
viewX = worldX - e.getX() / cellSize;
viewY = worldY - e.getY() / cellSize;
```

**Schwenken** erfolgt durch Rechtsklick-Ziehen (oder Mittelklick-Ziehen). Beim Drücken der Maustaste speichern wir die aktuelle Mausposition, und beim Ziehen berechnen wir den Versatz in Board-Koordinaten.

### Zeichenoptimierung

Da wir nicht mehr einfach über ein festes Array iterieren können, berechnen wir beim Zeichnen, welcher Bereich des Boards gerade sichtbar ist, und iterieren dann nur über die lebenden Zellen im `HashSet`, die in diesem Bereich liegen:

```java
for (long k : board.getAliveCells()) {
    int cx = GameOfLifeBoard.keyX(k);
    int cy = GameOfLifeBoard.keyY(k);
    if (cx >= x0 && cx <= x1 && cy >= y0 && cy <= y1) {
        // Zelle zeichnen
    }
}
```

Bei kleinem Zoom (viele Zellen sichtbar) kann das noch etwas langsam sein, aber für normale Simulationen funktioniert es gut.

---

## Probleme / Schwierigkeiten

- **Koordinaten-Umrechnung:** Die Umrechnung zwischen Bildschirmpixeln und Board-Koordinaten war zunächst fehleranfällig, vor allem wenn `cellSize` kein ganzzahliger Wert ist. Wir haben das durch konsequente `double`-Arithmetik gelöst.
- **Zoom auf Mauszeiger:** Die Formel für das Zoomen auf einen festen Punkt hat beim ersten Versuch nicht korrekt funktioniert. Nach einigem Ausprobieren und Nachrechnen haben wir die richtige Formel gefunden (siehe oben).
- **Muster-Platzierung ohne Löschen:** Wir wollen Muster auf das bestehende Board stempeln können, ohne es vorher zu löschen. Das funktioniert jetzt mit der `setCellPattern()`-Methode in `GameOfLifeBoard`, die einfach die Koordinaten des Musters (mit einem Offset) in das Set einfügt.

---

## Ergebnis

Die neue Version V2 läuft deutlich besser als V1. Das Spielfeld ist jetzt praktisch unendlich, und wir können mit dem Mausrad hinein- und herauszoomen sowie mit Rechtsklick schwenken. Die Gosper Glider Gun (Gospers Gleiter-Kanone) haben wir als Test verwendet: Sie feuert kontinuierlich Gleiter ab, die sich unendlich weit über das Board bewegen – das wäre mit dem alten festen Array nicht möglich gewesen.

---

## Nächste Schritte

- Eine Bibliothek verschiedener bekannter Muster einbauen (Still-Lifes, Oszillatoren, Raumschiffe, Methuselah-Muster).
- Einen Ghost-Cursor implementieren, der das Muster als transparente Vorschau anzeigt, bevor es platziert wird.
- Als Bonus-Feature: Tic-Tac-Toe, das auf dem Game-of-Life-Gitter läuft.

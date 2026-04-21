# Sitzungsbericht 1 – Conway's Game of Life

**Datum:** 05.03.2025  
**Projekt:** Conway's Game of Life  
**Teilnehmer:** Gruppe 3  
**Entwicklungsumgebung:** BlueJ

---

## Ziel der Sitzung

Wir wollten uns zunächst einen Überblick über Conway's Game of Life (kurz: GoL) verschaffen und verstehen, wie das Spiel funktioniert. Außerdem sollten erste Schritte zur Implementierung in Java unternommen werden.

---

## Was haben wir gemacht?

### Recherche und Regelverständnis

Zu Beginn haben wir uns gemeinsam die Regeln von Conway's Game of Life angeschaut. Das Spiel basiert auf einem zweidimensionalen Gitter aus Zellen, die entweder lebendig oder tot sein können. Pro Generation werden folgende Regeln angewendet:

- Eine **lebende Zelle mit 2 oder 3 lebenden Nachbarn** überlebt in die nächste Generation.
- Eine **lebende Zelle mit weniger als 2 oder mehr als 3 Nachbarn** stirbt.
- Eine **tote Zelle mit genau 3 lebenden Nachbarn** wird lebendig (Reproduktion).
- Alle anderen toten Zellen bleiben tot.

Diese vier einfachen Regeln erzeugen erstaunlich komplexe und teilweise unvorhersehbare Muster.

### Erste Implementierung in Java (BlueJ)

Wir haben ein neues BlueJ-Projekt angelegt und begonnen, die Grundstruktur zu programmieren. Als erstes haben wir eine Klasse `GameOfLifeBoard` erstellt, die das Spielfeld als zweidimensionales `boolean`-Array speichert. Jede Zelle ist entweder `true` (lebendig) oder `false` (tot).

```java
boolean[][] cells = new boolean[50][50];
```

Wir haben eine einfache Methode `nextGeneration()` geschrieben, die das Board einmal weiterentwickelt. Dabei haben wir darauf geachtet, das aktuelle Board nicht zu verändern, während wir es auslesen – stattdessen erstellen wir ein neues Array für den nächsten Zustand.

Die Methode zum Zählen der Nachbarn war die größte Herausforderung: Für jede Zelle müssen alle acht Nachbarfelder geprüft werden, dabei muss man aufpassen, die Randbedingungen (Rand des Arrays) nicht zu vergessen.

### Erste Tests

Wir haben einige Zellen manuell auf `true` gesetzt und die `nextGeneration()`-Methode aufgerufen. Die Ausgabe haben wir zunächst in der Konsole als Textgitter gedruckt (`#` für lebendig, `.` für tot). Das hat funktioniert, auch wenn es noch sehr unübersichtlich aussieht.

---

## Probleme / Schwierigkeiten

- **Randbehandlung:** Beim Zählen der Nachbarn an den Rändern des Gitters haben wir zunächst `ArrayIndexOutOfBoundsException`-Fehler bekommen. Wir haben das durch eine `if`-Abfrage gelöst, die prüft, ob der Nachbarindex noch im gültigen Bereich liegt.
- **Doppeltes Array:** Anfangs haben wir vergessen, ein separates Array für die nächste Generation zu erstellen, und haben das Board direkt verändert. Das hat zu falschen Ergebnissen geführt. Nach dem Debugging haben wir das Problem erkannt und behoben.

---

## Ergebnis

Am Ende der Sitzung haben wir ein funktionierendes, wenn auch noch sehr einfaches, textbasiertes Conway's Game of Life. Die Regeln werden korrekt angewendet, und wir konnten erste Muster wie den **Blinker** (3 Zellen in einer Reihe, die zwischen horizontaler und vertikaler Ausrichtung wechseln) testen.

---

## Nächste Schritte

- Eine grafische Oberfläche (GUI) mit Java Swing erstellen, damit das Spiel visuell ansprechend wird.
- Schaltflächen für Start, Stopp und Zurücksetzen hinzufügen.
- Die Möglichkeit einbauen, Zellen per Mausklick zu setzen oder zu löschen.

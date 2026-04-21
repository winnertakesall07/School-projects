============================================================
  JEOPARDY V2 – Nietzsche: „Gott ist tot"
  2-Team-Edition für den Unterricht (BlueJ-kompatibel)
============================================================

STARTEN IN BLUEJ
----------------
1. Öffne BlueJ
2. Klicke: Project → Open Project
3. Wähle diesen Ordner (jeopordy game v2)
4. BlueJ zeigt alle Klassen als Diagramm
5. Rechtsklick auf "JeopardyGameV2" → void main(String[] args) → OK
6. Der Titelbildschirm erscheint!

TITELBILDSCHIRM
---------------
• Gib den Namen von Team 1 und Team 2 in die Textfelder ein
• Klicke "▶ SPIEL STARTEN!" – das Spielbrett öffnet sich

SPIELREGELN
-----------
• Der Moderator (du) steuert alles vom Computer aus
• Schüler melden sich und du klickst das richtige Team
• Klicke eine farbige Kachel → die Frage erscheint (mit Einblend-Animation)
• Klicke "💡 ANTWORT ZEIGEN" um die Antwort zu enthüllen
• Klicke dann "✅ Team X" wenn ein Team richtig geantwortet hat
  → Eine Feier-Animation zeigt das richtige Team
• Oder "❌ NIEMAND" wenn niemand die Antwort wusste
• Punkte werden automatisch vergeben und angezeigt
• Mit "🔄 RESET" startet das Spiel neu

TEAMNAMEN WÄHREND DES SPIELS ÄNDERN
-------------------------------------
• Klicke den Button "✏️ Namen" oben rechts im Spiel
• Gib neue Namen für Team 1 und Team 2 ein

PUNKTE MANUELL ANPASSEN
------------------------
• Jedes Team hat kleine +/− Buttons neben seinem Punktestand
• +100 / −100 pro Klick (nützlich falls mal ein Fehler passiert)

FRAGEN ANPASSEN
---------------
Öffne JeopardyGameV2.java in BlueJ (Doppelklick auf die Klasse).
Scrolle zur Methode initializeQuestions().
Ändere Fragen nach diesem Schema:

    questions[KATEGORIE][REIHE] = new Question(
        "Deine Frage hier",
        "Die Antwort hier"
    );

KATEGORIE = 0–4 (von links nach rechts auf dem Brett)
REIHE     = 0–4 (entspricht 100, 200, 300, 400, 500 Punkte)

SOUNDS HINZUFÜGEN
-----------------
Lies die Datei sounds/HOW_TO_ADD_SOUNDS.txt
Lege WAV-Dateien in den Ordner sounds/ – fertig!

ÄNDERUNGEN GEGENÜBER V1
------------------------
✓ Nur 2 Teams (nicht 3 Spieler)
✓ Titelbildschirm mit animiertem Sternenhintergrund und Namenseingabe
✓ Schönere Benutzeroberfläche mit Farbverläufen
✓ Einblend-Animation beim Öffnen einer Frage
✓ Grüne Feier-Animation wenn ein Team richtig antwortet
✓ Puls-Animation der Punkteanzeige bei Punktevergabe

SYSTEMVORAUSSETZUNGEN
---------------------
• Java 8 oder höher (BlueJ bringt Java mit)
• BlueJ Version 4.x oder höher
• Keine weiteren Abhängigkeiten oder Installationen nötig

============================================================
  Viel Erfolg bei deiner Aktivierungsphase! 🎉
============================================================

============================================================
  JEOPARDY – Nietzsche: „Gott ist tot"
  BlueJ-Spiel für den Religionsunterricht, Q12 München
============================================================

STARTEN IN BLUEJ
----------------
1. Öffne BlueJ
2. Klicke: Project → Open Project
3. Wähle diesen Ordner (JeopardyGame)
4. BlueJ zeigt alle Klassen als Diagramm
5. Rechtsklick auf "JeopardyGame" → void main(String[] args) → OK
6. Das Spiel startet sofort!

SPIELREGELN
-----------
• Der Moderator (du) steuert alles vom Computer aus
• Schüler melden sich und du klickst den richtigen Spieler
• Klicke eine farbige Kachel → die Frage erscheint
• Klicke "💡 ANTWORT ZEIGEN" um die Antwort zu enthüllen
• Klicke dann "✅ Spieler X" wenn jemand richtig geantwortet hat
• Oder "❌ NIEMAND" wenn niemand die Antwort wusste
• Punkte werden automatisch vergeben und angezeigt
• Mit "🔄 RESET" startet das Spiel neu

SPIELERNAMEN ÄNDERN
-------------------
• Klicke den Button "✏️ Namen" oben rechts im Spiel
• Oder bearbeite in JeopardyGame.java das Array PLAYER_NAMES:
    private static final String[] PLAYER_NAMES = {
        "Team 1",    ← hier ändern
        "Team 2",    ← hier ändern
        "Team 3"     ← hier ändern
    };

PUNKTE MANUELL ANPASSEN
------------------------
• Jeder Spieler hat kleine +/− Buttons neben seinem Punktestand
• +100 / −100 pro Klick (nützlich falls mal ein Fehler passiert)

FRAGEN ANPASSEN
---------------
Öffne JeopardyGame.java in BlueJ (Doppelklick auf die Klasse).
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

SYSTEMVORAUSSETZUNGEN
---------------------
• Java 8 oder höher (BlueJ bringt Java mit)
• BlueJ Version 4.x oder höher
• Keine weiteren Abhängigkeiten oder Installationen nötig

============================================================
  Viel Erfolg bei deiner Aktivierungsphase! 🎉
============================================================

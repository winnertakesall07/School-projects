import javax.sound.sampled.*;
import java.io.File;

/**
 * ============================================================
 *  SoundManager – Audio engine for the Jeopardy game.
 * ============================================================
 *
 *  HOW TO ADD SOUNDS
 *  -----------------
 *  1. Obtain audio files in WAV format (see free sources below).
 *  2. Place them inside the  sounds/  folder next to this file.
 *  3. Name them EXACTLY as listed below:
 *
 *       sounds/background.wav  → looping background music
 *       sounds/question.wav    → plays when a question tile is opened
 *       sounds/correct.wav     → plays when a correct answer is given
 *       sounds/wrong.wav       → plays when nobody answers / wrong answer
 *       sounds/reveal.wav      → plays when the answer is revealed
 *       sounds/winner.wav      → plays at end of game
 *
 *  The game runs fine WITHOUT any sound files – they are all optional.
 *
 *  FREE SOUND SOURCES
 *  ------------------
 *   • https://freesound.org        (free account required)
 *   • https://mixkit.co/free-sound-effects/
 *   • https://zapsplat.com         (free account required)
 *
 *  TIPS FOR GREAT ATMOSPHERE
 *  -------------------------
 *   • Background: a calm orchestral or ambient loop (~30 sec)
 *   • Correct:    a short fanfare or "ding" (~2 sec)
 *   • Wrong:      a buzzer or "wah-wah" (~1-2 sec)
 *   • Winner:     triumphant fanfare (~5-10 sec)
 *
 *  NOTE: Only 16-bit PCM WAV files are guaranteed to work with
 *  Java's built-in audio system. Use Audacity (free) to convert
 *  MP3 → WAV if needed: File → Export → Export as WAV.
 * ============================================================
 */
public class SoundManager {

    // ── File paths ──────────────────────────────────────────
    private static final String SOUNDS_DIR    = "sounds/";
    private static final String BG_MUSIC      = SOUNDS_DIR + "background.wav";
    private static final String QUESTION_SFX  = SOUNDS_DIR + "question.wav";
    private static final String CORRECT_SFX   = SOUNDS_DIR + "correct.wav";
    private static final String WRONG_SFX     = SOUNDS_DIR + "wrong.wav";
    private static final String REVEAL_SFX    = SOUNDS_DIR + "reveal.wav";
    private static final String WINNER_SFX    = SOUNDS_DIR + "winner.wav";
    // ────────────────────────────────────────────────────────

    private Clip backgroundClip;
    private boolean enabled = true;

    // ── Public controls ─────────────────────────────────────

    public void playBackgroundMusic() {
        if (!enabled) return;
        try {
            Clip clip = loadClip(BG_MUSIC);
            if (clip != null) {
                backgroundClip = clip;
                FloatControl vol = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                vol.setValue(-10.0f); // slightly quieter for background
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
            }
        } catch (Exception ignored) { /* no sound file – silent mode */ }
    }

    public void stopBackgroundMusic() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
        }
    }

    public void resumeBackgroundMusic() {
        if (backgroundClip != null && !backgroundClip.isRunning()) {
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();
        }
    }

    public void playQuestionSound()  { playOnce(QUESTION_SFX); }
    public void playCorrectSound()   { stopBackgroundMusic(); playOnce(CORRECT_SFX); scheduleResume(2500); }
    public void playWrongSound()     { playOnce(WRONG_SFX); }
    public void playRevealSound()    { playOnce(REVEAL_SFX); }
    public void playWinnerSound()    { stopBackgroundMusic(); playOnce(WINNER_SFX); }

    /** Toggle all sound on/off at runtime. */
    public void setSoundEnabled(boolean on) {
        enabled = on;
        if (!on) stopBackgroundMusic();
        else     playBackgroundMusic();
    }

    public boolean isSoundEnabled() { return enabled; }

    // ── Private helpers ─────────────────────────────────────

    private void playOnce(String path) {
        if (!enabled) return;
        try {
            Clip clip = loadClip(path);
            if (clip != null) clip.start();
        } catch (Exception ignored) {}
    }

    private void scheduleResume(int delayMs) {
        new Thread(() -> {
            try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
            resumeBackgroundMusic();
        }).start();
    }

    private Clip loadClip(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) return null;
            AudioInputStream ais = AudioSystem.getAudioInputStream(f);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (Exception e) {
            return null;
        }
    }
}

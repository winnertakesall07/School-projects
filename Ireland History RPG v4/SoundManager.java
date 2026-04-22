import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.Random;

/**
 * Manages all audio for Éire's Stand v3.
 *
 * Background music: programmatically generated MIDI sequences per chapter.
 *   Chapter 0 — Dark medieval drone (string ensemble, minor key)
 *   Chapter 1 — Military march (trumpet + snare)
 *   Chapter 2 — Tense rising tension (strings, driving rhythm)
 *
 * Sound effects: brief synthesised PCM clips fired on a daemon thread.
 *   "shoot"  — player/ally fires a ranged shot
 *   "sword"  — player swings melee weapon
 *   "hit"    — entity takes damage
 *   "reload" — weapon ready
 *
 * All audio failures are silently swallowed so the game runs on systems
 * without a sound device.
 *
 * Call setMuted(true) to silence everything; the state persists across chapters.
 */
public class SoundManager {

    private static final int PPQ = 24;   // pulses per quarter note (MIDI resolution)

    private Sequencer    sequencer;
    private Synthesizer  synthesizer;
    private boolean      muted   = false;
    private int          currentChapter = -1;
    private final Random rng = new Random();

    // ── Initialisation ────────────────────────────────────────────────────────

    public SoundManager() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            sequencer   = MidiSystem.getSequencer(false);
            sequencer.open();
            // Wire sequencer output into the synthesizer
            Transmitter trans  = sequencer.getTransmitter();
            Receiver    recv   = synthesizer.getReceiver();
            trans.setReceiver(recv);
        } catch (MidiUnavailableException e) {
            sequencer   = null;
            synthesizer = null;
        }
    }

    // ── Music control ─────────────────────────────────────────────────────────

    /**
     * Start the background music appropriate for the given chapter.
     * Does nothing if already playing that chapter's music.
     */
    public void playChapterMusic(int chapter) {
        if (muted || sequencer == null) return;
        if (chapter == currentChapter && sequencer.isRunning()) return;
        currentChapter = chapter;
        try {
            sequencer.stop();
            Sequence seq;
            switch (chapter) {
                case 0:  seq = buildMedievalSequence(); break;
                case 1:  seq = buildMarchSequence();    break;
                default: seq = buildTenseSequence();    break;
            }
            sequencer.setSequence(seq);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.setMicrosecondPosition(0);
            sequencer.start();
        } catch (Exception e) { /* silently ignore */ }
    }

    public void stopMusic() {
        if (sequencer != null && sequencer.isRunning()) sequencer.stop();
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) stopMusic();
        else if (currentChapter >= 0) playChapterMusic(currentChapter);
    }

    public boolean isMuted() { return muted; }

    // ── Sound effects ─────────────────────────────────────────────────────────

    /** Fire-and-forget sound effect on a daemon thread. */
    public void playSound(String type) {
        if (muted || synthesizer == null) return;
        Thread t = new Thread(() -> playSoundInternal(type));
        t.setDaemon(true);
        t.start();
    }

    private void playSoundInternal(String type) {
        try {
            MidiChannel[] ch = synthesizer.getChannels();
            if (ch == null || ch.length == 0) return;
            switch (type) {
                case "shoot":
                    // High-pitched short percussion burst (channel 9 = percussion)
                    ch[9].noteOn(40, 110);   // Electric Snare
                    Thread.sleep(60);
                    ch[9].noteOff(40);
                    break;
                case "sword":
                    // Quick metallic hit
                    ch[9].noteOn(56, 90);    // Cowbell (metallic)
                    Thread.sleep(50);
                    ch[9].noteOff(56);
                    break;
                case "hit":
                    // Low thud
                    ch[9].noteOn(36, 100);   // Bass Drum 1
                    Thread.sleep(40);
                    ch[9].noteOff(36);
                    break;
                case "ally_cry":
                    // Short horn-like note
                    ch[0].programChange(60);  // French Horn
                    ch[0].noteOn(55, 80);
                    Thread.sleep(120);
                    ch[0].noteOff(55);
                    break;
                case "enemy_cry":
                    // Short low brass note
                    ch[1].programChange(57);  // Trombone
                    ch[1].noteOn(48, 80);
                    Thread.sleep(120);
                    ch[1].noteOff(48);
                    break;
                case "chapter_win":
                    // Triumphant fanfare
                    ch[0].programChange(56);  // Trumpet
                    int[] fanfare = {60, 64, 67, 72};
                    for (int note : fanfare) {
                        ch[0].noteOn(note, 100);
                        Thread.sleep(150);
                        ch[0].noteOff(note);
                        Thread.sleep(20);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) { /* silently ignore */ }
    }

    // ── MIDI sequence builders ─────────────────────────────────────────────────

    /**
     * Chapter 0 — Dark medieval drone.
     * String ensemble in D-minor, slow ominous feel.
     */
    private Sequence buildMedievalSequence() throws InvalidMidiDataException {
        Sequence seq   = new Sequence(Sequence.PPQ, PPQ);
        Track    track = seq.createTrack();

        programChange(track, 0, 48, 0); // String Ensemble 1

        // D-minor chord arpeggio: D3(50), F3(53), A3(57), C4(60)
        int[] melody = { 50, 53, 57, 60, 57, 53, 50, 48 };
        int[] durs   = {  2,  2,  2,  2,  2,  2,  2,  4 };
        int tick = 0;
        // Two passes to fill about 8 bars
        for (int pass = 0; pass < 4; pass++) {
            for (int i = 0; i < melody.length; i++) {
                int dur = durs[i] * PPQ / 2;
                addNote(track, 0, melody[i], 70, tick, dur);
                tick += dur;
            }
        }
        // Low bass drone on D2 throughout
        addNote(track, 1, 38, 55, 0, tick);
        programChange(track, 1, 32, 0); // Acoustic Bass
        setTempo(track, 800_000); // ~75 BPM (slow and menacing)
        return seq;
    }

    /**
     * Chapter 1 — British military march.
     * Trumpet melody + snare drum rhythm.
     */
    private Sequence buildMarchSequence() throws InvalidMidiDataException {
        Sequence seq   = new Sequence(Sequence.PPQ, PPQ);
        Track    melody = seq.createTrack();
        Track    drums  = seq.createTrack();

        programChange(melody, 0, 56, 0); // Trumpet

        // C major march phrase: C4, E4, G4, C5, G4, E4, C4 ...
        int[] notes = { 60, 64, 67, 72, 67, 64, 60, 62, 64, 65, 64, 62, 60 };
        int[] durs  = {  1,  1,  1,  2,  1,  1,  2,  1,  1,  1,  1,  1,  2 };
        int tick = 0;
        for (int pass = 0; pass < 3; pass++) {
            for (int i = 0; i < notes.length; i++) {
                int dur = durs[i] * PPQ / 2;
                addNote(melody, 0, notes[i], 90, tick, dur);
                tick += dur;
            }
        }

        // Snare + bass drum pattern per quarter note
        int totalTicks = tick;
        for (int t = 0; t < totalTicks; t += PPQ) {
            addNote(drums,  9, 36, 100, t, PPQ / 4);      // Bass drum on beat 1
            addNote(drums,  9, 38,  90, t + PPQ / 2, PPQ / 4); // Snare on beat 2
        }

        setTempo(melody, 500_000); // 120 BPM march
        return seq;
    }

    /**
     * Chapter 2 — Rising tension.
     * Strings in D-minor, driving eighth-note pattern.
     */
    private Sequence buildTenseSequence() throws InvalidMidiDataException {
        Sequence seq   = new Sequence(Sequence.PPQ, PPQ);
        Track    track = seq.createTrack();
        Track    bass  = seq.createTrack();

        programChange(track, 0, 48, 0);  // String Ensemble
        programChange(bass,  2, 32, 0);  // Acoustic Bass

        // Driving D-minor ascending pattern: D4, E4, F4, G4, A4, Bb4, C5, D5
        int[] pattern = { 62, 64, 65, 67, 69, 70, 72, 74, 72, 70, 69, 67, 65, 64 };
        int tick = 0;
        int noteDur = PPQ / 3;  // short staccato notes
        for (int pass = 0; pass < 6; pass++) {
            for (int note : pattern) {
                addNote(track, 0, note, 85 + (pass % 3) * 8, tick, noteDur);
                tick += noteDur + 2;
            }
        }

        // Bass ostinato on D2-A2
        int[] bassNotes = { 38, 45, 38, 45, 38, 45, 40, 45 };
        int bassTick = 0;
        int bassDur  = PPQ / 2;
        while (bassTick < tick) {
            for (int bn : bassNotes) {
                if (bassTick >= tick) break;
                addNote(bass, 2, bn, 80, bassTick, bassDur);
                bassTick += bassDur;
            }
        }

        setTempo(track, 428_571); // ~140 BPM tense
        return seq;
    }

    // ── MIDI helper utilities ─────────────────────────────────────────────────

    private void addNote(Track track, int channel, int note,
                         int velocity, int startTick, int durationTicks) {
        try {
            ShortMessage on  = new ShortMessage(ShortMessage.NOTE_ON,  channel, note, velocity);
            ShortMessage off = new ShortMessage(ShortMessage.NOTE_OFF, channel, note, 0);
            track.add(new MidiEvent(on,  startTick));
            track.add(new MidiEvent(off, startTick + Math.max(1, durationTicks)));
        } catch (InvalidMidiDataException ignored) {}
    }

    private void programChange(Track track, int channel, int program, int tick) {
        try {
            ShortMessage pc = new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, program, 0);
            track.add(new MidiEvent(pc, tick));
        } catch (InvalidMidiDataException ignored) {}
    }

    private void setTempo(Track track, int microsecondsPerBeat) {
        try {
            // Meta message type 0x51 = Set Tempo
            byte[] data = {
                (byte)((microsecondsPerBeat >> 16) & 0xFF),
                (byte)((microsecondsPerBeat >>  8) & 0xFF),
                (byte)( microsecondsPerBeat        & 0xFF)
            };
            MetaMessage mm = new MetaMessage(0x51, data, 3);
            track.add(new MidiEvent(mm, 0));
        } catch (InvalidMidiDataException ignored) {}
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    public void close() {
        if (sequencer   != null && sequencer.isOpen())   sequencer.close();
        if (synthesizer != null && synthesizer.isOpen()) synthesizer.close();
    }
}

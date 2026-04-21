/**
 * Holds data for a single story page (chapter narrative screen).
 */
public class StoryPage {
    public final String chapterLabel;  // e.g. "CHAPTER I  •  1169 A.D."
    public final String title;         // large heading
    public final String text;          // body paragraph
    public final String vocabNote;     // key vocabulary note shown at the bottom

    public StoryPage(String chapterLabel, String title, String text, String vocabNote) {
        this.chapterLabel = chapterLabel;
        this.title        = title;
        this.text         = text;
        this.vocabNote    = vocabNote;
    }
}

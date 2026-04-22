import java.util.ArrayList;
import java.util.List;

/**
 * Builds and manages the story pages for each of the three chapters.
 * All required English-class vocabulary is woven naturally into the text.
 */
public class StoryManager {

    private final List<StoryPage> pages = new ArrayList<>();
    private int currentIndex = 0;

    /** Load narrative pages for the given chapter (0, 1, or 2). */
    public void loadChapter(int chapter) {
        pages.clear();
        currentIndex = 0;
        switch (chapter) {
            case 0: buildChapter0(); break;
            case 1: buildChapter1(); break;
            case 2: buildChapter2(); break;
        }
    }

    // ── Chapter I — The Norman Conquest (1169) ────────────────────────────────

    private void buildChapter0() {
        pages.add(new StoryPage(
            "CHAPTER I  \u2022  1169 A.D.",
            "The Norman Conquest",
            "The year 1169 was to be the starting point of centuries of foreign domination " +
            "over Ireland. When Norman forces first landed on Irish shores, their intent was " +
            "clear: to conquer the land and bring it under the control of the English crown. " +
            "The Irish chieftains, divided among themselves, could not form a united defence " +
            "against the armoured invaders pressing inland from the coast.",
            "Key term \u2014 'to be the starting point': to mark the beginning of something significant"
        ));
        pages.add(new StoryPage(
            "CHAPTER I  \u2022  1169 A.D.",
            "Innumerable Battles",
            "Ireland was marked by a proud warrior culture, yet its many kingdoms were " +
            "often at war with one another. The Normans exploited these divisions, conquering " +
            "territory piece by piece across the green hills and boglands. Innumerable battles " +
            "were fought as the bravest clans chose to rebel against the invaders. Without " +
            "unity, however, each uprising was crushed in turn. Today, your clan fights back.",
            "Key terms \u2014 'innumerable': countless | 'to rebel against': to resist with force"
        ));
        pages.add(new StoryPage(
            "CHAPTER I  \u2022  1169 A.D.",
            "The Battle of the Green Hills",
            "Your clan brothers stand ready beside you on the fog-shrouded hillside. " +
            "The Norman knights advance in formation from their stone fortification. " +
            "Their commander rides at the front, armoured in steel. " +
            "Your allies can harry the flanks, but only you can break their commander. " +
            "The fate of your people depends on your sword arm today.",
            "Battle tip \u2014 Defeat the COMMANDER (gold health bar) to break enemy morale!"
        ));
    }

    // ── Chapter II — British Rule & Home Rule (1800–1916) ────────────────────

    private void buildChapter1() {
        pages.add(new StoryPage(
            "CHAPTER II  \u2022  1800 A.D.",
            "The Act of Union",
            "Centuries of colonial rule culminated in the British government's decision to " +
            "forcibly merge Ireland with Great Britain through the Act of Union in 1800. " +
            "The Irish Parliament was dissolved and Ireland was henceforth governed directly " +
            "from Westminster. Many Irish people were outraged \u2014 their nation had been " +
            "absorbed by force, their political voice silenced, and their land marked by " +
            "poverty and deliberate neglect.",
            "Key term \u2014 'to forcibly merge': to combine by compulsion, against the will of the people"
        ));
        pages.add(new StoryPage(
            "CHAPTER II  \u2022  1800\u20131916 A.D.",
            "Flagrant Discrimination & Home Rule",
            "Life under British rule was marked by flagrant discrimination. Catholics \u2014 the " +
            "vast majority of Ireland's population \u2014 faced laws barring them from office, " +
            "property, and free worship. As the 19th century advanced, the debate over Home " +
            "Rule \u2014 granting Ireland self-governance \u2014 split the island. Unionists, " +
            "loyal to Britain, fiercely opposed any change. Nationalists demanded Home Rule " +
            "as their birthright. The clash between unionists and nationalists defined the age.",
            "Key terms \u2014 'flagrant discrimination' | 'Home Rule' | 'unionists vs nationalists'"
        ));
        pages.add(new StoryPage(
            "CHAPTER II  \u2022  1803 A.D.",
            "Robert Emmet's Rebellion",
            "Inspired by the United Irishmen, you join Robert Emmet's rebels in the streets " +
            "of Dublin. British redcoats have fortified their barracks and begun rounding up " +
            "rebels. A Sergeant \u2014 their COMMANDER \u2014 is coordinating the crackdown. " +
            "Your fellow rebels will engage the regular soldiers, but the Sergeant must fall " +
            "to you alone. His capture will rally the people and drive back the redcoats.",
            "Battle tip \u2014 The COMMANDER Sergeant (gold bar) ignores allies \u2014 he is YOUR target!"
        ));
    }

    // ── Chapter III — Easter Rising & Civil War (1916–1923) ──────────────────

    private void buildChapter2() {
        pages.add(new StoryPage(
            "CHAPTER III  \u2022  1916 A.D.",
            "The Easter Rising",
            "On Easter Monday 1916, Irish nationalists chose to rebel against British rule in " +
            "a bold uprising at the heart of Dublin. The British response was devastating \u2014 " +
            "shelling the city centre and executing the Rising's leaders. Yet rather than " +
            "crushing the movement, these actions ignited the War of Independence, driving " +
            "innumerable ordinary Irish citizens to take up the cause of freedom at last.",
            "Key terms \u2014 'to rebel against' | 'devastating': causing enormous destruction or grief"
        ));
        pages.add(new StoryPage(
            "CHAPTER III  \u2022  1922\u20131923 A.D.",
            "The Civil War",
            "Victory over Britain brought no peace. The Anglo-Irish Treaty of 1922 divided " +
            "Ireland into civil war-like factions: those who accepted the Free State and those " +
            "who rejected it outright. The conflict was devastating \u2014 tearing families and " +
            "communities apart. Unionists in the north aligned firmly with Britain, deepening " +
            "the divide. The struggle between unionists and nationalists, between faction and " +
            "faction, would echo across Ireland for generations to come.",
            "Key terms \u2014 'civil war-like' | 'faction': a divided group | 'unionists vs nationalists'"
        ));
        pages.add(new StoryPage(
            "CHAPTER III  \u2022  1916 A.D.",
            "The Battle for the GPO",
            "You stand inside the bullet-scarred General Post Office. IRA fighters hold the " +
            "ground floor while Black-and-Tan officers direct their assault from the street. " +
            "Their Officer COMMANDER controls the assault \u2014 your comrades cannot reach him " +
            "through the smoke and rubble. Fight your way through the Tans, eliminate the " +
            "Officer, and Ireland's struggle may yet survive this devastating day.",
            "Battle tip \u2014 IRA allies fight bravely but need YOU to take down the Officer COMMANDER!"
        ));
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    public StoryPage getCurrentPage() {
        if (pages.isEmpty() || currentIndex >= pages.size()) return null;
        return pages.get(currentIndex);
    }

    public boolean hasMorePages() {
        return currentIndex < pages.size() - 1;
    }

    public void nextPage() {
        if (currentIndex < pages.size() - 1) currentIndex++;
    }
}

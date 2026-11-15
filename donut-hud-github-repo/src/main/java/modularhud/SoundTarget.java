package modularhud;

import java.util.ArrayList;
import java.util.List;

/** Things the sound detector can box. Blocks are things that make ongoing/ambient sound. */
public enum SoundTarget {
    FURNACES(true, "Furnaces", 0xFFFF9A3C),     // lit furnace / blast furnace / smoker
    CAMPFIRES(true, "Campfires", 0xFFFFC857),   // lit campfire / soul campfire
    PORTALS(true, "Portals", 0xFFB04DFF),       // nether portal
    BEES(true, "Bee nests", 0xFFFFE14D),        // bee nest / beehive
    BEACONS(true, "Beacons", 0xFF5CF2FF),       // beacon / conduit
    JUKEBOXES(true, "Jukeboxes", 0xFFFF6FB5),   // jukebox with a disc
    SCULK(true, "Sculk", 0xFF2AD1C9),           // sensors, shriekers, catalysts
    NOTES(true, "Note/Bells", 0xFF8CFF6F),      // note blocks, bells

    PLAYERS(false, "Players", 0xFFFF4D4D),
    HOSTILE(false, "Hostile", 0xFFFF7A59),
    ANIMALS(false, "Animals", 0xFF7CFF7C),
    VILLAGERS(false, "Villagers", 0xFFC9A66B),
    MINECARTS(false, "Minecarts", 0xFFA0A0A0);

    public final boolean block;
    public final String label;
    public final int color;

    SoundTarget(boolean block, String label, int color) {
        this.block = block;
        this.label = label;
        this.color = color;
    }

    public static List<SoundTarget> blocks() {
        List<SoundTarget> l = new ArrayList<>();
        for (SoundTarget t : values()) if (t.block) l.add(t);
        return l;
    }

    public static List<SoundTarget> entities() {
        List<SoundTarget> l = new ArrayList<>();
        for (SoundTarget t : values()) if (!t.block) l.add(t);
        return l;
    }
}

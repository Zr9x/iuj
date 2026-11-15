package modularhud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HudConfig {
    public enum Anchor {
        TOP_LEFT("Top Left"), TOP_RIGHT("Top Right"),
        BOTTOM_LEFT("Bottom Left"), BOTTOM_RIGHT("Bottom Right");

        public final String label;
        Anchor(String label) { this.label = label; }

        public Anchor next() {
            Anchor[] v = values();
            return v[(ordinal() + 1) % v.length];
        }
    }

    public static final class Entry {
        public boolean enabled = true;
        public Anchor anchor = Anchor.TOP_LEFT;

        public Entry() {}
        public Entry(boolean enabled, Anchor anchor) {
            this.enabled = enabled;
            this.anchor = anchor;
        }
    }

    public static final int MIN_RANGE = 1, MAX_RANGE = 16;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("modularhud.json");
    private static HudConfig instance;

    public Map<String, Entry> modules = new LinkedHashMap<>();

    // ---- sound detector ----
    public boolean soundEnabled = false;
    public int soundRange = 6;                 // in chunks
    public boolean soundLabels = true;
    public Map<String, Boolean> soundTargets = new LinkedHashMap<>();
    public Map<String, Integer> soundColors = new LinkedHashMap<>();   // per-target colour overrides
    public boolean soundSummary = true;        // side list: counts + nearest distance per type
    public boolean soundArrows = true;         // edge arrows toward off-screen targets
    public boolean soundTracers = false;       // line from screen centre to each target

    // ---- block / entity ESP ----
    public boolean espBlocksEnabled = false;
    public boolean espEntitiesEnabled = false;
    public boolean espLabels = true;
    public boolean espTracers = false;
    public int espRange = 4;                   // in chunks
    public Map<String, Integer> espBlocks = defaultBlocks();      // block id -> ARGB colour
    public Map<String, Integer> espEntities = defaultEntities();  // entity id -> ARGB colour

    private static Map<String, Integer> defaultBlocks() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("minecraft:diamond_ore", 0xFF5CF2FF);
        m.put("minecraft:deepslate_diamond_ore", 0xFF5CF2FF);
        m.put("minecraft:ancient_debris", 0xFFB5651D);
        m.put("minecraft:spawner", 0xFFFF4D4D);
        return m;
    }

    private static Map<String, Integer> defaultEntities() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("minecraft:player", 0xFFFF4D4D);
        return m;
    }

    public int colorOf(SoundTarget t) {
        if (soundColors == null) soundColors = new LinkedHashMap<>();
        return soundColors.getOrDefault(t.name(), t.color);
    }

    public void setColor(SoundTarget t, int argb) {
        if (soundColors == null) soundColors = new LinkedHashMap<>();
        soundColors.put(t.name(), argb);
    }

    public int espRange() {
        return Math.max(MIN_RANGE, Math.min(MAX_RANGE, espRange));
    }

    public boolean targetOn(SoundTarget t) {
        if (soundTargets == null) soundTargets = new LinkedHashMap<>();
        return soundTargets.getOrDefault(t.name(), true);
    }

    public void setTarget(SoundTarget t, boolean on) {
        if (soundTargets == null) soundTargets = new LinkedHashMap<>();
        soundTargets.put(t.name(), on);
    }

    public int range() {
        return Math.max(MIN_RANGE, Math.min(MAX_RANGE, soundRange));
    }

    public static HudConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static HudConfig load() {
        if (Files.exists(PATH)) {
            try {
                HudConfig cfg = GSON.fromJson(Files.readString(PATH), HudConfig.class);
                if (cfg != null && cfg.modules != null) {
                    if (cfg.espBlocks == null) cfg.espBlocks = defaultBlocks();
                    if (cfg.espEntities == null) cfg.espEntities = defaultEntities();
                    if (cfg.soundColors == null) cfg.soundColors = new LinkedHashMap<>();
                    return cfg;
                }
            } catch (Exception ignored) {
                // fall through to defaults on a corrupt file
            }
        }
        return new HudConfig();
    }

    public Entry entry(HudModule module) {
        Entry e = modules.computeIfAbsent(module.id(),
                k -> new Entry(module.defaultEnabled(), module.defaultAnchor()));
        if (e.anchor == null) {
            e.anchor = module.defaultAnchor();
        }
        return e;
    }

    public static void save() {
        try {
            Files.writeString(PATH, GSON.toJson(get()));
        } catch (IOException e) {
            System.err.println("[modularhud] Failed to save config: " + e.getMessage());
        }
    }
}

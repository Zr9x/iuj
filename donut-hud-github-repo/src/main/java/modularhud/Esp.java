package modularhud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_2248;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_332;

/** Block ESP + entity ESP: wireframe outlines for the blocks / entities picked in the selector screens. */
public final class Esp {
    private record BlockHit(String id, int x, int y, int z) {}
    private record Item(String id, double minX, double minY, double minZ,
                        double maxX, double maxY, double maxZ, double dist, int color) {}

    private static final int MAX_ITEMS = 150;
    private static final ChunkScanner<BlockHit> scanner = new ChunkScanner<>(Esp::scanChunk, 6);
    private static final Map<class_2248, String> idCache = new IdentityHashMap<>();
    private static final List<class_1297> ents = new ArrayList<>();

    public static int blockCount, entityCount;

    private Esp() {}

    // ------------------------------------------------------------------ ids

    /** Block.toString() is "Block{minecraft:stone}", which avoids needing registry method names. */
    private static String blockId(class_2248 b) {
        return idCache.computeIfAbsent(b, k -> {
            String s = k.toString();
            int i = s.indexOf('{'), j = s.lastIndexOf('}');
            return i >= 0 && j > i ? s.substring(i + 1, j) : s;
        });
    }

    /** EntityType.toString() is its translation key, e.g. "entity.minecraft.zombie" -> "minecraft:zombie". */
    private static String entityId(class_1297 e) {
        String k = e.method_5864().toString();
        if (k.startsWith("entity.")) k = k.substring(7);
        int i = k.indexOf('.');
        return i >= 0 ? k.substring(0, i) + ":" + k.substring(i + 1) : k;
    }

    /** "minecraft:diamond_ore" -> "Diamond Ore". */
    public static String pretty(String id) {
        String s = id.substring(id.indexOf(':') + 1).replace('_', ' ').replace('.', ' ');
        StringBuilder sb = new StringBuilder();
        for (String w : s.split(" ")) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(w.substring(0, 1).toUpperCase(Locale.ROOT)).append(w.substring(1));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ scanning

    private static boolean matches(class_2680 s) {
        return HudConfig.get().espBlocks.containsKey(blockId(s.method_26204()));
    }

    private static List<BlockHit> scanChunk(class_2818 chunk) {
        List<BlockHit> out = new ArrayList<>();
        class_2826[] sections = chunk.method_12006();
        int bottom = chunk.method_32891();
        int startX = chunk.method_12004().method_8326();
        int startZ = chunk.method_12004().method_8328();
        Map<String, Integer> wanted = HudConfig.get().espBlocks;
        for (int i = 0; i < sections.length; i++) {
            class_2826 sec = sections[i];
            if (sec == null || sec.method_38292() || !sec.method_19523(Esp::matches)) continue;
            int baseY = (bottom + i) << 4;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        String id = blockId(sec.method_12254(x, y, z).method_26204());
                        if (wanted.containsKey(id)) out.add(new BlockHit(id, startX + x, baseY + y, startZ + z));
                    }
                }
            }
        }
        return out;
    }

    /** Call after the selection changes so cached chunks are rescanned. */
    public static void invalidate() {
        scanner.invalidate();
    }

    public static void tick(class_310 mc) {
        HudConfig cfg = HudConfig.get();
        if (mc.field_1724 == null || mc.field_1687 == null) {
            scanner.clear();
            ents.clear();
            return;
        }
        if (cfg.espBlocksEnabled) {
            scanner.tick(mc, cfg.espRange());
        } else if (!scanner.cache.isEmpty()) {
            scanner.clear();
        }

        ents.clear();
        if (cfg.espEntitiesEnabled) {
            double maxD = cfg.espRange() * 16.0, maxD2 = maxD * maxD;
            for (class_1297 e : mc.field_1687.method_18112()) {
                if (e == mc.field_1724 || e.method_31481()) continue;
                double dx = e.method_23317() - mc.field_1724.method_23317();
                double dz = e.method_23321() - mc.field_1724.method_23321();
                if (dx * dx + dz * dz > maxD2) continue;
                if (cfg.espEntities.containsKey(entityId(e))) ents.add(e);
            }
        }
    }

    // ------------------------------------------------------------------ rendering

    public static void render(class_332 ctx, class_310 mc) {
        HudConfig cfg = HudConfig.get();
        if (!cfg.espBlocksEnabled && !cfg.espEntitiesEnabled) {
            blockCount = entityCount = 0;
            return;
        }
        if (!Projector.begin(ctx, mc)) return;

        List<Item> list = new ArrayList<>();
        int bc = 0, ec = 0;
        if (cfg.espBlocksEnabled) {
            for (List<BlockHit> hits : scanner.cache.values()) {
                for (BlockHit h : hits) {
                    Integer c = cfg.espBlocks.get(h.id());
                    if (c == null) continue;
                    double d = Math.sqrt(sq(h.x() + 0.5 - Projector.ex) + sq(h.y() + 0.5 - Projector.ey) + sq(h.z() + 0.5 - Projector.ez));
                    list.add(new Item(h.id(), h.x(), h.y(), h.z(), h.x() + 1, h.y() + 1, h.z() + 1, d, c));
                    bc++;
                }
            }
        }
        for (class_1297 e : ents) {
            if (e.method_31481()) continue;
            Integer c = cfg.espEntities.get(entityId(e));
            if (c == null) continue;
            class_238 b = e.method_5829();
            double d = Math.sqrt(sq((b.field_1323 + b.field_1320) / 2 - Projector.ex)
                    + sq((b.field_1322 + b.field_1325) / 2 - Projector.ey)
                    + sq((b.field_1321 + b.field_1324) / 2 - Projector.ez));
            list.add(new Item(entityId(e), b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324, d, c));
            ec++;
        }
        blockCount = bc;
        entityCount = ec;

        list.sort(Comparator.comparingDouble(Item::dist));
        List<Item> drawn = list.size() > MAX_ITEMS ? list.subList(0, MAX_ITEMS) : list;
        double[] cam = new double[3], scr = new double[2];
        for (int i = drawn.size() - 1; i >= 0; i--) {
            Item it = drawn.get(i);
            Projector.wireBox(ctx, it.minX(), it.minY(), it.minZ(), it.maxX(), it.maxY(), it.maxZ(), it.color());

            double cx = (it.minX() + it.maxX()) / 2, cy = (it.minY() + it.maxY()) / 2, cz = (it.minZ() + it.maxZ()) / 2;
            Projector.toCam(cx, cy, cz, cam);
            if (cam[2] <= 0.1) continue;
            Projector.toScreen(cam, scr);
            if (cfg.espTracers) {
                Projector.clippedLine(ctx, Projector.sw / 2.0, Projector.sh / 2.0, scr[0], scr[1],
                        (it.color() & 0x00FFFFFF) | 0x70000000);
            }
            if (cfg.espLabels && scr[0] >= 0 && scr[0] <= Projector.sw && scr[1] >= 0 && scr[1] <= Projector.sh) {
                SoundDetector.drawLabel(ctx, mc, pretty(it.id()) + " " + (int) it.dist() + "m",
                        (int) scr[0], (int) scr[1] - 14, it.color());
            }
        }
    }

    private static double sq(double v) { return v * v; }
}

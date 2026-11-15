package modularhud;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1296;
import net.minecraft.class_1297;
import net.minecraft.class_1480;
import net.minecraft.class_1569;
import net.minecraft.class_1657;
import net.minecraft.class_1688;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_238;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3988;

/**
 * Finds blocks/entities that make sound within a chunk range.
 *
 * Depth over the old version: adjacent blocks of one type are merged into a single group (one outline,
 * "x3" count), outlines are real 3D wireframes, distance fades the colour, off-screen targets get edge
 * arrows, optional tracers, vertical offset (up/down) in labels, and a side summary of counts + nearest.
 */
public final class SoundDetector {
    private record Hit(SoundTarget target, int x, int y, int z) {}
    private record Group(SoundTarget target, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int count) {}
    private record EntHit(class_1297 entity, SoundTarget target) {}
    private record Item(SoundTarget target, double minX, double minY, double minZ,
                        double maxX, double maxY, double maxZ, int count, double dist, double dy) {}

    private static final int CHUNKS_PER_TICK = 8;
    private static final int MAX_ITEMS = 60;

    private static final ChunkScanner<Group> scanner = new ChunkScanner<>(SoundDetector::scanChunk, CHUNKS_PER_TICK);
    private static final List<EntHit> entities = new ArrayList<>();

    /** Read by the Multitask HUD module. */
    public static int totalCount;
    public static String nearestText = "";

    private SoundDetector() {}

    // ------------------------------------------------------------------ classification

    private static boolean lit(class_2680 s) {
        return s.method_28498(class_2741.field_12548) && s.method_11654(class_2741.field_12548);
    }

    static SoundTarget classify(class_2680 s) {
        class_2248 b = s.method_26204();
        if (b == class_2246.field_10181 || b == class_2246.field_16333 || b == class_2246.field_16334) {
            return lit(s) ? SoundTarget.FURNACES : null;
        }
        if (b == class_2246.field_17350 || b == class_2246.field_23860) {
            return lit(s) ? SoundTarget.CAMPFIRES : null;
        }
        if (b == class_2246.field_10316) return SoundTarget.PORTALS;
        if (b == class_2246.field_20421 || b == class_2246.field_20422) return SoundTarget.BEES;
        if (b == class_2246.field_10327 || b == class_2246.field_10502) return SoundTarget.BEACONS;
        if (b == class_2246.field_10223) {
            return s.method_28498(class_2741.field_12544) && s.method_11654(class_2741.field_12544)
                    ? SoundTarget.JUKEBOXES : null;
        }
        if (b == class_2246.field_28108 || b == class_2246.field_43231
                || b == class_2246.field_37571 || b == class_2246.field_37570) {
            return SoundTarget.SCULK;
        }
        if (b == class_2246.field_10179 || b == class_2246.field_16332) return SoundTarget.NOTES;
        return null;
    }

    private static boolean matches(class_2680 s) {
        return classify(s) != null;
    }

    private static SoundTarget classify(class_1297 e) {
        if (e instanceof class_1657) return SoundTarget.PLAYERS;
        if (e instanceof class_3988) return SoundTarget.VILLAGERS;
        if (e instanceof class_1569) return SoundTarget.HOSTILE;
        if (e instanceof class_1296 || e instanceof class_1480) return SoundTarget.ANIMALS;
        if (e instanceof class_1688) return SoundTarget.MINECARTS;
        return null;
    }

    // ------------------------------------------------------------------ scanning (per tick)

    public static void tick(class_310 mc) {
        HudConfig cfg = HudConfig.get();
        if (mc.field_1724 == null || mc.field_1687 == null) {
            scanner.clear();
            entities.clear();
            return;
        }
        if (!cfg.soundEnabled) {
            if (!scanner.cache.isEmpty() || !entities.isEmpty()) {
                scanner.clear();
                entities.clear();
            }
            return;
        }
        scanner.tick(mc, cfg.range());
        scanEntities(mc, cfg);
    }

    private static List<Group> scanChunk(class_2818 chunk) {
        List<Hit> hits = new ArrayList<>();
        class_2826[] sections = chunk.method_12006();
        int bottom = chunk.method_32891();
        int startX = chunk.method_12004().method_8326();
        int startZ = chunk.method_12004().method_8328();
        for (int i = 0; i < sections.length; i++) {
            class_2826 sec = sections[i];
            if (sec == null || sec.method_38292() || !sec.method_19523(SoundDetector::matches)) continue;
            int baseY = (bottom + i) << 4;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        SoundTarget t = classify(sec.method_12254(x, y, z));
                        if (t != null) hits.add(new Hit(t, startX + x, baseY + y, startZ + z));
                    }
                }
            }
        }
        return group(hits);
    }

    private static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | ((y + 2048) & 0xFFF);
    }

    /** Merges touching (26-neighbour) hits of the same type into one group with a bounding box. */
    private static List<Group> group(List<Hit> hits) {
        Map<Long, Hit> byPos = new HashMap<>();
        for (Hit h : hits) byPos.put(pack(h.x(), h.y(), h.z()), h);
        Set<Long> seen = new HashSet<>();
        List<Group> out = new ArrayList<>();
        for (Hit start : hits) {
            if (!seen.add(pack(start.x(), start.y(), start.z()))) continue;
            ArrayDeque<Hit> queue = new ArrayDeque<>();
            queue.add(start);
            int minX = start.x(), minY = start.y(), minZ = start.z();
            int maxX = minX, maxY = minY, maxZ = minZ, n = 0;
            while (!queue.isEmpty()) {
                Hit h = queue.poll();
                n++;
                minX = Math.min(minX, h.x()); maxX = Math.max(maxX, h.x());
                minY = Math.min(minY, h.y()); maxY = Math.max(maxY, h.y());
                minZ = Math.min(minZ, h.z()); maxZ = Math.max(maxZ, h.z());
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            long k = pack(h.x() + dx, h.y() + dy, h.z() + dz);
                            Hit o = byPos.get(k);
                            if (o != null && o.target() == start.target() && seen.add(k)) queue.add(o);
                        }
                    }
                }
            }
            out.add(new Group(start.target(), minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1, n));
        }
        return out;
    }

    private static void scanEntities(class_310 mc, HudConfig cfg) {
        entities.clear();
        double maxD = cfg.range() * 16.0;
        double maxD2 = maxD * maxD;
        for (class_1297 e : mc.field_1687.method_18112()) {
            if (e == mc.field_1724 || e.method_31481()) continue;
            SoundTarget t = classify(e);
            if (t == null || !cfg.targetOn(t)) continue;
            double dx = e.method_23317() - mc.field_1724.method_23317();
            double dz = e.method_23321() - mc.field_1724.method_23321();
            if (dx * dx + dz * dz > maxD2) continue;
            entities.add(new EntHit(e, t));
        }
    }

    // ------------------------------------------------------------------ rendering (per frame)

    public static void render(class_332 ctx, class_310 mc) {
        HudConfig cfg = HudConfig.get();
        if (!cfg.soundEnabled) {
            totalCount = 0;
            nearestText = "";
            return;
        }
        if (!Projector.begin(ctx, mc)) return;

        double maxD = cfg.range() * 16.0;
        List<Item> list = new ArrayList<>();
        for (List<Group> groups : scanner.cache.values()) {
            for (Group g : groups) {
                if (!cfg.targetOn(g.target())) continue;
                double cx = (g.minX() + g.maxX()) / 2.0, cy = (g.minY() + g.maxY()) / 2.0, cz = (g.minZ() + g.maxZ()) / 2.0;
                double d = Math.sqrt(sq(cx - Projector.ex) + sq(cy - Projector.ey) + sq(cz - Projector.ez));
                list.add(new Item(g.target(), g.minX(), g.minY(), g.minZ(), g.maxX(), g.maxY(), g.maxZ(),
                        g.count(), d, cy - Projector.ey));
            }
        }
        for (EntHit eh : entities) {
            if (eh.entity().method_31481()) continue;
            class_238 b = eh.entity().method_5829();
            double cx = (b.field_1323 + b.field_1320) / 2, cy = (b.field_1322 + b.field_1325) / 2, cz = (b.field_1321 + b.field_1324) / 2;
            double d = Math.sqrt(sq(cx - Projector.ex) + sq(cy - Projector.ey) + sq(cz - Projector.ez));
            list.add(new Item(eh.target(), b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324,
                    1, d, cy - Projector.ey));
        }
        list.sort(Comparator.comparingDouble(Item::dist));

        // stats for the summary + Multitask module (over everything found, not just what is drawn)
        Map<SoundTarget, double[]> stats = new EnumMap<>(SoundTarget.class);
        int total = 0;
        for (Item it : list) {
            double[] s = stats.computeIfAbsent(it.target(), k -> new double[]{0, Double.MAX_VALUE});
            s[0] += it.count();
            s[1] = Math.min(s[1], it.dist());
            total += it.count();
        }
        totalCount = total;
        nearestText = list.isEmpty() ? "" : list.get(0).target().label + " " + (int) list.get(0).dist() + "m";

        List<Item> drawn = list.size() > MAX_ITEMS ? list.subList(0, MAX_ITEMS) : list;
        double[] cam = new double[3], scr = new double[2];
        for (int i = drawn.size() - 1; i >= 0; i--) {   // far -> near so near ones sit on top
            drawItem(ctx, mc, cfg, drawn.get(i), maxD, cam, scr);
        }

        if (cfg.soundSummary) drawSummary(ctx, mc, cfg, stats);
    }

    private static void drawItem(class_332 ctx, class_310 mc, HudConfig cfg, Item it, double maxD,
                                 double[] cam, double[] scr) {
        int base = cfg.colorOf(it.target());
        double fade = Math.max(0.45, Math.min(1.0, 1.0 - it.dist() / (maxD * 1.25)));
        int col = ((int) (255 * fade) << 24) | (base & 0x00FFFFFF);

        double cx = (it.minX() + it.maxX()) / 2, cy = (it.minY() + it.maxY()) / 2, cz = (it.minZ() + it.maxZ()) / 2;
        Projector.toCam(cx, cy, cz, cam);
        boolean ahead = cam[2] > 0.1;
        if (ahead) Projector.toScreen(cam, scr);
        boolean onScreen = ahead && scr[0] >= 0 && scr[0] <= Projector.sw && scr[1] >= 0 && scr[1] <= Projector.sh;

        if (cfg.soundTracers && ahead) {
            Projector.clippedLine(ctx, Projector.sw / 2.0, Projector.sh / 2.0, scr[0], scr[1],
                    (col & 0x00FFFFFF) | 0x70000000);
        }

        if (!onScreen) {
            // the centre may be off-screen while part of a big group is still visible
            Projector.wireBox(ctx, it.minX(), it.minY(), it.minZ(), it.maxX(), it.maxY(), it.maxZ(), col);
            if (cfg.soundArrows) Projector.edgeArrow(ctx, cam, col);
            return;
        }

        Projector.wireBox(ctx, it.minX(), it.minY(), it.minZ(), it.maxX(), it.maxY(), it.maxZ(), col);

        if (cfg.soundLabels) {
            double[] top = new double[2];
            int lx0 = (int) scr[0], ly0 = (int) scr[1];
            if (Projector.project(cx, it.maxY(), cz, top)) {
                lx0 = (int) top[0];
                ly0 = (int) top[1];
            }
            drawLabel(ctx, mc, label(it), lx0, ly0 - 12, col);
        }
    }

    private static String label(Item it) {
        StringBuilder sb = new StringBuilder(it.target().label);
        if (it.count() > 1) sb.append(" x").append(it.count());
        sb.append(' ').append((int) it.dist()).append('m');
        int dy = (int) Math.round(it.dy());
        if (dy >= 3) sb.append(" \u2191").append(dy);
        else if (dy <= -3) sb.append(" \u2193").append(-dy);
        return sb.toString();
    }

    static void drawLabel(class_332 ctx, class_310 mc, String text, int cx, int y, int col) {
        int tw = mc.field_1772.method_1727(text);
        int lx = Math.max(1, Math.min(Projector.sw - tw - 8, cx - tw / 2));
        int ly = Math.max(1, Math.min(Projector.sh - 12, y));
        ctx.method_25294(lx, ly, lx + tw + 7, ly + 11, Theme.BG);
        ctx.method_25294(lx, ly, lx + 2, ly + 11, col);
        ctx.method_25303(mc.field_1772, text, lx + 4, ly + 2, Theme.WHITE);
    }

    /** Left-side list: one row per type found, nearest first. */
    private static void drawSummary(class_332 ctx, class_310 mc, HudConfig cfg, Map<SoundTarget, double[]> stats) {
        if (stats.isEmpty()) return;
        List<Map.Entry<SoundTarget, double[]>> rows = new ArrayList<>(stats.entrySet());
        rows.sort(Comparator.comparingDouble(e -> e.getValue()[1]));
        if (rows.size() > 8) rows = rows.subList(0, 8);

        int w = 0;
        List<String> lines = new ArrayList<>();
        for (Map.Entry<SoundTarget, double[]> e : rows) {
            String s = e.getKey().label + " x" + (int) e.getValue()[0] + "  " + (int) e.getValue()[1] + "m";
            lines.add(s);
            w = Math.max(w, mc.field_1772.method_1727(s));
        }
        int rowH = 11, x = 4, h = rows.size() * rowH + 6;
        int y = Projector.sh / 2 - h / 2;
        ctx.method_25294(x, y, x + w + 14, y + h, Theme.BG);
        for (int i = 0; i < rows.size(); i++) {
            int ry = y + 3 + i * rowH;
            ctx.method_25294(x + 3, ry + 1, x + 6, ry + 8, cfg.colorOf(rows.get(i).getKey()));
            ctx.method_25303(mc.field_1772, lines.get(i), x + 10, ry, Theme.WHITE);
        }
    }

    private static double sq(double v) { return v * v; }
}

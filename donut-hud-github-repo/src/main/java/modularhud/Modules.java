package modularhud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_640;

import static modularhud.HudConfig.Anchor.*;

public final class Modules {
    static final int PAD = 3;
    static final int STRIPE = 2;
    static final int TEXT_H = 9;

    // ---- state used by the tick-based modules ----
    private static final Deque<Long> leftClicks = new ArrayDeque<>();
    private static final Deque<Long> rightClicks = new ArrayDeque<>();
    private static boolean lastLeft, lastRight;
    private static double lastX, lastZ, speed;
    private static boolean hasLast;

    private static void trackClicks(class_310 mc) {
        boolean l = mc.field_1690.field_1886.method_1434();
        boolean r = mc.field_1690.field_1904.method_1434();
        long now = System.currentTimeMillis();
        if (l && !lastLeft) leftClicks.add(now);
        if (r && !lastRight) rightClicks.add(now);
        lastLeft = l;
        lastRight = r;
        prune(leftClicks, now);
        prune(rightClicks, now);
    }

    private static void prune(Deque<Long> q, long now) {
        while (!q.isEmpty() && now - q.peekFirst() > 1000) q.pollFirst();
    }

    private static void trackSpeed(class_310 mc) {
        if (mc.field_1724 == null) {
            hasLast = false;
            speed = 0;
            return;
        }
        double x = mc.field_1724.method_23317();
        double z = mc.field_1724.method_23321();
        if (hasLast) speed = Math.hypot(x - lastX, z - lastZ) * 20.0;
        lastX = x;
        lastZ = z;
        hasLast = true;
    }

    private static String cap(String s) {
        return s.charAt(0) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    private static String pingText(class_310 mc) {
        if (mc.field_1724 == null || mc.method_1562() == null) return "Ping: --";
        class_640 e = mc.method_1562().method_2871(mc.field_1724.method_5667());
        return e == null ? "Ping: --" : "Ping: " + e.method_2959() + " ms";
    }

    // ---- registry (order here = order in the settings screen) ----
    public static final List<HudModule> ALL = List.of(
            new TextModule("fps", "FPS", true, TOP_LEFT,
                    mc -> mc.method_47599() + " FPS", null),
            new TextModule("coords", "Coordinates", true, TOP_LEFT,
                    mc -> mc.field_1724 == null ? "" :
                            "XYZ: " + mc.field_1724.method_31477() + " / " + mc.field_1724.method_31478() + " / " + mc.field_1724.method_31479(),
                    null),
            new TextModule("direction", "Direction", true, TOP_LEFT,
                    mc -> mc.field_1724 == null ? "" : "Facing: " + cap(mc.field_1724.method_5735().name()),
                    null),
            new TextModule("ping", "Ping", true, TOP_LEFT, Modules::pingText, null),
            new TextModule("cps", "CPS", true, TOP_RIGHT,
                    mc -> "CPS: " + leftClicks.size() + " | " + rightClicks.size(),
                    Modules::trackClicks),
            new TextModule("speed", "Speed", false, TOP_RIGHT,
                    mc -> String.format(Locale.ROOT, "%.2f b/s", speed),
                    Modules::trackSpeed),
            new TextModule("clock", "Clock", false, TOP_RIGHT,
                    mc -> LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), null),
            new ArmorModule(),
            new KeystrokesModule(),
            new MultitaskModule()
    );

    // ---------------------------------------------------------------
    static final class TextModule implements HudModule {
        private final String id, label;
        private final boolean enabledByDefault;
        private final HudConfig.Anchor anchor;
        private final Function<class_310, String> text;
        private final Consumer<class_310> tick;

        TextModule(String id, String label, boolean enabledByDefault, HudConfig.Anchor anchor,
                   Function<class_310, String> text, Consumer<class_310> tick) {
            this.id = id;
            this.label = label;
            this.enabledByDefault = enabledByDefault;
            this.anchor = anchor;
            this.text = text;
            this.tick = tick;
        }

        @Override public String id() { return id; }
        @Override public String label() { return label; }
        @Override public boolean defaultEnabled() { return enabledByDefault; }
        @Override public HudConfig.Anchor defaultAnchor() { return anchor; }

        @Override public int width(class_310 mc) {
            return mc.field_1772.method_1727(text.apply(mc)) + PAD * 2 + STRIPE;
        }

        @Override public int height(class_310 mc) {
            return text.apply(mc).isEmpty() ? 0 : TEXT_H + PAD * 2;
        }

        @Override public void render(class_332 ctx, class_310 mc, int x, int y) {
            String s = text.apply(mc);
            Theme.box(ctx, x, y, width(mc), height(mc));
            ctx.method_25303(mc.field_1772, s, x + PAD + STRIPE, y + PAD, Theme.WHITE);
        }

        @Override public void tick(class_310 mc) {
            if (tick != null) tick.accept(mc);
        }
    }

    // ---------------------------------------------------------------
    static final class ArmorModule implements HudModule {
        private static final class_1304[] SLOTS = {
                class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166
        };
        private static final int ROW = 18;

        @Override public String id() { return "armor"; }
        @Override public String label() { return "Armor"; }
        @Override public boolean defaultEnabled() { return true; }
        @Override public HudConfig.Anchor defaultAnchor() { return BOTTOM_RIGHT; }

        private List<class_1799> worn(class_310 mc) {
            List<class_1799> list = new ArrayList<>();
            if (mc.field_1724 == null) return list;
            for (class_1304 slot : SLOTS) {
                class_1799 s = mc.field_1724.method_6118(slot);
                if (!s.method_7960()) list.add(s);
            }
            return list;
        }

        @Override public int width(class_310 mc) {
            return PAD * 2 + STRIPE + ROW + mc.field_1772.method_1727("100%");
        }

        @Override public int height(class_310 mc) {
            int n = worn(mc).size();
            return n == 0 ? 0 : n * ROW - 2 + PAD * 2;
        }

        @Override public void render(class_332 ctx, class_310 mc, int x, int y) {
            List<class_1799> items = worn(mc);
            Theme.box(ctx, x, y, width(mc), height(mc));
            int rowY = y + PAD;
            for (class_1799 stack : items) {
                ctx.method_51427(stack, x + PAD + STRIPE, rowY);
                if (stack.method_7963()) {
                    int max = stack.method_7936();
                    int pct = Math.max(0, (max - stack.method_7919()) * 100 / max);
                    int color = pct > 60 ? 0xFF55FF55 : pct > 25 ? 0xFFFFFF55 : 0xFFFF5555;
                    ctx.method_25303(mc.field_1772, pct + "%", x + PAD + STRIPE + ROW, rowY + 4, color);
                }
                rowY += ROW;
            }
        }
    }

    // ---------------------------------------------------------------
    static final class KeystrokesModule implements HudModule {
        private static final int KEY = 20, GAP = 2, MOUSE_H = 14;

        @Override public String id() { return "keystrokes"; }
        @Override public String label() { return "Keystrokes"; }
        @Override public boolean defaultEnabled() { return true; }
        @Override public HudConfig.Anchor defaultAnchor() { return BOTTOM_LEFT; }

        @Override public int width(class_310 mc) { return KEY * 3 + GAP * 2; }
        @Override public int height(class_310 mc) { return KEY * 2 + MOUSE_H + GAP * 2; }

        private void key(class_332 ctx, class_310 mc, int x, int y, int w, int h,
                         String label, boolean down) {
            Theme.key(ctx, x, y, w, h, down);
            int tx = x + (w - mc.field_1772.method_1727(label)) / 2;
            int ty = y + (h - TEXT_H) / 2 + 1;
            ctx.method_25303(mc.field_1772, label, tx, ty, down ? 0xFF0B0B14 : Theme.WHITE);
        }

        @Override public void render(class_332 ctx, class_310 mc, int x, int y) {
            var o = mc.field_1690;
            int col = KEY + GAP;
            key(ctx, mc, x + col, y, KEY, KEY, "W", o.field_1894.method_1434());
            int row2 = y + KEY + GAP;
            key(ctx, mc, x, row2, KEY, KEY, "A", o.field_1913.method_1434());
            key(ctx, mc, x + col, row2, KEY, KEY, "S", o.field_1881.method_1434());
            key(ctx, mc, x + col * 2, row2, KEY, KEY, "D", o.field_1849.method_1434());
            int row3 = row2 + KEY + GAP;
            int half = (width(mc) - GAP) / 2;
            key(ctx, mc, x, row3, half, MOUSE_H, "LMB", o.field_1886.method_1434());
            key(ctx, mc, x + half + GAP, row3, half, MOUSE_H, "RMB", o.field_1904.method_1434());
        }
    }

    // ---------------------------------------------------------------
    /**
     * One panel that tracks several things at once: session timer, a stopwatch-style "in this world" timer,
     * sound-detector and ESP counts, and the nearest detected target.
     */
    static final class MultitaskModule implements HudModule {
        private static final long SESSION_START = System.currentTimeMillis();
        private static long worldStart = -1;

        @Override public String id() { return "multitask"; }
        @Override public String label() { return "Multitask"; }
        @Override public boolean defaultEnabled() { return true; }
        @Override public HudConfig.Anchor defaultAnchor() { return TOP_RIGHT; }

        @Override public void tick(class_310 mc) {
            if (mc.field_1724 == null) worldStart = -1;
            else if (worldStart < 0) worldStart = System.currentTimeMillis();
        }

        private static String clock(long ms) {
            long s = Math.max(0, ms / 1000);
            return String.format(Locale.ROOT, "%02d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60);
        }

        private List<String> lines() {
            HudConfig cfg = HudConfig.get();
            List<String> l = new ArrayList<>();
            long now = System.currentTimeMillis();
            l.add("Session " + clock(now - SESSION_START));
            l.add("World   " + clock(worldStart < 0 ? 0 : now - worldStart));
            l.add("Sound " + (cfg.soundEnabled ? SoundDetector.totalCount : "off")
                    + "  |  Blocks " + (cfg.espBlocksEnabled ? Esp.blockCount : "off")
                    + "  |  Ents " + (cfg.espEntitiesEnabled ? Esp.entityCount : "off"));
            if (cfg.soundEnabled && !SoundDetector.nearestText.isEmpty()) l.add("Nearest: " + SoundDetector.nearestText);
            return l;
        }

        @Override public int width(class_310 mc) {
            int w = 0;
            for (String s : lines()) w = Math.max(w, mc.field_1772.method_1727(s));
            return w + PAD * 2 + STRIPE;
        }

        @Override public int height(class_310 mc) {
            return lines().size() * (TEXT_H + 2) - 2 + PAD * 2;
        }

        @Override public void render(class_332 ctx, class_310 mc, int x, int y) {
            List<String> l = lines();
            Theme.box(ctx, x, y, width(mc), height(mc));
            int ty = y + PAD;
            for (String s : l) {
                ctx.method_25303(mc.field_1772, s, x + PAD + STRIPE, ty, Theme.WHITE);
                ty += TEXT_H + 2;
            }
        }
    }
}

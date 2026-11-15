package modularhud;

import net.minecraft.class_332;

/** Shared look: pure-black panels with a thin pink-to-violet accent. */
public final class Theme {
    public static final int ACCENT_A = 0xFFFF6FB5;
    public static final int ACCENT_B = 0xFF9B6BFF;
    public static final int BG = 0xE6000000;
    public static final int BG_PANEL = 0xFF000000;
    public static final int SCREEN_DIM = 0xE8000000;
    public static final int BTN = 0xFF000000;
    public static final int BTN_HOVER = 0xFF1C1C1C;
    public static final int BTN_BORDER = 0xFF34343C;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int MUTED = 0xFFB8B8C8;

    /** 16-colour palette used by the colour grids. */
    public static final int[] PALETTE = {
            0xFFFF4D4D, 0xFFFF7A59, 0xFFFF9A3C, 0xFFFFC857,
            0xFFFFE14D, 0xFF8CFF6F, 0xFF7CFF7C, 0xFF2AD1C9,
            0xFF5CF2FF, 0xFF4D8DFF, 0xFF9B6BFF, 0xFFB04DFF,
            0xFFFF6FB5, 0xFFB5651D, 0xFFB8B8C8, 0xFFFFFFFF
    };

    private Theme() {}

    public static int lerp(int a, int b, float t) {
        int aa = (int) (((a >>> 24) & 255) + (((b >>> 24) & 255) - ((a >>> 24) & 255)) * t);
        int ar = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int ag = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int ab = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (aa << 24) | (ar << 16) | (ag << 8) | ab;
    }

    /** Horizontal 1px line fading from colA to colB. */
    public static void hLine(class_332 c, int x, int y, int w, int colA, int colB) {
        int seg = Math.max(1, w / 48);
        for (int i = 0; i < w; i += seg) {
            c.method_25294(x + i, y, Math.min(x + i + seg, x + w), y + 1, lerp(colA, colB, i / (float) w));
        }
    }

    /** 1px rectangle outline. */
    public static void rectOutline(class_332 c, int x, int y, int w, int h, int col) {
        c.method_25294(x, y, x + w, y + 1, col);
        c.method_25294(x, y + h - 1, x + w, y + h, col);
        c.method_25294(x, y, x + 1, y + h, col);
        c.method_25294(x + w - 1, y, x + w, y + h, col);
    }

    /** HUD module background: black, accent stripe on the left. */
    public static void box(class_332 c, int x, int y, int w, int h) {
        c.method_25294(x, y, x + w, y + h, BG);
        c.method_25296(x, y, x + 2, y + h, ACCENT_A, ACCENT_B);
    }

    /** Settings panel: solid black with a gradient border. */
    public static void panel(class_332 c, int x, int y, int w, int h) {
        c.method_25294(x, y, x + w, y + h, BG_PANEL);
        hLine(c, x, y, w, ACCENT_A, ACCENT_B);
        hLine(c, x, y + h - 1, w, ACCENT_B, ACCENT_A);
        c.method_25296(x, y, x + 1, y + h, ACCENT_A, ACCENT_B);
        c.method_25296(x + w - 1, y, x + w, y + h, ACCENT_A, ACCENT_B);
    }

    /** One keystroke-style key. */
    public static void key(class_332 c, int x, int y, int w, int h, boolean down) {
        c.method_25294(x, y, x + w, y + h, down ? 0xD0FF6FB5 : BG);
        rectOutline(c, x, y, w, h, down ? 0x80FFFFFF : 0x40FFFFFF);
    }
}

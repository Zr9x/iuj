package modularhud;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_9779;

public final class HudRenderer {
    private static final int MARGIN = 4;
    private static final int GAP = 2;

    private HudRenderer() {}

    public static void render(class_332 ctx, class_9779 tickCounter) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1690.field_1842) return;

        int sw = ctx.method_51421();
        int sh = ctx.method_51443();
        HudConfig cfg = HudConfig.get();

        Esp.render(ctx, mc);
        SoundDetector.render(ctx, mc);

        for (HudConfig.Anchor anchor : HudConfig.Anchor.values()) {
            List<HudModule> stack = new ArrayList<>();
            for (HudModule m : Modules.ALL) {
                HudConfig.Entry e = cfg.entry(m);
                if (e.enabled && e.anchor == anchor) stack.add(m);
            }

            boolean bottom = anchor == HudConfig.Anchor.BOTTOM_LEFT || anchor == HudConfig.Anchor.BOTTOM_RIGHT;
            boolean right = anchor == HudConfig.Anchor.TOP_RIGHT || anchor == HudConfig.Anchor.BOTTOM_RIGHT;
            int y = bottom ? sh - MARGIN : MARGIN;

            for (HudModule m : stack) {
                int h = m.height(mc);
                if (h <= 0) continue;
                int w = m.width(mc);
                int x = right ? sw - MARGIN - w : MARGIN;
                if (bottom) {
                    y -= h;
                    m.render(ctx, mc, x, y);
                    y -= GAP;
                } else {
                    m.render(ctx, mc, x, y);
                    y += h + GAP;
                }
            }
        }
    }
}

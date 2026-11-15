package modularhud;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/**
 * Base for the mod's screens: solid black backdrop and black buttons.
 * Real vanilla buttons are still added (they handle clicks); this class repaints them black on top.
 */
public abstract class BlackScreen extends class_437 {
    private record Btn(int x, int y, int w, int h, Supplier<String> label) {}
    private record Swatch(int x, int y, int size, int color, Supplier<Integer> current) {}

    private record Chip(int x, int y, int w, int h, Supplier<Integer> color, Supplier<Boolean> selected) {}

    private final List<Chip> chips = new ArrayList<>();
    private final List<Btn> btns = new ArrayList<>();
    private final List<Swatch> swatches = new ArrayList<>();

    protected BlackScreen(class_2561 title) {
        super(title);
    }

    /** Lay out widgets here (called on open and on resize). */
    protected abstract void build();

    /** Extra drawing on top of the buttons. */
    protected void afterWidgets(class_332 ctx, int mouseX, int mouseY) {}

    @Override
    protected final void method_25426() {
        btns.clear();
        swatches.clear();
        chips.clear();
        build();
    }

    protected void btn(int x, int y, int w, int h, Supplier<String> label, Runnable action) {
        btns.add(new Btn(x, y, w, h, label));
        this.method_37063(class_4185.method_46430(class_2561.method_43470(" "), b -> action.run())
                .method_46434(x, y, w, h).method_46431());
    }

    /** Small colour chip button (shows {@code color}, null = greyed out). */
    protected void chip(int x, int y, int w, int h, Supplier<Integer> color, Supplier<Boolean> selected, Runnable action) {
        chips.add(new Chip(x, y, w, h, color, selected));
        this.method_37063(class_4185.method_46430(class_2561.method_43470(" "), b -> action.run())
                .method_46434(x, y, w, h).method_46431());
    }

    /** One row of palette swatches. {@code current} says which colour is selected; {@code pick} receives the choice. */
    protected void colorRow(int x, int y, int cell, Supplier<Integer> current, IntConsumer pick) {
        for (int i = 0; i < Theme.PALETTE.length; i++) {
            final int col = Theme.PALETTE[i];
            int cx = x + i * cell;
            swatches.add(new Swatch(cx, y, cell - 2, col, current));
            this.method_37063(class_4185.method_46430(class_2561.method_43470(" "), b -> pick.accept(col))
                    .method_46434(cx, y, cell - 2, cell - 2).method_46431());
        }
    }

    @Override
    public void method_25420(class_332 ctx, int mouseX, int mouseY, float delta) {
        super.method_25420(ctx, mouseX, mouseY, delta);
        ctx.method_25294(0, 0, this.field_22789, this.field_22790, Theme.SCREEN_DIM);
    }

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        super.method_25394(ctx, mouseX, mouseY, delta);

        for (Btn b : btns) {
            boolean hover = mouseX >= b.x() && mouseX < b.x() + b.w() && mouseY >= b.y() && mouseY < b.y() + b.h();
            ctx.method_25294(b.x(), b.y(), b.x() + b.w(), b.y() + b.h(), hover ? Theme.BTN_HOVER : Theme.BTN);
            Theme.rectOutline(ctx, b.x(), b.y(), b.w(), b.h(), hover ? Theme.ACCENT_A : Theme.BTN_BORDER);
            ctx.method_25300(this.field_22793, b.label().get(), b.x() + b.w() / 2, b.y() + (b.h() - 8) / 2, Theme.WHITE);
        }
        for (Swatch s : swatches) {
            boolean hover = mouseX >= s.x() && mouseX < s.x() + s.size() && mouseY >= s.y() && mouseY < s.y() + s.size();
            boolean selected = s.current().get() != null && (s.current().get() & 0x00FFFFFF) == (s.color() & 0x00FFFFFF);
            ctx.method_25294(s.x(), s.y(), s.x() + s.size(), s.y() + s.size(), 0xFF000000);
            ctx.method_25294(s.x() + 2, s.y() + 2, s.x() + s.size() - 2, s.y() + s.size() - 2, s.color());
            if (selected || hover) {
                Theme.rectOutline(ctx, s.x(), s.y(), s.size(), s.size(), selected ? Theme.WHITE : Theme.ACCENT_A);
            }
        }
        for (Chip c : chips) {
            boolean hover = mouseX >= c.x() && mouseX < c.x() + c.w() && mouseY >= c.y() && mouseY < c.y() + c.h();
            Integer col = c.color().get();
            ctx.method_25294(c.x(), c.y(), c.x() + c.w(), c.y() + c.h(), Theme.BTN);
            if (col != null) ctx.method_25294(c.x() + 3, c.y() + 3, c.x() + c.w() - 3, c.y() + c.h() - 3, col);
            Theme.rectOutline(ctx, c.x(), c.y(), c.w(), c.h(),
                    c.selected().get() ? Theme.WHITE : hover ? Theme.ACCENT_A : Theme.BTN_BORDER);
        }
        afterWidgets(ctx, mouseX, mouseY);
    }

    @Override
    public void method_25432() {
        HudConfig.save();
    }
}

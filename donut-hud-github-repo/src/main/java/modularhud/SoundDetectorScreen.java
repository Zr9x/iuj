package modularhud;

import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/** Sound detector settings: toggles, range, which blocks/entities to outline, and per-target colours. */
public class SoundDetectorScreen extends BlackScreen {
    private static final int PAD = 14;
    private static final int BTN_W = 124, GAP = 6, ROW_H = 22, CELL = 24, CHIP = 20;

    private final class_437 parent;
    private int panelX, panelY, panelW, panelH, headerY, gridY, leftX;
    private SoundTarget editing;

    public SoundDetectorScreen(class_437 parent) {
        super(class_2561.method_43470("Sound Detector"));
        this.parent = parent;
    }

    @Override
    protected void build() {
        HudConfig cfg = HudConfig.get();
        int contentW = BTN_W * 3 + GAP * 2;
        int half = (contentW - GAP) / 2;
        int rows = 5;

        panelW = contentW + PAD * 2;
        panelH = 34 + ROW_H * 3 + 16 + rows * ROW_H + 16 + CELL + 8 + 20 + PAD;
        panelX = this.field_22789 / 2 - panelW / 2;
        panelY = Math.max(2, (this.field_22790 - panelH) / 2);
        leftX = panelX + PAD;

        int y = panelY + 34;
        btn(leftX, y, half, 20, () -> "Detector: " + (cfg.soundEnabled ? "ON" : "OFF"), () -> {
            cfg.soundEnabled = !cfg.soundEnabled;
            HudConfig.save();
        });
        btn(leftX + half + GAP, y, half, 20, () -> "Labels: " + (cfg.soundLabels ? "ON" : "OFF"), () -> {
            cfg.soundLabels = !cfg.soundLabels;
            HudConfig.save();
        });
        y += ROW_H;

        btn(leftX, y, half, 20, () -> "Summary list: " + (cfg.soundSummary ? "ON" : "OFF"), () -> {
            cfg.soundSummary = !cfg.soundSummary;
            HudConfig.save();
        });
        btn(leftX + half + GAP, y, half, 20, () -> "Edge arrows: " + (cfg.soundArrows ? "ON" : "OFF"), () -> {
            cfg.soundArrows = !cfg.soundArrows;
            HudConfig.save();
        });
        y += ROW_H;

        btn(leftX, y, half, 20, () -> "Tracers: " + (cfg.soundTracers ? "ON" : "OFF"), () -> {
            cfg.soundTracers = !cfg.soundTracers;
            HudConfig.save();
        });
        int rx = leftX + half + GAP;
        btn(rx, y, 24, 20, () -> "-", () -> {
            cfg.soundRange = Math.max(HudConfig.MIN_RANGE, cfg.range() - 1);
            HudConfig.save();
        });
        btn(rx + 24, y, half - 48, 20, () -> "Range: " + cfg.range() + " chunks (" + cfg.range() * 16 + "m)", () -> {});
        btn(rx + half - 24, y, 24, 20, () -> "+", () -> {
            cfg.soundRange = Math.min(HudConfig.MAX_RANGE, cfg.range() + 1);
            HudConfig.save();
        });
        y += ROW_H;

        headerY = y + 3;
        y += 16;

        List<SoundTarget> blocks = SoundTarget.blocks();
        List<SoundTarget> ents = SoundTarget.entities();
        for (int i = 0; i < blocks.size(); i++) {
            addTarget(cfg, blocks.get(i), leftX + (i / rows) * (BTN_W + GAP), y + (i % rows) * ROW_H);
        }
        for (int i = 0; i < ents.size(); i++) {
            addTarget(cfg, ents.get(i), leftX + 2 * (BTN_W + GAP), y + i * ROW_H);
        }
        y += rows * ROW_H + 16;
        gridY = y;

        colorRow(leftX, y, CELL, () -> editing == null ? null : cfg.colorOf(editing), col -> {
            if (editing != null) {
                cfg.setColor(editing, col);
                HudConfig.save();
            }
        });
        y += CELL + 8;

        btn(this.field_22789 / 2 - 50, y, 100, 20, () -> "Back", this::method_25419);
    }

    private void addTarget(HudConfig cfg, SoundTarget t, int x, int y) {
        btn(x, y, BTN_W - CHIP - 2, 20, () -> t.label + ": " + (cfg.targetOn(t) ? "ON" : "OFF"), () -> {
            cfg.setTarget(t, !cfg.targetOn(t));
            HudConfig.save();
        });
        chip(x + BTN_W - CHIP, y, CHIP, 20, () -> cfg.colorOf(t), () -> t == editing, () -> editing = t);
    }

    @Override
    public void method_25420(class_332 ctx, int mouseX, int mouseY, float delta) {
        super.method_25420(ctx, mouseX, mouseY, delta);
        Theme.panel(ctx, panelX, panelY, panelW, panelH);
    }

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        super.method_25394(ctx, mouseX, mouseY, delta);
        ctx.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, panelY + 10, Theme.ACCENT_A);
        Theme.hLine(ctx, panelX + PAD, panelY + 24, panelW - PAD * 2, Theme.ACCENT_A, Theme.ACCENT_B);

        int twoCols = BTN_W * 2 + GAP;
        ctx.method_25300(this.field_22793, "Blocks", leftX + twoCols / 2, headerY, Theme.MUTED);
        ctx.method_25300(this.field_22793, "Entities", leftX + 2 * (BTN_W + GAP) + BTN_W / 2, headerY, Theme.MUTED);
        ctx.method_25303(this.field_22793,
                editing == null ? "Colour: click a colour chip, then pick below" : "Colour for: " + editing.label,
                leftX, gridY - 11, Theme.MUTED);
    }

    @Override
    public void method_25419() {
        this.field_22787.method_1507(parent);
    }
}

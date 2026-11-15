package modularhud;

import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_332;

public class HudConfigScreen extends BlackScreen {
    private static final int ROW_H = 22;
    private static final int PAD = 14;

    private int panelX, panelY, panelW, panelH;

    public HudConfigScreen() {
        super(class_2561.method_43471("modularhud.config.title"));
    }

    @Override
    protected void build() {
        HudConfig cfg = HudConfig.get();
        List<HudModule> mods = Modules.ALL;

        int toggleW = 104, anchorW = 86, gap = 4, colGap = 12;
        int pairW = toggleW + gap + anchorW;
        int contentW = pairW * 2 + colGap;
        int perCol = (mods.size() + 1) / 2;

        panelW = contentW + PAD * 2;
        panelH = 34 + perCol * ROW_H + 10 + ROW_H * 3 + 8 + 20 + PAD;
        panelX = this.field_22789 / 2 - panelW / 2;
        panelY = Math.max(2, (this.field_22790 - panelH) / 2);

        int left = panelX + PAD;
        int top = panelY + 34;

        for (int i = 0; i < mods.size(); i++) {
            HudModule m = mods.get(i);
            HudConfig.Entry e = cfg.entry(m);
            int col = i / perCol, row = i % perCol;
            int x = left + col * (pairW + colGap);
            int y = top + row * ROW_H;

            btn(x, y, toggleW, 20, () -> m.label() + ": " + (e.enabled ? "ON" : "OFF"), () -> {
                e.enabled = !e.enabled;
                HudConfig.save();
            });
            btn(x + toggleW + gap, y, anchorW, 20, () -> e.anchor.label, () -> {
                e.anchor = e.anchor.next();
                HudConfig.save();
            });
        }

        int y = top + perCol * ROW_H + 10;
        int settingsW = 80;
        int wide = contentW - settingsW - gap;

        btn(left, y, wide, 20, () -> "Sound Detector: " + (cfg.soundEnabled ? "ON" : "OFF"), () -> {
            cfg.soundEnabled = !cfg.soundEnabled;
            HudConfig.save();
        });
        btn(left + contentW - settingsW, y, settingsW, 20, () -> "Settings", () ->
                this.field_22787.method_1507(new SoundDetectorScreen(this)));
        y += ROW_H;

        btn(left, y, wide, 20, () -> "Block ESP: " + (cfg.espBlocksEnabled ? "ON" : "OFF"), () -> {
            cfg.espBlocksEnabled = !cfg.espBlocksEnabled;
            Esp.invalidate();
            HudConfig.save();
        });
        btn(left + contentW - settingsW, y, settingsW, 20, () -> "Select", () ->
                this.field_22787.method_1507(new EspScreen(this, true)));
        y += ROW_H;

        btn(left, y, wide, 20, () -> "Entity ESP: " + (cfg.espEntitiesEnabled ? "ON" : "OFF"), () -> {
            cfg.espEntitiesEnabled = !cfg.espEntitiesEnabled;
            HudConfig.save();
        });
        btn(left + contentW - settingsW, y, settingsW, 20, () -> "Select", () ->
                this.field_22787.method_1507(new EspScreen(this, false)));
        y += ROW_H + 8;

        btn(this.field_22789 / 2 - 50, y, 100, 20, () -> "Done", this::method_25419);
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
    }
}

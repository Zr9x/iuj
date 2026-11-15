package modularhud;

import java.util.List;
import java.util.Map;
import net.minecraft.class_2561;
import net.minecraft.class_332;

/** Block / entity selector: pick what to outline and give each pick its own colour from the grid. */
public class EspScreen extends BlackScreen {
    private static final int PAD = 14, BTN_W = 150, GAP = 6, ROW_H = 22, ROWS = 8, CELL = 19;
    private static final int CHIP = 20;

    private final net.minecraft.class_437 parentScreen;
    private final boolean blocks;
    private int page;
    private String editing;
    private int panelX, panelY, panelW, panelH, listY, headerY, gridY, leftX;

    public EspScreen(net.minecraft.class_437 parent, boolean blocks) {
        super(class_2561.method_43470(blocks ? "Block ESP" : "Entity ESP"));
        this.parentScreen = parent;
        this.blocks = blocks;
    }

    private Map<String, Integer> map() {
        HudConfig c = HudConfig.get();
        return blocks ? c.espBlocks : c.espEntities;
    }

    private List<String> all() {
        return blocks ? Candidates.BLOCKS : Candidates.ENTITIES;
    }

    private int pages() {
        int per = ROWS * 2;
        return Math.max(1, (all().size() + per - 1) / per);
    }

    private String idAt(int slot) {
        int idx = page * ROWS * 2 + slot;
        return idx < all().size() ? all().get(idx) : null;
    }

    private void changed() {
        HudConfig.save();
        if (blocks) Esp.invalidate();
    }

    private boolean enabled(HudConfig c) { return blocks ? c.espBlocksEnabled : c.espEntitiesEnabled; }

    @Override
    protected void build() {
        HudConfig cfg = HudConfig.get();
        int contentW = BTN_W * 2 + GAP;
        int half = (contentW - GAP) / 2;
        panelW = contentW + PAD * 2;
        panelH = 34 + ROW_H * 2 + 14 + ROWS * ROW_H + ROW_H + 14 + CELL + 8 + 20 + PAD;
        panelX = this.field_22789 / 2 - panelW / 2;
        panelY = Math.max(2, (this.field_22790 - panelH) / 2);
        leftX = panelX + PAD;
        page = Math.max(0, Math.min(page, pages() - 1));

        int y = panelY + 34;
        btn(leftX, y, half, 20, () -> (blocks ? "Block ESP: " : "Entity ESP: ") + (enabled(cfg) ? "ON" : "OFF"), () -> {
            if (blocks) cfg.espBlocksEnabled = !cfg.espBlocksEnabled;
            else cfg.espEntitiesEnabled = !cfg.espEntitiesEnabled;
            changed();
        });
        btn(leftX + half + GAP, y, half, 20, () -> "Labels: " + (cfg.espLabels ? "ON" : "OFF"), () -> {
            cfg.espLabels = !cfg.espLabels;
            HudConfig.save();
        });
        y += ROW_H;

        btn(leftX, y, half, 20, () -> "Tracers: " + (cfg.espTracers ? "ON" : "OFF"), () -> {
            cfg.espTracers = !cfg.espTracers;
            HudConfig.save();
        });
        int rx = leftX + half + GAP;
        btn(rx, y, 24, 20, () -> "-", () -> {
            cfg.espRange = Math.max(HudConfig.MIN_RANGE, cfg.espRange() - 1);
            changed();
        });
        btn(rx + 24, y, half - 48, 20, () -> "Range: " + cfg.espRange() + " chunks", () -> {});
        btn(rx + half - 24, y, 24, 20, () -> "+", () -> {
            cfg.espRange = Math.min(HudConfig.MAX_RANGE, cfg.espRange() + 1);
            changed();
        });
        y += ROW_H;

        headerY = y + 3;
        y += 14;
        listY = y;

        int per = ROWS * 2;
        for (int i = 0; i < per; i++) {
            final int slot = i;
            int col = i / ROWS, row = i % ROWS;
            int x = leftX + col * (BTN_W + GAP);
            int by = listY + row * ROW_H;
            btn(x, by, BTN_W - CHIP - 2, 20, () -> {
                String id = idAt(slot);
                return id == null ? "" : (map().containsKey(id) ? "[x] " : "[ ] ") + Esp.pretty(id);
            }, () -> {
                String id = idAt(slot);
                if (id == null) return;
                if (map().containsKey(id)) {
                    map().remove(id);
                    if (id.equals(editing)) editing = null;
                } else {
                    map().put(id, Theme.PALETTE[8]);
                    editing = id;
                }
                changed();
            });
            chip(x + BTN_W - CHIP, by, CHIP, 20, () -> {
                String id = idAt(slot);
                return id == null ? null : map().get(id);
            }, () -> {
                String id = idAt(slot);
                return id != null && id.equals(editing);
            }, () -> {
                String id = idAt(slot);
                if (id != null && map().containsKey(id)) editing = id;
            });
        }
        y += ROWS * ROW_H;

        // pager
        int pw = 24;
        btn(leftX, y, pw, 20, () -> "<", () -> page = (page + pages() - 1) % pages());
        btn(leftX + pw + 2, y, 90, 20, () -> "Page " + (page + 1) + "/" + pages(), () -> {});
        btn(leftX + pw + 94, y, pw, 20, () -> ">", () -> page = (page + 1) % pages());
        btn(leftX + contentW - 90, y, 90, 20, () -> "Clear all", () -> {
            map().clear();
            editing = null;
            changed();
        });
        y += ROW_H + 14;
        gridY = y;

        colorRow(leftX, y, CELL, () -> editing == null ? null : map().get(editing), col -> {
            if (editing != null && map().containsKey(editing)) {
                map().put(editing, col);
                changed();
            }
        });
        y += CELL + 8;

        btn(this.field_22789 / 2 - 50, y, 100, 20, () -> "Back", this::method_25419);
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
        ctx.method_25303(this.field_22793, "Click to add / remove. Chip = pick that entry's colour.",
                leftX, headerY, Theme.MUTED);
        String c = editing == null ? "Colour: select an entry's chip first" : "Colour for: " + Esp.pretty(editing);
        ctx.method_25303(this.field_22793, c, leftX, gridY - 11, Theme.MUTED);
    }

    @Override
    public void method_25419() {
        this.field_22787.method_1507(parentScreen);
    }
}

package modularhud;

import net.minecraft.class_310;
import net.minecraft.class_332;

public interface HudModule {
    String id();
    String label();
    boolean defaultEnabled();
    HudConfig.Anchor defaultAnchor();

    /** Width/height in scaled GUI pixels. Return a height of 0 to skip drawing this frame. */
    int width(class_310 mc);
    int height(class_310 mc);

    void render(class_332 ctx, class_310 mc, int x, int y);

    default void tick(class_310 mc) {}
}

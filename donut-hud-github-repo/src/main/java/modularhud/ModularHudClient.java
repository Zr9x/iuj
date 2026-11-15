package modularhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import org.lwjgl.glfw.GLFW;

public class ModularHudClient implements ClientModInitializer {
    public static final String MOD_ID = "modularhud";
    private static class_304 openConfigKey;
    private static class_304 toggleSoundKey;
    private static class_304 toggleEspKey;

    @Override
    public void onInitializeClient() {
        HudConfig.get();

        class_304.class_11900 category = class_304.class_11900.method_74698(class_2960.method_60655(MOD_ID, "main"));

        // Right Shift opens the HUD settings
        openConfigKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.modularhud.open_config", class_3675.class_307.field_1668, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        // J toggles the sound detector on/off (rebindable)
        toggleSoundKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.modularhud.toggle_sound", class_3675.class_307.field_1668, GLFW.GLFW_KEY_J, category));

        // K toggles block + entity ESP (rebindable)
        toggleEspKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.modularhud.toggle_esp", class_3675.class_307.field_1668, GLFW.GLFW_KEY_K, category));

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                class_2960.method_60655(MOD_ID, "hud"),
                HudRenderer::render
        );

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            for (HudModule m : Modules.ALL) {
                m.tick(mc);
            }
            Projector.tick(mc);
            SoundDetector.tick(mc);
            Esp.tick(mc);

            while (openConfigKey.method_1436()) {
                if (mc.field_1755 == null) {
                    mc.method_1507(new HudConfigScreen());
                }
            }
            while (toggleEspKey.method_1436()) {
                HudConfig cfg = HudConfig.get();
                boolean on = !(cfg.espBlocksEnabled || cfg.espEntitiesEnabled);
                cfg.espBlocksEnabled = on;
                cfg.espEntitiesEnabled = on;
                Esp.invalidate();
                HudConfig.save();
                if (mc.field_1724 != null) {
                    mc.field_1724.method_7353(class_2561.method_43470("ESP: " + (on ? "ON" : "OFF")), true);
                }
            }
            while (toggleSoundKey.method_1436()) {
                HudConfig cfg = HudConfig.get();
                cfg.soundEnabled = !cfg.soundEnabled;
                HudConfig.save();
                if (mc.field_1724 != null) {
                    mc.field_1724.method_7353(class_2561.method_43470("Sound Detector: " + (cfg.soundEnabled ? "ON" : "OFF")), true);
                }
            }
        });
    }
}

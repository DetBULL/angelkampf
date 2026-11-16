package de.angelkampf.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class AngelkampfClient implements ClientModInitializer {
    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        AngelkampfConfig.load();

        // Ohne Mod Menu kommt man über diese (standardmäßig freie) Taste in die Einstellungen
        configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.angelkampf.config", InputConstants.UNKNOWN.getValue(), "key.categories.angelkampf"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            FightGame.clientTick(mc);
            while (configKey.consumeClick()) {
                mc.setScreen(new AngelkampfConfigScreen(mc.screen));
            }
        });

        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            Minecraft mc = Minecraft.getInstance();
            FightGame game = FightGame.current();
            if (game == null || mc.options.hideGui) return;

            float tension = Mth.lerp(tickDelta, game.prevTension, game.tension);
            float progress = Mth.lerp(tickDelta, game.prevProgress, game.progress);

            float alpha = Mth.clamp((game.age + tickDelta * FightGame.DT) / 0.15f, 0f, 1f);
            if (game.end != FightGame.End.NONE) {
                float t = game.endTime + tickDelta * FightGame.DT;
                float hold = game.end == FightGame.End.CANCELLED ? 0f : 0.4f;
                alpha *= 1f - Mth.clamp((t - hold) / 0.5f, 0f, 1f);
            }

            AngelkampfConfig cfg = AngelkampfConfig.get();
            float[] pos = FightRenderer.position(graphics.guiWidth(), graphics.guiHeight(), cfg);
            FightRenderer.draw(graphics, pos[0], pos[1], cfg.scale, tension, progress,
                    game.end == FightGame.End.CAUGHT, alpha);
        });
    }
}

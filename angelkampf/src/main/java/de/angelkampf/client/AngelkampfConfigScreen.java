package de.angelkampf.client;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Einstellungen mit Live-Vorschau: Die Leiste wird genau dort gezeichnet,
 * wo sie auch im Spiel erscheint, und bewegt sich zur Probe.
 */
public class AngelkampfConfigScreen extends Screen {
    private final Screen parent;
    private ScaleSlider scaleSlider;
    private OffsetSlider offsetSlider;

    public AngelkampfConfigScreen(Screen parent) {
        super(Component.translatable("angelkampf.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        AngelkampfConfig cfg = AngelkampfConfig.get();
        int w = 200, x = (this.width - w) / 2, y = 40;

        scaleSlider = addRenderableWidget(new ScaleSlider(x, y, w, 20, cfg.scale));
        offsetSlider = addRenderableWidget(new OffsetSlider(x, y + 24, w, 20, cfg.offset));

        addRenderableWidget(Button.builder(Component.translatable("angelkampf.config.reset"), b -> {
            cfg.scale = AngelkampfConfig.DEFAULT_SCALE;
            cfg.offset = AngelkampfConfig.DEFAULT_OFFSET;
            this.rebuildWidgets();
        }).bounds(x, y + 52, 98, 20).build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(x + 102, y + 52, 98, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        // Angedeutete Hotbar, damit man die Position einschätzen kann
        int hx = (this.width - 182) / 2, hy = this.height - 22;
        g.fill(hx, hy, hx + 182, hy + 22, 0x90000000);
        g.fill(hx + 1, hy + 1, hx + 181, hy + 21, 0x60808080);
        for (int i = 1; i < 9; i++) g.fill(hx + i * 20 + 1, hy + 1, hx + i * 20 + 2, hy + 21, 0x90000000);
        // Herzen/Hunger-Zone andeuten
        g.fill(hx, hy - 16, hx + 81, hy - 7, 0x50A83232);
        g.fill(hx + 101, hy - 16, hx + 182, hy - 7, 0x50A8892E);

        // Bewegte Vorschau
        AngelkampfConfig cfg = AngelkampfConfig.get();
        float t = (Util.getMillis() % 100000L) / 1000f;
        float tension = 48f + 34f * Mth.sin(t * 1.3f) + 6f * Mth.sin(t * 4.1f);
        float progress = (t % 6f) / 6f;
        float[] pos = FightRenderer.position(this.width, this.height, cfg);
        FightRenderer.draw(g, pos[0], pos[1], cfg.scale, tension, progress, false, 1f);
        g.drawCenteredString(this.font, Component.translatable("angelkampf.config.preview"),
                this.width / 2, (int) pos[1] - 11, 0xA0A0A0);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        AngelkampfConfig.save();
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    private static class ScaleSlider extends AbstractSliderButton {
        ScaleSlider(int x, int y, int w, int h, float scale) {
            super(x, y, w, h, Component.empty(),
                    (scale - AngelkampfConfig.MIN_SCALE) / (AngelkampfConfig.MAX_SCALE - AngelkampfConfig.MIN_SCALE));
            updateMessage();
        }

        private float scale() {
            float raw = AngelkampfConfig.MIN_SCALE + (float) this.value * (AngelkampfConfig.MAX_SCALE - AngelkampfConfig.MIN_SCALE);
            return Math.round(raw * 20f) / 20f; // 5-%-Schritte
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("angelkampf.config.scale", Math.round(scale() * 100f)));
        }

        @Override
        protected void applyValue() {
            AngelkampfConfig.get().scale = scale();
        }
    }

    private static class OffsetSlider extends AbstractSliderButton {
        OffsetSlider(int x, int y, int w, int h, int offset) {
            super(x, y, w, h, Component.empty(),
                    (offset - AngelkampfConfig.MIN_OFFSET) / (double) (AngelkampfConfig.MAX_OFFSET - AngelkampfConfig.MIN_OFFSET));
            updateMessage();
        }

        private int offset() {
            return (int) Math.round(AngelkampfConfig.MIN_OFFSET + this.value * (AngelkampfConfig.MAX_OFFSET - AngelkampfConfig.MIN_OFFSET));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("angelkampf.config.offset", offset()));
        }

        @Override
        protected void applyValue() {
            AngelkampfConfig.get().offset = offset();
        }
    }
}

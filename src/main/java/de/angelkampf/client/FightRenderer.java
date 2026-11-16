package de.angelkampf.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Zeichnet das kleine Fenster: dunkelblauer Hintergrund, mangogelber Rahmen,
 * abgerundete Ecken, eine Spannungsleiste mit Zonen und eine dünne Fortschrittslinie.
 */
public final class FightRenderer {
    // Maße in GUI-Pixeln bei Größe 100 %
    public static final int W = 100, H = 14;
    private static final int BAR_X = 4, BAR_Y = 4, BAR_W = 92, BAR_H = 6;

    private static final int MANGO = 0xF4A42B;
    private static final int WIN_GREEN = 0x5FAE73;
    private static final int WINDOW = 0x2B4A5E;
    private static final int BAR_BG = 0x1A2F3D;
    private static final int ZONE_GREEN = 0x3F7A52;
    private static final int ZONE_YELLOW = 0xA8892E;
    private static final int ZONE_RED = 0x9C3434;
    private static final int MARKER = 0xF2F2F2;
    private static final int PROGRESS = 0xCFE6F5;

    private static final int[] OUTER_CORNER = {2, 1, 0};
    private static final int[] INNER_CORNER = {1, 0};

    /** Linke obere Ecke des Fensters auf dem Bildschirm (vor der Skalierung). */
    public static float[] position(int guiWidth, int guiHeight, AngelkampfConfig cfg) {
        float w = W * cfg.scale, h = H * cfg.scale;
        float x = (guiWidth - w) / 2f;
        float y = guiHeight - 59 - h - cfg.offset; // knapp über Herzen/Hungerleiste
        return new float[]{x, y};
    }

    /**
     * @param tension  0..100
     * @param progress 0..1
     * @param won      Rahmen grün färben
     * @param alpha    0..1 zum Ein-/Ausblenden
     */
    public static void draw(GuiGraphics g, float x, float y, float scale, float tension, float progress, boolean won, float alpha) {
        int a = Mth.clamp((int) (alpha * 255f), 0, 255);
        if (a <= 3) return;

        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1f);

        // Rahmen und Fenster
        rounded(g, 0, 0, W, H, OUTER_CORNER, argb(a, won ? WIN_GREEN : MANGO));
        rounded(g, 1, 1, W - 1, H - 1, INNER_CORNER, argb(a, WINDOW));

        // Leiste mit Zonen
        int bx = BAR_X, by = BAR_Y, bx2 = BAR_X + BAR_W, by2 = BAR_Y + BAR_H;
        g.fill(bx, by, bx2, by2, argb(a, BAR_BG));
        g.fill(bx + zone(FightGame.GREEN_START), by, bx + zone(FightGame.YELLOW_START), by2, argb(a, ZONE_GREEN));
        g.fill(bx + zone(FightGame.YELLOW_START), by, bx + zone(FightGame.RED_START), by2, argb(a, ZONE_YELLOW));
        g.fill(bx + zone(FightGame.RED_START), by, bx2, by2, argb(a, ZONE_RED));

        // Fortschritt als dünne Linie an der Unterkante
        int pw = Math.round(Mth.clamp(progress, 0f, 1f) * BAR_W);
        if (pw > 0) g.fill(bx, by2 - 1, bx + pw, by2, argb(a, PROGRESS));

        // Runde Ecken der Leiste
        int win = argb(a, WINDOW);
        g.fill(bx, by, bx + 1, by + 1, win);
        g.fill(bx2 - 1, by, bx2, by + 1, win);
        g.fill(bx, by2 - 1, bx + 1, by2, win);
        g.fill(bx2 - 1, by2 - 1, bx2, by2, win);

        // Spannungsmarker (gleitend, darum über die Matrix verschoben)
        float mx = bx + Mth.clamp(tension, 0f, 100f) / 100f * (BAR_W - 1);
        g.pose().pushPose();
        g.pose().translate(mx, 0, 0);
        g.fill(0, by - 1, 1, by2 + 1, argb(a, MARKER));
        g.pose().popPose();

        g.pose().popPose();
    }

    private static int zone(float percent) {
        return Math.round(percent / 100f * BAR_W);
    }

    private static int argb(int a, int rgb) {
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    /** Rechteck mit pixeligen runden Ecken. insets[i] = wie weit Zeile i oben/unten eingerückt ist. */
    private static void rounded(GuiGraphics g, int x1, int y1, int x2, int y2, int[] insets, int color) {
        int r = insets.length;
        g.fill(x1, y1 + r, x2, y2 - r, color);
        for (int i = 0; i < r; i++) {
            int in = insets[i];
            g.fill(x1 + in, y1 + i, x2 - in, y1 + i + 1, color);
            g.fill(x1 + in, y2 - i - 1, x2 - in, y2 - i, color);
        }
    }
}

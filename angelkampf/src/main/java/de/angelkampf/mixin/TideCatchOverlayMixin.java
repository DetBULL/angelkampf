package de.angelkampf.mixin;

import de.angelkampf.client.FightGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Klinkt sich in Tides Minispiel-Anzeige ein (Tide 2.x, Fabric 1.20.1).
 * Ist Tide nicht installiert, wird dieser Mixin einfach übersprungen.
 */
@Pseudo
@Mixin(targets = "com.li64.tide.client.gui.overlays.CatchMinigameOverlay", remap = false)
public abstract class TideCatchOverlayMixin {

    /** Statt Tides Klick-Minispiel startet unser Drill. */
    @Inject(method = "start", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void angelkampf$start(byte type, byte behavior, float area, float speed, CallbackInfo ci) {
        FightGame.start(type, behavior, area, speed);
        ci.cancel();
    }

    /** Tides Klick-Auswertung abschalten. */
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void angelkampf$interact(CallbackInfo ci) {
        ci.cancel();
    }

    /** Server hat das Minispiel beendet. */
    @Inject(method = "close", at = @At("HEAD"), remap = false, require = 0)
    private static void angelkampf$close(CallbackInfo ci) {
        FightGame.serverClosed();
    }

    /** Tides eigene Leiste nie zeichnen. */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void angelkampf$render(CallbackInfo ci) {
        ci.cancel();
    }
}

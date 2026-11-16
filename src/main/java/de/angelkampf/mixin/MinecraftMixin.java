package de.angelkampf.mixin;

import de.angelkampf.client.FightGame;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Während des Drills ist Rechtsklick nur zum Einholen da:
 * Die Rute wird nicht benutzt, also kein Einholen per Klick und kein Armschwingen.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void angelkampf$blockUse(CallbackInfo ci) {
        if (FightGame.isFighting()) ci.cancel();
    }
}

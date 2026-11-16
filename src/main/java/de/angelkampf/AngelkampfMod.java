package de.angelkampf;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gemeinsamer Einstiegspunkt (Client und Server).
 * Registriert nur ein optionales Paket: Wenn die Schnur reißt, nimmt die Rute Schaden.
 * Ist die Mod nicht auf dem Server installiert, funktioniert alles andere trotzdem.
 */
public class AngelkampfMod implements ModInitializer {
    public static final String MOD_ID = "angelkampf";
    public static final Logger LOG = LoggerFactory.getLogger("Angelkampf");

    public static final ResourceLocation LINE_SNAPPED = new ResourceLocation(MOD_ID, "line_snapped");

    /** Haltbarkeitsschaden an der Rute, wenn die Schnur reißt (Haltbarkeit-Verzauberung wirkt automatisch). */
    public static final int SNAP_ROD_DAMAGE = 3;

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(LINE_SNAPPED, (server, player, handler, buf, responseSender) ->
                server.execute(() -> {
                    for (InteractionHand hand : InteractionHand.values()) {
                        ItemStack stack = player.getItemInHand(hand);
                        if (stack.getItem() instanceof FishingRodItem && stack.isDamageableItem()) {
                            stack.hurtAndBreak(SNAP_ROD_DAMAGE, player, p -> p.broadcastBreakEvent(hand));
                            break;
                        }
                    }
                }));
        LOG.info("Angelkampf geladen");
    }
}

package de.angelkampf.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Äußere Einflüsse auf den Drill. Alles wird auf dem Client aus der Spielwelt gelesen,
 * im Fenster wird davon nichts angezeigt.
 */
public final class Influences {
    /** Wie wild der Fisch ist (Wetter, Nacht, Lava/Void). */
    public float aggression = 1f;
    /** Dauerhafter Zug durch Strömung (0 = stehendes Wasser). */
    public float current = 0f;
    /** Je tiefer, desto länger dauert das Einholen. */
    public float depthFactor = 1f;
    /** Bessere Rute = Spannung steigt langsamer. */
    public float rodFactor = 1f;

    public static Influences sample(Minecraft mc, int display) {
        Influences inf = new Influences();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return inf;

        // Wetter und Tageszeit
        float weather = level.isThundering() ? 1.45f : (level.isRaining() ? 1.2f : 1f);
        long time = level.getDayTime() % 24000L;
        float night = (time >= 13000L && time <= 23000L) ? 1.15f : 1f;
        float medium = display == 1 ? 1.2f : (display == 2 ? 1.3f : 1f); // 1 = Lava, 2 = Void (Tide)
        inf.aggression = weather * night * medium;

        // Strömung und Tiefe am Schwimmer
        BlockPos hookPos = findHookPos(mc, player);
        if (hookPos != null) {
            FluidState fluid = level.getFluidState(hookPos);
            if (fluid.isEmpty()) fluid = level.getFluidState(hookPos.below());
            if (!fluid.isEmpty()) {
                Vec3 flow = fluid.getFlow(level, hookPos);
                inf.current = (float) Mth.clamp(flow.length() * 0.6, 0.0, 0.8);
            }
            int depth = 0;
            BlockPos.MutableBlockPos p = hookPos.mutable();
            for (int i = 0; i < 24; i++) {
                if (level.getFluidState(p).isEmpty()) {
                    if (i > 1) break;
                } else depth++;
                p.move(0, -1, 0);
            }
            inf.depthFactor = 1f + Mth.clamp(depth - 4, 0, 12) * 0.04f;
        }

        // Rute
        ItemStack rod = rodInHand(player);
        if (!rod.isEmpty()) {
            int unbreaking = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, rod);
            float rod_ = 1f + unbreaking * 0.12f;
            if (!rod.is(Items.FISHING_ROD)) rod_ += 0.1f; // Tide-Ruten (Leinen/Haken stecken schon in der Fischstärke)
            inf.rodFactor = rod_;
        }
        return inf;
    }

    public static ItemStack rodInHand(LocalPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack s = player.getItemInHand(hand);
            if (s.getItem() instanceof FishingRodItem) return s;
        }
        return ItemStack.EMPTY;
    }

    /** Sucht Tides Schwimmer-Entity des Spielers; fällt auf den Vanilla-Haken zurück. */
    private static BlockPos findHookPos(Minecraft mc, LocalPlayer player) {
        List<Entity> hooks = mc.level.getEntities(player, player.getBoundingBox().inflate(64),
                e -> e instanceof Projectile pr && pr.getOwner() == player
                        && e.getClass().getSimpleName().toLowerCase().contains("hook"));
        if (!hooks.isEmpty()) return hooks.get(0).blockPosition();
        if (player.fishing != null && player.fishing.distanceToSqr(player) < 64 * 64) return player.fishing.blockPosition();
        return null;
    }
}

package de.angelkampf.client;

import de.angelkampf.AngelkampfMod;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Der eigentliche Drill. Läuft auf dem Client mit 20 Ticks pro Sekunde.
 * Das Ergebnis wird über Tides eigenes Paket "tide:minigame_server" gemeldet,
 * dadurch vergibt Tide Fisch, Journal-Eintrag, Sounds usw. ganz normal.
 */
public final class FightGame {
    /** Tides Paket vom Client zum Server. Inhalt: ein Byte (0 = Zeit abgelaufen, 1 = verloren, 2 = gefangen, 3 = perfekt). */
    private static final ResourceLocation TIDE_MINIGAME_SERVER = new ResourceLocation("tide", "minigame_server");

    public static final float DT = 0.05f;
    public static final float GREEN_START = 12f, YELLOW_START = 75f, RED_START = 90f;

    private static FightGame current;

    // Fisch (aus Tide)
    private final float strength;   // 0..1, wie stark der Fisch ist
    private final float speed;      // Bewegungen pro Sekunde
    private final int display;      // 0 Wasser, 1 Lava, 2 Void

    // Zustand
    public float tension = 35f, prevTension = 35f;
    public float progress = 0f, prevProgress = 0f;
    private float stamina = 1f;
    private float surge = 0f, surgeTimer = 1.2f;
    private float overTime = 0f, slackTime = 0f, grace = 0.35f;
    private float maxTension = 0f;
    private int tickCount = 0;
    private Influences inf = new Influences();
    private final RandomSource random = RandomSource.create();

    public enum End { NONE, CAUGHT, SNAPPED, ESCAPED, CANCELLED }
    public End end = End.NONE;
    public float endTime = 0f;
    public float age = 0f;

    private FightGame(float strength, float speed, int display) {
        this.strength = Mth.clamp(strength, 0f, 0.95f);
        this.speed = Mth.clamp(speed, 0.1f, 4f);
        this.display = display;
    }

    public static FightGame current() {
        return current;
    }

    /** Läuft gerade ein Drill, der Eingaben braucht? */
    public static boolean isFighting() {
        return current != null && current.end == End.NONE;
    }

    /** Wird aus dem Tide-Mixin aufgerufen, wenn ein Fisch angebissen hat. */
    public static void start(byte display, byte behavior, float area, float speedParam) {
        float strength = 1f - area;                 // Tide: area = 1 - Fischstärke (inkl. Leine)
        float speed = speedParam * 20f;             // Tide: speed = Fischtempo / 20
        current = new FightGame(strength, speed, display);
        current.inf = Influences.sample(Minecraft.getInstance(), display);
    }

    /** Tide hat das Minispiel serverseitig beendet (z. B. Schwimmer weg). */
    public static void serverClosed() {
        if (isFighting()) current.finish(End.CANCELLED, false);
    }

    public static void clientTick(Minecraft mc) {
        if (current == null) return;
        if (mc.isPaused()) return;
        current.tick(mc);
    }

    private void tick(Minecraft mc) {
        prevTension = tension;
        prevProgress = progress;
        age += DT;

        if (end != End.NONE) {
            endTime += DT;
            if (endTime > 1.0f && current == this) current = null;
            return;
        }

        LocalPlayer player = mc.player;
        if (player == null || player.fishing == null) {
            finish(End.CANCELLED, true);
            return;
        }

        if (++tickCount % 20 == 0) inf = Influences.sample(mc, display);

        if (grace > 0) grace -= DT;
        boolean hold = grace <= 0 && mc.screen == null && mc.options.keyUse.isDown();

        // Zugphasen des Fisches: ruhige Momente und plötzliche Zugstöße
        surgeTimer -= DT;
        if (surgeTimer <= 0) {
            if (surge > 0) {
                surge = 0;
                surgeTimer = (1.0f + random.nextFloat() * 2.5f) / inf.aggression / (0.6f + speed * 0.4f);
            } else {
                surge = (0.6f + random.nextFloat() * 0.8f) * (0.6f + strength) * inf.aggression;
                surgeTimer = 0.4f + random.nextFloat() * (0.6f + strength);
            }
        }

        float tired = 0.35f + 0.65f * stamina;
        float pull = (0.25f + strength * 0.9f + surge) * tired * inf.aggression + inf.current;

        if (hold) {
            tension += (20f + pull * 28f) / inf.rodFactor * DT;
            progress += Math.max(0.015f, 0.10f - pull * 0.03f) / inf.depthFactor * DT;
            if (tickCount % 4 == 0)
                player.playSound(SoundEvents.FISHING_BOBBER_RETRIEVE, 0.12f, 1.5f + random.nextFloat() * 0.2f);
        } else {
            tension -= Math.max(6f, 30f - pull * 8f) * DT;
            progress -= pull * 0.035f * DT;
        }
        tension = Mth.clamp(tension, 0f, 100f);
        progress = Mth.clamp(progress, 0f, 1f);
        maxTension = Math.max(maxTension, tension);

        // Im grünen Bereich ermüdet der Fisch
        if (tension >= GREEN_START && tension < YELLOW_START)
            stamina -= (hold ? 0.09f : 0.04f) / (0.5f + strength) * inf.rodFactor * DT;
        stamina = Mth.clamp(stamina, 0f, 1f);

        // Warnknarzen, wenn es eng wird
        if (tension >= RED_START - 5 && tickCount % 6 == 0)
            player.playSound(SoundEvents.CROSSBOW_LOADING_MIDDLE, 0.35f, 0.55f);

        overTime = tension >= RED_START ? overTime + DT : 0f;
        slackTime = tension < 6f ? slackTime + DT : 0f;

        if (overTime > 0.4f) finish(End.SNAPPED, true);
        else if (slackTime > 2.5f) finish(End.ESCAPED, true);
        else if (progress >= 1f) finish(End.CAUGHT, true);
    }

    private void finish(End result, boolean notifyServer) {
        end = result;
        endTime = 0f;
        if (!notifyServer) return;

        byte event = switch (result) {
            case CAUGHT -> (byte) (maxTension < YELLOW_START ? 3 : 2);
            case SNAPPED, ESCAPED -> (byte) 1;
            default -> (byte) 0;
        };
        sendTide(event);

        if (result == End.SNAPPED && ClientPlayNetworking.canSend(AngelkampfMod.LINE_SNAPPED)) {
            ClientPlayNetworking.send(AngelkampfMod.LINE_SNAPPED, new FriendlyByteBuf(Unpooled.buffer()));
        }
    }

    private static void sendTide(byte event) {
        if (!ClientPlayNetworking.canSend(TIDE_MINIGAME_SERVER)) return;
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeByte(event);
        ClientPlayNetworking.send(TIDE_MINIGAME_SERVER, buf);
    }
}

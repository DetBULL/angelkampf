package de.angelkampf.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.angelkampf.AngelkampfMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Einfache JSON-Konfiguration unter config/angelkampf.json. */
public class AngelkampfConfig {
    public static final float MIN_SCALE = 0.5f, MAX_SCALE = 2.0f, DEFAULT_SCALE = 1.0f;
    public static final int MIN_OFFSET = 0, MAX_OFFSET = 120, DEFAULT_OFFSET = 0;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AngelkampfConfig instance = new AngelkampfConfig();

    /** Größe des Fensters (1.0 = Standard). */
    public float scale = DEFAULT_SCALE;
    /** Zusätzlicher Abstand nach oben, in GUI-Pixeln. */
    public int offset = DEFAULT_OFFSET;

    public static AngelkampfConfig get() {
        return instance;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("angelkampf.json");
    }

    public static void load() {
        Path p = path();
        if (Files.exists(p)) {
            try (Reader r = Files.newBufferedReader(p)) {
                AngelkampfConfig loaded = GSON.fromJson(r, AngelkampfConfig.class);
                if (loaded != null) instance = loaded;
            } catch (Exception e) {
                AngelkampfMod.LOG.warn("Konnte angelkampf.json nicht lesen, nutze Standardwerte", e);
            }
        }
        instance.clamp();
        save();
    }

    public static void save() {
        instance.clamp();
        try {
            Files.createDirectories(path().getParent());
            try (Writer w = Files.newBufferedWriter(path())) {
                GSON.toJson(instance, w);
            }
        } catch (Exception e) {
            AngelkampfMod.LOG.warn("Konnte angelkampf.json nicht speichern", e);
        }
    }

    private void clamp() {
        scale = Mth.clamp(scale, MIN_SCALE, MAX_SCALE);
        offset = Mth.clamp(offset, MIN_OFFSET, MAX_OFFSET);
    }
}

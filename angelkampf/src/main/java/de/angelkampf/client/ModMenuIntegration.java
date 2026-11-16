package de.angelkampf.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Einstellungs-Button in der Modliste (nur aktiv, wenn Mod Menu installiert ist). */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return AngelkampfConfigScreen::new;
    }
}

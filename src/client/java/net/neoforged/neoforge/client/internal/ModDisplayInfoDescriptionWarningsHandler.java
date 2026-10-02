/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.internal;

import static java.util.Collections.singletonList;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import net.neoforged.neoforge.common.NeoForgeMod;

@EventBusSubscriber(Dist.CLIENT)
@Mod(value = NeoForgeMod.MOD_ID, dist = Dist.CLIENT)
public class ModDisplayInfoDescriptionWarningsHandler {
    private static final boolean HIDE_WARNING_SCREEN = Boolean.getBoolean("neoforge.warnings.mods.info.description.hide");

    @SubscribeEvent
    public static void onResourceLoadFinished(ClientResourceLoadFinishedEvent event) {
        // Only run once
        if (!event.isInitial()) return;

        // Skip if in production or if the property to skip the warnings is set
        if (FMLEnvironment.isProduction() || HIDE_WARNING_SCREEN) return;

        var languages = getLanguages();

        ModList.get().getMods().forEach(info -> {
            var modId = info.getModId();

            // See DefaultModDisplayInfo#description()
            var oldKey = "fml.menu.mods.info.description." + modId;
            var newKey = "neoforge.screen.mods.info.description." + modId;

            // Warn for languages that translate the old key, unless they also translate the new key
            var langsToWarn = languages.stream()
                    .filter(entry -> entry.getValue().has(oldKey) && !entry.getValue().has(newKey))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            if (langsToWarn.isEmpty()) return;

            // This shouldn't need to be translated, as it will only ever show for developers
            var warning = ModLoadingIssue.warning(
                    "Mod %s (%s) uses the deprecated `%s` translation key; change to `%s`",
                    info.getModId(),
                    String.join(", ", langsToWarn),
                    oldKey,
                    newKey);

            //noinspection UnstableApiUsage
            ModLoader.addLoadingIssue(warning.withAffectedMod(info));
        });
    }

    /// Get each language currently loaded by Minecraft.
    /// Unlike the vanilla localization system, languages are not stacked on top of `en_us`.
    private static Set<Map.Entry<String, ? extends Language>> getLanguages() {
        var mc = Minecraft.getInstance();
        var resourceManager = mc.getResourceManager();
        var languageManager = mc.getLanguageManager();

        return languageManager.getLanguages()
                .keySet()
                .stream()
                .map(code -> Map.entry(code, ClientLanguage.loadFrom(resourceManager, singletonList(code), false)))
                .collect(Collectors.toSet());
    }
}

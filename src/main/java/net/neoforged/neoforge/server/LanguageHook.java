/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.i18n.I18nManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

/**
 * Loads the built-in language files, and handles loading the default language ({@value Language#DEFAULT}) on the
 * dedicated server.
 */
@ApiStatus.Internal
public class LanguageHook {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Map<String, String> defaultLanguageTable = new HashMap<>();
    private static Map<String, Component> defaultLanguageComponentTable = new HashMap<>();
    private static Map<String, String> modTable = new HashMap<>();
    private static Map<String, Component> modComponentTable = new HashMap<>();

    public static void captureLanguageMap(Map<String, String> table, Map<String, Component> componentTable) {
        defaultLanguageTable = table;
        defaultLanguageComponentTable = componentTable;
        if (!modTable.isEmpty()) {
            defaultLanguageTable.putAll(modTable);
        }
        if (!modComponentTable.isEmpty()) {
            defaultLanguageComponentTable.putAll(modComponentTable);
        }
    }

    private static void loadLanguage(String langName, MinecraftServer server) {
        String langFile = String.format(Locale.ROOT, "lang/%s.json", langName);
        // noinspection resource
        ResourceManager resourceManager = server.getServerResources().resourceManager();
        // We cannot use the resource manager itself, because it is specifically scoped to data packs
        // (the PackType given to MultiPackResourceManager is SERVER_DATA)
        // Instead, we create a MultiPackResourceManager configured for PackType.CLIENT_RESOURCES
        // (We must not close this manager, as closing it would close all of its contained packs)
        // noinspection resource
        ResourceManager clientResources = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, resourceManager.listPacks().toList());
        int loaded = 0;
        for (String namespace : clientResources.getNamespaces()) {
            try {
                modTable.putAll(I18nManager.loadTranslations(langName));
                ResourceLocation langResource = ResourceLocation.fromNamespaceAndPath(namespace, langFile);
                for (Resource resource : clientResources.getResourceStack(langResource)) {
                    try (InputStream stream = resource.open()) {
                        Language.loadFromJson(stream, (key, value) -> modTable.put(key, value), (key, value) -> modComponentTable.put(key, value));
                    }
                }
                loaded++;
            } catch (Exception exception) {
                LOGGER.warn("Skipped language file: {}:{}", namespace, langFile, exception);
            }
        }
        LOGGER.debug("Loaded {} language files for {}", loaded, langName);
    }

    public static void loadBuiltinLanguages() {
        modTable = new HashMap<>(5000);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream input = classLoader.getResourceAsStream("assets/minecraft/lang/en_us.json")) {
            assert input != null;
            Language.loadFromJson(input, (key, value) -> modTable.put(key, value), (key, value) -> modComponentTable.put(key, value));
        } catch (Exception exception) {
            LOGGER.warn("Failed to load built-in language file for Minecraft", exception);
        }

        try (InputStream input = classLoader.getResourceAsStream("assets/neoforge/lang/en_us.json")) {
            assert input != null;
            Language.loadFromJson(input, (key, value) -> modTable.put(key, value), (key, value) -> modComponentTable.put(key, value));
        } catch (Exception exception) {
            LOGGER.warn("Failed to load built-in language file for NeoForge", exception);
        }

        modTable.putAll(I18nManager.loadTranslations("en_us"));

        defaultLanguageTable.putAll(modTable);
        I18nManager.injectTranslations(modTable);
    }

    static void loadModLanguages(MinecraftServer server) {
        modTable = new HashMap<>(5000);
        modComponentTable = new HashMap<>();
        loadLanguage("en_us", server);
        defaultLanguageTable.putAll(modTable);
        defaultLanguageComponentTable.putAll(modComponentTable);
        I18nManager.injectTranslations(modTable);
    }
}

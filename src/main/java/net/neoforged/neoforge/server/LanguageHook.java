/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.common.I18nExtension;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LanguageHook {
    private static final Logger LOGGER = LogManager.getLogger();
    private static List<Map<String, String>> capturedTables = new ArrayList<>(2);
    private static Map<String, String> modTable;

    /**
     * Loads lang files on the server
     */
    public static void captureLanguageMap(Map<String, String> table) {
        capturedTables.add(table);
        if (modTable != null) {
            capturedTables.forEach(t -> t.putAll(modTable));
        }
    }

    private static void loadLocaleData(final InputStream inputstream) {
        try {
            Language.loadFromJson(inputstream, (key, value) -> modTable.put(key, value));
        } finally {
            IOUtils.closeQuietly(inputstream);
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
                ResourceLocation langResource = new ResourceLocation(namespace, langFile);
                for (Resource resource : clientResources.getResourceStack(langResource)) {
                    loadLocaleData(resource.open());
                }
                loaded++;
            } catch (Exception exception) {
                LOGGER.warn("Skipped language file: {}:{}", namespace, langFile, exception);
            }
        }
        LOGGER.debug("Loaded {} language files for {}", loaded, langName);
    }

    public static void loadForgeAndMCLangs() {
        modTable = new HashMap<>(5000);
        final InputStream mc = Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/minecraft/lang/en_us.json");
        final InputStream forge = Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/neoforge/lang/en_us.json");
        loadLocaleData(mc);
        loadLocaleData(forge);
        capturedTables.forEach(t -> t.putAll(modTable));
        I18nExtension.loadLanguageData(modTable);
    }

    static void loadLanguagesOnServer(MinecraftServer server) {
        modTable = new HashMap<>(5000);
        // Possible multi-language server support?
        for (String lang : Arrays.asList("en_us")) {
            loadLanguage(lang, server);
        }
        capturedTables.forEach(t -> t.putAll(modTable));
        I18nExtension.loadLanguageData(modTable);
    }
}

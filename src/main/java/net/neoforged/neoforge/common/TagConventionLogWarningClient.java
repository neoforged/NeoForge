/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TagConventionLogWarningClient {
    private TagConventionLogWarningClient() {}

    private static final Logger LOGGER = LogManager.getLogger();

    /*package private*/
    static void init() {
        IEventBus forgeBus = NeoForge.EVENT_BUS;

        TagConventionLogWarningClient.setupUntranslatedItemTagWarning(forgeBus);
    }

    /*package private*/
    static void setupUntranslatedItemTagWarning(IEventBus forgeBus) {
        // Log missing item tag translations only in integrated server so we can safely get translations.
        forgeBus.addListener((ServerStartingEvent serverStartingEvent) -> {
            // We have to wait for server start to read the server config.
            TagConventionLogWarning.LogWarningMode untranslatedTagWarningMode = NeoForgeConfig.COMMON.logUntranslatedItemTagWarnings.get();
            if (FMLEnvironment.dist == Dist.CLIENT && untranslatedTagWarningMode != TagConventionLogWarning.LogWarningMode.SILENCED) {
                boolean isConfigSetToDev = untranslatedTagWarningMode == TagConventionLogWarning.LogWarningMode.DEV_SHORT ||
                        untranslatedTagWarningMode == TagConventionLogWarning.LogWarningMode.DEV_VERBOSE;

                if (!FMLLoader.isProduction() == isConfigSetToDev) {
                    List<TagKey<?>> untranslatedTags = new ObjectArrayList<>();
                    RegistryAccess.Frozen registryAccess = serverStartingEvent.getServer().registryAccess();
                    extractUnregisteredModdedTags(registryAccess.registryOrThrow(Registries.ITEM), untranslatedTags);

                    if (!untranslatedTags.isEmpty()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("""
                                \n	Dev warning - Untranslated Item Tags detected. Please translate your item tags so other mods such as recipe viewers can properly display your tag's name.
                                    The format desired is tag.item.<namespace>.<path> for the translation key with slashes in path turned into periods.
                                    You can see a shortened version of this message by setting logUntranslatedItemTagWarnings in NeoForge's common config to "DEV_SHORT".
                                    You can see individual untranslated item tags by setting logUntranslatedItemTagWarnings in NeoForge's common config to "DEV_VERBOSE".
                                    Default is "SILENCED" to hide this message.
                                """);

                        // Print out all untranslated tags when desired.
                        boolean isConfigSetToVerbose = untranslatedTagWarningMode == TagConventionLogWarning.LogWarningMode.DEV_VERBOSE ||
                                untranslatedTagWarningMode == TagConventionLogWarning.LogWarningMode.PROD_VERBOSE;

                        if (isConfigSetToVerbose) {
                            stringBuilder.append("\nUntranslated item tags:");
                            for (TagKey<?> tagKey : untranslatedTags) {
                                stringBuilder.append("\n     ").append(tagKey.location());
                            }
                        }

                        LOGGER.warn(stringBuilder);
                    }
                }
            }
        });
    }

    private static void extractUnregisteredModdedTags(Registry<?> registry, List<TagKey<?>> untranslatedTags) {
        registry.getTagNames().forEach(itemTagKey -> {
            // We do not translate vanilla's tags at this moment.
            if (itemTagKey.location().getNamespace().equals("minecraft")) {
                return;
            }

            String translationKey = Tags.getTagTranslationKey(itemTagKey);
            if (!I18n.exists(translationKey)) {
                untranslatedTags.add(itemTagKey);
            }
        });
    }
}

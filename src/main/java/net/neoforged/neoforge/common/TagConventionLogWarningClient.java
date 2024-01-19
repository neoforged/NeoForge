/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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
            TagConventionLogWarning.LOG_WARNING_MODES untranslatedTagWarningMode = NeoForgeConfig.COMMON.logUntranslatedItemTagWarnings.get();
            if (FMLEnvironment.dist == Dist.CLIENT && untranslatedTagWarningMode != TagConventionLogWarning.LOG_WARNING_MODES.SILENCED) {
                boolean isConfigSetToDev = untranslatedTagWarningMode == TagConventionLogWarning.LOG_WARNING_MODES.DEV_SHORT ||
                        untranslatedTagWarningMode == TagConventionLogWarning.LOG_WARNING_MODES.DEV_VERBOSE;

                if (!FMLLoader.isProduction() == isConfigSetToDev) {
                    Registry<Item> itemRegistry = serverStartingEvent.getServer().registryAccess().registryOrThrow(Registries.ITEM);
                    List<TagKey<Item>> untranslatedItemTags = new ObjectArrayList<>();
                    itemRegistry.getTagNames().forEach(itemTagKey -> {
                        // We do not translate vanilla's tags at this moment.
                        if (itemTagKey.location().getNamespace().equals("minecraft")) {
                            return;
                        }

                        String translationKey = Tags.getTagTranslationKey(itemTagKey);
                        if (!I18n.exists(translationKey)) {
                            untranslatedItemTags.add(itemTagKey);
                        }
                    });

                    if (!untranslatedItemTags.isEmpty()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("""
                                \n	Dev warning - Untranslated Item Tags detected. Please translate your item tags so other mods such as recipe viewers can properly display your tag's name.
                                    The format desired is tag.item.<namespace>.<path> for the translation key with slashes in path turned into periods.
                                    You can disable this message in Neoforge's common config by setting logUntranslatedItemTagWarnings to "SILENCED" or see individual tags with "DEV_VERBOSE".
                                """);

                        // Print out all untranslated tags when desired.
                        boolean isConfigSetToVerbose = untranslatedTagWarningMode == TagConventionLogWarning.LOG_WARNING_MODES.DEV_VERBOSE ||
                                untranslatedTagWarningMode == TagConventionLogWarning.LOG_WARNING_MODES.PROD_VERBOSE;

                        if (isConfigSetToVerbose) {
                            stringBuilder.append("\nUntranslated item tags:");
                            for (TagKey<Item> tagKey : untranslatedItemTags) {
                                stringBuilder.append("\n     ").append(tagKey.location());
                            }
                        }

                        LOGGER.warn(stringBuilder);
                    }
                }
            }
        });
    }
}

/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

/**
 * Log lack of referralUrl in [[dependences.modid]] section warnings
 * 
 * @author HowXu {@code <dev@howxu.cn>}
 */
@ApiStatus.Internal
@SuppressWarnings("unused")
@Mod(NeoForgeMod.MOD_ID)
public class ReferralUrlWarningsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ReferralUrlWarningsHandler(ModContainer container) {
        if (!FMLEnvironment.isProduction()) {
            ModList.get().forEachModFile(file -> {
                if (file.getModInfos().stream().anyMatch(info -> info.getModId().equals("minecraft"))) {
                    return;
                }
                file.getModInfos().forEach(info -> {
                    if (info.getModId().equals(NeoForgeMod.MOD_ID)) return;
                    info.getDependencies().forEach(dep -> {
                        if (dep.getReferralURL().isEmpty()) {
                            LOGGER.warn(
                                    "ReferralUrlWarnings: Mod '{}' declares a dependency on '{}' without a referralUrl. "
                                            + "Adding 'referralUrl = \"...\"' under [[dependencies.{}]] in mods.toml lets tools "
                                            + "offer users a download link if this mod is missing.",
                                    info.getModId(), dep.getModId(), dep.getModId());
                        }
                    });
                });
            });
        }
    }
}

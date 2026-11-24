/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.slf4j.Logger;

@Mod(NeoForgeMod.MOD_ID)
public class LogoFileWarningsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean HIDE_WARNING_SCREEN = Boolean.getBoolean("neoforge.warnings.logofile.hide");

    public LogoFileWarningsHandler() {
        // Skip if in production or if the property to skip the warnings is set
        if (FMLEnvironment.isProduction() || HIDE_WARNING_SCREEN) return;

        ModList.get().getMods().forEach(info -> {
            // See DefaultModDisplayInfo#banner()
            var logoFile = info.getLogoFile()
                    .or(() -> info.getOwningFile().getConfig().getConfigElement("logoFile"));

            // If logoFile is not set, there is nothing to warn about
            if (logoFile.isEmpty()) return;

            var bannerFile = info.getConfig().getConfigElement("bannerFile")
                    .or(() -> info.getOwningFile().getConfig().getConfigElement("bannerFile"));
            // See DefaultModDisplayInfo#icon()
            var iconFile = info.getConfig().getConfigElement("iconFile")
                    .or(() -> info.getOwningFile().getConfig().getConfigElement("iconFile"));

            // Skip warning if bannerFile or iconFile is present, since it means the developer consciously kept the old property (for multi-version compat)
            if (bannerFile.isEmpty() && iconFile.isEmpty()) {
                // Log a warning instead of a loading warning, since we're on a well-established version
                LOGGER.warn("Mod {} uses the deprecated `logoFile` property; change to `bannerFile` and/or (for square icons) `iconFile`", info.getModId());
            }
        });
    }
}

/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForgeMod;

@Mod(NeoForgeMod.MOD_ID)
public class LogoFileWarningsHandler {
    private static final boolean HIDE_WARNING_SCREEN = Boolean.getBoolean("neoforge.warnings.logofile.hide");

    public LogoFileWarningsHandler() {
        // Skip if in production or if the property to skip the warnings is set
        if (FMLEnvironment.isProduction() || HIDE_WARNING_SCREEN) return;

        ModList.get().getMods().forEach(info -> {
            // See DefaultModDisplayInfo#banner()
            var logoFile = info.getLogoFile()
                    .or(() -> info.getOwningFile().getConfig().getConfigElement("logoFile"));

            if (logoFile.isPresent()) {
                // This shouldn't need to be translated, as it will only ever show for developers
                //noinspection UnstableApiUsage
                ModLoader.addLoadingIssue(ModLoadingIssue.warning("Mod %s uses the deprecated `logoFile` property; change to `bannerFile` and/or (for square icons) `iconFile`", info.getModId()).withAffectedMod(info));
            }
        });
    }
}

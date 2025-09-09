/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.junit;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.startup.JUnitGameBootstrapper;

public class JUnitMain implements JUnitGameBootstrapper {
    @Override
    public void bootstrap(FMLLoader fmlLoader) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        // Load mods
        net.neoforged.neoforge.server.loading.ServerModLoader.load(false);
    }
}

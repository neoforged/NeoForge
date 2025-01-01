/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.junit;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.startup.JUnitGameBootstrapper;

public class JUnitBootstrapper implements JUnitGameBootstrapper {
    @Override
    public void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        // Load mods
        net.neoforged.neoforge.server.loading.ServerModLoader.load();
    }
}

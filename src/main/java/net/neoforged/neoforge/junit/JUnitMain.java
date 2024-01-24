/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.junit;

import com.mojang.logging.LogUtils;
import cpw.mods.modlauncher.Launcher;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.api.distmarker.Dist;
import org.slf4j.Logger;

public class JUnitMain {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        // Load mods
        net.neoforged.neoforge.server.loading.ServerModLoader.load();

        Consumer<Dist> extension = Launcher.INSTANCE.environment().findLaunchPlugin("runtimedistcleaner")
                .orElseThrow().getExtension();
        extension.accept(Dist.CLIENT);
    }
}

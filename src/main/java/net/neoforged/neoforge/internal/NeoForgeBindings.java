/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IBindingsProvider;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.ConfigSync;
import org.jetbrains.annotations.Nullable;

public class NeoForgeBindings implements IBindingsProvider {
    @Override
    public IEventBus getGameBus() {
        return NeoForge.EVENT_BUS;
    }

    @Override
    public void onConfigChanged(ModConfig modConfig, @Nullable IConfigSpec.ILoadedConfig loadedConfig) {
        if (modConfig.getType() != ModConfig.Type.SERVER || loadedConfig == null) {
            return;
        }

        ConfigSync.addPendingConfig(modConfig, loadedConfig);
    }
}

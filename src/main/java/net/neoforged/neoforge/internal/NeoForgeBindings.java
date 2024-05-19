/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IBindingsProvider;
import net.neoforged.fml.config.IConfigEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoForgeBindings implements IBindingsProvider {
    @Override
    public IEventBus getGameBus() {
        return NeoForge.EVENT_BUS;
    }

    @Override
    public IConfigEvent.ConfigConfig getConfigConfiguration() {
        return new IConfigEvent.ConfigConfig(ModConfigEvent.Loading::new, ModConfigEvent.Reloading::new, ModConfigEvent.Unloading::new);
    }
}

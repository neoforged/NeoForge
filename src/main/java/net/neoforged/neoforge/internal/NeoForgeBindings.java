/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IBindingsProvider;
import net.neoforged.neoforge.common.NeoForge;

public class NeoForgeBindings implements IBindingsProvider {
    @Override
    public IEventBus getGameBus() {
        return NeoForge.EVENT_BUS;
    }
}

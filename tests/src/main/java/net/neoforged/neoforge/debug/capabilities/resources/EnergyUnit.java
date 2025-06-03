/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.resources;

import net.neoforged.neoforge.transfer.resources.IResource;

public final class EnergyUnit implements IResource {
    public static final EnergyUnit INSTANCE = new EnergyUnit();

    @Override
    public boolean isEmpty() {
        return false;
    }

    private EnergyUnit() {}
}

/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.io.File;

public class DummySavedData extends SavedData {
    public static final DummySavedData DUMMY = new DummySavedData();

    private DummySavedData() {
        super();
    }

    @Override
    public CompoundTag save(final CompoundTag compound, HolderLookup.Provider provider) {
        // NOOP
        return null;
    }

    @Override
    public void save(final File file, final HolderLookup.Provider provider) {
        // Do nothing, because we don't want to overwrite the saved data
        // that may already be here.
    }
}

/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.mixins;

import java.util.Map;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DebugScreenEntries.class)
public interface DebugScreenEntriesAccessor {
    @Accessor("PROFILES")
    @Mutable
    static void neoforge$setProfiles(Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> profiles) {
        throw new IllegalCallerException("Mixin not injected!");
    }

    @Accessor("ENTRIES_BY_ID")
    @Mutable
    static Map<Identifier, DebugScreenEntry> neoforge$getEntriesById() {
        throw new IllegalCallerException("Mixin not injected!");
    }
}

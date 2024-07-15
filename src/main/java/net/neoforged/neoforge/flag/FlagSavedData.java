/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

final class FlagSavedData extends SavedData {
    private static final Factory<FlagSavedData> FACTORY = new Factory<>(FlagSavedData::new, FlagSavedData::new);

    private FlagSavedData() {}

    private FlagSavedData(CompoundTag tag, HolderLookup.Provider provider) {
        var flags = new Object2BooleanOpenHashMap<ResourceLocation>();

        for (var key : tag.getAllKeys()) {
            var flag = ResourceLocation.parse(key);
            var enabled = tag.getBoolean(key);
            flags.put(flag, enabled);
        }

        if (FlagManager.INSTANCE.setEnabled(flags, false))
            FlagManager.INSTANCE.markDirty(false, true);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        FlagManager.INSTANCE.enabledFlags.forEach((flag, enabled) -> {
            var key = flag.toString();
            tag.putBoolean(key, enabled);
        });

        return tag;
    }

    static void accept(Consumer<FlagSavedData> action) {
        var server = ServerLifecycleHooks.getCurrentServer();

        if (server != null)
            action.accept(get(server));
    }

    static FlagSavedData get(MinecraftServer server) {
        var storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(FACTORY, "modded_feature_flags");
    }
}

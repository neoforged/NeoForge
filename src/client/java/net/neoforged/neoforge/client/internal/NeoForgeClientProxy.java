/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.internal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.internal.NeoForgeProxy;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class NeoForgeClientProxy extends NeoForgeProxy {
    @Override
    public BlockableEventLoop<Runnable> getClientExecutor() {
        return Minecraft.getInstance();
    }

    @Override
    public TooltipFlag getTooltipFlag() {
        return Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
    }

    @Override
    @Nullable
    public <T> HolderLookup.RegistryLookup<T> resolveLookup(ResourceKey<? extends Registry<T>> key) {
        var lookup = super.resolveLookup(key);
        if (lookup == null) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                return level.registryAccess().lookup(key).orElse(null);
            }
        }
        return lookup;
    }
}

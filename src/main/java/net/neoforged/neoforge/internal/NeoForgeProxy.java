/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Allows common code to call client-only methods, through {@code NeoForgeClientProxy}.
 *
 * <p>Try not to add methods to this class, there are generally better ways to
 * handle this kind of thing, possibly through different API design.
 */
@ApiStatus.Internal // Already internal package, but let's be doubly clear
public class NeoForgeProxy {
    public static final NeoForgeProxy INSTANCE = instantiate();

    private static NeoForgeProxy instantiate() {
        return switch (FMLEnvironment.getDist()) {
            case CLIENT -> {
                try {
                    yield (NeoForgeProxy) Class.forName("net.neoforged.neoforge.client.internal.NeoForgeClientProxy").getConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to instantiate client proxy", e);
                }
            }
            case DEDICATED_SERVER -> new NeoForgeProxy();
        };
    }

    public BlockableEventLoop<Runnable> getClientExecutor() {
        throw new UnsupportedOperationException("Cannot access client on the server");
    }

    public TooltipFlag getTooltipFlag() {
        return TooltipFlag.NORMAL;
    }

    @Nullable
    public <T> HolderLookup.RegistryLookup<T> resolveLookup(ResourceKey<? extends Registry<T>> key) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess().lookup(key).orElse(null);
        }
        return null;
    }
}

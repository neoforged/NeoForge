/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.ClientDispatchPayload;
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
        return switch (FMLLoader.getDist()) {
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

    public void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        throw new UnsupportedOperationException("Cannot send serverbound payloads on the server");
    }

    public IPayloadContext newClientPayloadContext(ClientCommonPacketListener listener, ResourceLocation payloadId) {
        throw new UnsupportedOperationException("Cannot create ClientPayloadContext on the server");
    }

    public void handleClientPayload(ClientDispatchPayload payload, IPayloadContext context) {
        throw new UnsupportedOperationException("Cannot handle client payload on the server");
    }

    public void reloadRenderer() {
        throw new UnsupportedOperationException("Cannot reload renderer on the server");
    }

    public BlockableEventLoop<Runnable> getClientExecutor() {
        throw new UnsupportedOperationException("Cannot access client on the server");
    }

    public TooltipFlag getTooltipFlag() {
        return TooltipFlag.NORMAL;
    }

    public PackResources createVanillaPackSource(File assetsDir, String assetIndex) {
        throw new UnsupportedOperationException("Cannot instantiate vanilla pack source on the server");
    }

    public RecipeBookType[] getFilteredRecipeBookTypeValues() {
        return RecipeBookType.values();
    }

    @Nullable
    public <T> HolderLookup.RegistryLookup<T> resolveLookup(ResourceKey<? extends Registry<T>> key) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess().lookup(key).orElse(null);
        }
        return null;
    }

    // First parameter: Supplier<Minecraft>
    // Returns: Supplier<LoadingOverlay>
    public Supplier<?> instantiateLoadingOverlay(Supplier<?> mc, Supplier<ReloadInstance> ri, Consumer<Optional<Throwable>> ex, boolean fadein) {
        throw new UnsupportedOperationException("Cannot instantiate loading overlay on the server");
    }
}

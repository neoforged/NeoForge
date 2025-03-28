/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.internal;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.client.network.handling.ClientPayloadContext;
import net.neoforged.neoforge.internal.NeoForgeProxy;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.ClientDispatchPayload;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class NeoForgeClientProxy extends NeoForgeProxy {
    @Override
    public void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        ClientPacketListener listener = Objects.requireNonNull(Minecraft.getInstance().getConnection());
        listener.send(payload);
        for (CustomPacketPayload otherPayload : payloads) {
            listener.send(otherPayload);
        }
    }

    @Override
    public IPayloadContext newClientPayloadContext(ClientCommonPacketListener listener, ResourceLocation payloadId) {
        return new ClientPayloadContext(listener, payloadId);
    }

    @Override
    public void handleClientPayload(ClientDispatchPayload payload, IPayloadContext context) {
        ClientPayloadHandler.dispatch(payload, context);
    }

    @Override
    public BlockableEventLoop<Runnable> getClientExecutor() {
        return Minecraft.getInstance();
    }

    @Override
    public TooltipFlag getTooltipFlag() {
        return Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
    }

    @Override
    public RecipeBookType[] getFilteredRecipeBookTypeValues() {
        return ClientHooks.getFilteredRecipeBookTypeValues();
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
        return null;
    }
}

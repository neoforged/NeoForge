/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Payload for the modded network query request
 *
 * @param configuration The configuration components
 * @param play          The play components
 */
@ApiStatus.Internal
public record ModdedNetworkQueryPayload(Set<ModdedNetworkQueryComponent> configuration, Set<ModdedNetworkQueryComponent> play) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation("register");
    public static final FriendlyByteBuf.Reader<? extends CustomPacketPayload> READER = ModdedNetworkQueryPayload::new;
    public ModdedNetworkQueryPayload() {
        this(Set.of(), Set.of());
    }

    public ModdedNetworkQueryPayload(FriendlyByteBuf byteBuf) {
        this(byteBuf.readCollection(HashSet::new, ModdedNetworkQueryComponent::new), byteBuf.readCollection(HashSet::new, ModdedNetworkQueryComponent::new));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeObjectCollection(configuration(), ModdedNetworkQueryComponent::write);
        buf.writeObjectCollection(play(), ModdedNetworkQueryComponent::write);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}

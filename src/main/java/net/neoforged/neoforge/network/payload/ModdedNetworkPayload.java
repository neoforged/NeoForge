/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ModdedNetworkPayload(Set<ModdedNetworkComponent> configuration, Set<ModdedNetworkComponent> play) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation("network");
    public static final FriendlyByteBuf.Reader<? extends CustomPacketPayload> READER = ModdedNetworkPayload::new;

    public ModdedNetworkPayload(FriendlyByteBuf buf) {
        this(buf.readSet(ModdedNetworkComponent::new), buf.readSet(ModdedNetworkComponent::new));
    }

    @Override
    public void write(FriendlyByteBuf p_294947_) {
        p_294947_.writeObjectSet(configuration(), ModdedNetworkComponent::write);
        p_294947_.writeObjectSet(play(), ModdedNetworkComponent::write);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}

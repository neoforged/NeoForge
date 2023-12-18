/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record ModdedNetworkQueryPayload(Set<ModdedNetworkQueryComponent> configuration, Set<ModdedNetworkQueryComponent> play) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("register");
    public static final FriendlyByteBuf.Reader<? extends CustomPacketPayload> READER = ModdedNetworkQueryPayload::new;
    
    public ModdedNetworkQueryPayload() {
        this(Set.of(), Set.of());
    }
    
    public ModdedNetworkQueryPayload(FriendlyByteBuf byteBuf) {
        this(byteBuf.readSet(ModdedNetworkQueryComponent::new), byteBuf.readSet(ModdedNetworkQueryComponent::new));
    }
    
    @Override
    public void write(FriendlyByteBuf p_294947_) {
        p_294947_.writeObjectSet(configuration(), ModdedNetworkQueryComponent::write);
        p_294947_.writeObjectSet(play(), ModdedNetworkQueryComponent::write);
    }
    
    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}

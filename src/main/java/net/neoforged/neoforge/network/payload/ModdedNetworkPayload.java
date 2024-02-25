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
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * A payload that contains the modded network configuration and play components.
 *
 * @param configuration The configuration components.
 * @param play          The play components.
 */
@ApiStatus.Internal
public record ModdedNetworkPayload(Set<ModdedNetworkComponent> configuration, Set<ModdedNetworkComponent> play) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "network");
    public static final FriendlyByteBuf.Reader<? extends CustomPacketPayload> READER = ModdedNetworkPayload::new;
    public ModdedNetworkPayload(FriendlyByteBuf buf) {
        this(buf.readCollection(HashSet::new, ModdedNetworkComponent::new), buf.readCollection(HashSet::new, ModdedNetworkComponent::new));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeObjectCollection(configuration(), ModdedNetworkComponent::write);
        buf.writeObjectCollection(play(), ModdedNetworkComponent::write);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}

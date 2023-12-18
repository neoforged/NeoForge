/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.NotNull;

public record ConfigFilePayload(byte[] contents, String fileName) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "config_file");

    public ConfigFilePayload(FriendlyByteBuf buf) {
        this(buf.readByteArray(), buf.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByteArray(contents);
        buf.writeUtf(fileName);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}

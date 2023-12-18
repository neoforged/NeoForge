/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public record AdvancedOpenScreenPayload(
        int windowId,
        MenuType<?> menuType,
        Component name,
        byte[] additionalData) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "advanced_open_screen");

    public AdvancedOpenScreenPayload(int windowId, MenuType<?> menuType, Component name, Consumer<FriendlyByteBuf> dataWriter) {
        this(windowId, menuType, name, writeCustomData(dataWriter));
    }

    private static byte[] writeCustomData(Consumer<FriendlyByteBuf> dataWriter) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            dataWriter.accept(buf);
            return buf.array();
        } finally {
            buf.release();
        }
    }

    public AdvancedOpenScreenPayload(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readById(BuiltInRegistries.MENU), buffer.readComponentTrusted(), buffer.readByteArray());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(windowId());
        buffer.writeId(BuiltInRegistries.MENU, menuType());
        buffer.writeComponent(name());
        buffer.writeByteArray(additionalData());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}

/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * A custom payload that allows for opening screens with additional data.
 *
 * @param windowId       The window ID to use for the screen.
 * @param menuType       The menu type to open.
 * @param name           The name of the screen.
 * @param additionalData The additional data to pass to the screen.
 */
@ApiStatus.Internal
public record AdvancedOpenScreenPayload(
        int windowId,
        MenuType<?> menuType,
        Component name,
        byte[] additionalData) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "advanced_open_screen");
    public AdvancedOpenScreenPayload(int windowId, MenuType<?> menuType, Component name, Consumer<FriendlyByteBuf> dataWriter) {
        this(windowId, menuType, name, FriendlyByteBufUtil.writeCustomData(dataWriter));
    }

    public AdvancedOpenScreenPayload(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), Objects.requireNonNull(buffer.readById(BuiltInRegistries.MENU)), buffer.readComponentTrusted(), buffer.readByteArray());
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

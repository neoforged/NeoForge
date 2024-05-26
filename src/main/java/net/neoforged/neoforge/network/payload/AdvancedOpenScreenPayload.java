/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
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
public record AdvancedOpenScreenPayload(int windowId, MenuType<?> menuType, Component name, byte[] additionalData) implements CustomPacketPayload {
    public static final Type<AdvancedOpenScreenPayload> TYPE = new Type<>(new ResourceLocation(NeoForgeVersion.MOD_ID, "advanced_open_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedOpenScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            AdvancedOpenScreenPayload::windowId,
            ByteBufCodecs.idMapper(BuiltInRegistries.MENU),
            AdvancedOpenScreenPayload::menuType,
            ComponentSerialization.STREAM_CODEC,
            AdvancedOpenScreenPayload::name,
            NeoForgeStreamCodecs.UNBOUNDED_BYTE_ARRAY,
            AdvancedOpenScreenPayload::additionalData,
            AdvancedOpenScreenPayload::new);

    @Override
    public Type<AdvancedOpenScreenPayload> type() {
        return TYPE;
    }
}

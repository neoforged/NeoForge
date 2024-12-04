/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.configuration.CheckExtensibleEnums;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ExtensibleEnumDataPayload(Map<String, CheckExtensibleEnums.EnumEntry> enumEntries) implements CustomPacketPayload {
    public static final Type<ExtensibleEnumDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("neoforge", "extensible_enum_data"));
    public static final StreamCodec<ByteBuf, ExtensibleEnumDataPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, CheckExtensibleEnums.EnumEntry.STREAM_CODEC),
            ExtensibleEnumDataPayload::entries,
            ExtensibleEnumDataPayload::new);

    private ExtensibleEnumDataPayload(Collection<CheckExtensibleEnums.EnumEntry> entries) {
        this(entries.stream().collect(Collectors.toMap(CheckExtensibleEnums.EnumEntry::className, Function.identity())));
    }

    private Collection<CheckExtensibleEnums.EnumEntry> entries() {
        return enumEntries.values();
    }

    @Override
    public Type<ExtensibleEnumDataPayload> type() {
        return TYPE;
    }
}

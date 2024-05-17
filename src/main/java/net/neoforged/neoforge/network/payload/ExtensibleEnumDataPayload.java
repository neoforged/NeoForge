/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.IExtensibleEnum;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * A payload used to verify that specific Enums that implement {@linkplain IExtensibleEnum} have the same Enum Constants in the same order.
 */
@ApiStatus.Internal
public record ExtensibleEnumDataPayload<T extends Enum<?> & IExtensibleEnum>(Map<Class<? extends T>, EnumData> enums) implements CustomPacketPayload {
    public static final Type<ExtensibleEnumDataPayload<?>> TYPE = new Type<>(new ResourceLocation(NeoForgeVersion.MOD_ID, "extensible_enum_data"));
    public static final StreamCodec<FriendlyByteBuf, ExtensibleEnumDataPayload<?>> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            ExtensibleEnumDataPayload::enumClassNames,
            EnumData.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ExtensibleEnumDataPayload::enumValueNames,
            ExtensibleEnumDataPayload::of);

    @SuppressWarnings("unchecked")
    private static <T extends Enum<?> & IExtensibleEnum> ExtensibleEnumDataPayload<T> of(List<String> enumClassNames, List<EnumData> enumData) {
        Map<Class<? extends T>, EnumData> map = new HashMap<>();
        if (enumClassNames.size() != enumData.size()) {
            throw new IllegalArgumentException("different amount of enum classes and enum value lists during extensible enum sync");
        }
        for (int i = 0; i < enumClassNames.size(); i++) {
            String enumClassName = enumClassNames.get(i);
            EnumData data = enumData.get(i);
            try {
                Class<? extends T> enumClazz = (Class<? extends T>) Class.forName(enumClassName);
                if (!Enum.class.isAssignableFrom(enumClazz)) {
                    throw new IllegalStateException("Class " + enumClassName + " is not an Enum");
                }
                if (!IExtensibleEnum.class.isAssignableFrom(enumClazz)) {
                    throw new IllegalStateException("Class " + enumClassName + " is not IExtensible");
                }
                map.put(enumClazz, data);
            } catch (ClassNotFoundException cnfe) {
                throw new IllegalStateException("EnumClass " + enumClassName + " couldn't be found", cnfe);
            }
        }
        return new ExtensibleEnumDataPayload<>(map);
    }

    public static <T extends Enum<?> & IExtensibleEnum> ExtensibleEnumDataPayload<T> factory(List<Class<? extends T>> orderedEnums, List<Class<? extends T>> unorderedEnums) {
        Map<Class<? extends T>, EnumData> map = new HashMap<>();
        for (Class<? extends T> enumToVerify : orderedEnums) {
            map.put(enumToVerify, new EnumData(Arrays.stream(enumToVerify.getEnumConstants()).map(Enum::name).toList(), true));
        }
        for (Class<? extends T> enumToVerify : unorderedEnums) {
            map.put(enumToVerify, new EnumData(Arrays.stream(enumToVerify.getEnumConstants()).map(Enum::name).toList(), false));
        }
        return new ExtensibleEnumDataPayload<>(map);
    }

    @Override
    public Type<ExtensibleEnumDataPayload<?>> type() {
        return TYPE;
    }

    private List<String> enumClassNames() {
        return enums().keySet().stream().map(Class::getName).toList();
    }

    private List<EnumData> enumValueNames() {
        return enums().values().stream().toList();
    }

    public record EnumData(List<String> enumValues, boolean verifyOrder) {
        private static final StreamCodec<FriendlyByteBuf, EnumData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
                EnumData::enumValues,
                ByteBufCodecs.BOOL,
                EnumData::verifyOrder,
                EnumData::new);
    }
}

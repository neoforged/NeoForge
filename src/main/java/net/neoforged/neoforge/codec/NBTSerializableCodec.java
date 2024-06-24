/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.codec;

import static net.neoforged.neoforge.codec.NeoforgeNbtCodecs.TAG_CODEC;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * A codec for instances of {@link INBTSerializable}.
 *
 * @param defaultSupplier Used to construct empty instances.
 * @param serializerClass The class implementing {@link INBTSerializable}.
 * @param nbtTypeClass    The type of NBT tag the serializer serializes into.
 * @param serializer      Method reference ({@link INBTSerializable#serializeNBT(HolderLookup.Provider)})
 * @param deserializer    Method reference ({@link INBTSerializable#deserializeNBT(HolderLookup.Provider, Tag)})
 * @param <S>             Serializer type param.
 * @param <NBT>           NBT type param.
 */
public record NBTSerializableCodec<S extends INBTSerializable<NBT>, NBT extends Tag>(Supplier<S> defaultSupplier,
        Class<S> serializerClass,
        Class<NBT> nbtTypeClass,
        Method serializer,
        Method deserializer) implements Codec<S> {
    public static <S extends INBTSerializable<NBT>, NBT extends Tag> Codec<S> forSerializable(Class<S> serializerClass, Supplier<S> defaultSupplier) {
        try {
            final var nbtTypeClass = Arrays.stream(serializerClass.getGenericInterfaces())
                    .filter(ParameterizedType.class::isInstance)
                    .map(ParameterizedType.class::cast)
                    .filter(pt -> INBTSerializable.class == pt.getRawType())
                    .map(pt -> pt.getActualTypeArguments()[0])
                    .findFirst()
                    .orElseThrow();

            if (nbtTypeClass instanceof Class<?> nbt2) {
                final var serializer = serializerClass.getDeclaredMethod("serializeNBT", HolderLookup.Provider.class);
                final var deserializer = serializerClass.getDeclaredMethod("deserializeNBT", HolderLookup.Provider.class, nbt2);

                return new NBTSerializableCodec<>(defaultSupplier, serializerClass, (Class<NBT>) nbt2, serializer, deserializer);
            }

            throw new RuntimeException("The type of NBT serializable could not be determined! Do you implement it more than once?");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> ops, T input) {
        if (ops instanceof RegistryOps<T> regOps && regOps.lookupProvider instanceof RegistryOps.HolderLookupAdapter adapter) {
            return TAG_CODEC.decode(ops, input)
                    .result()
                    .map(asNBT -> decodeNBT(input, adapter, asNBT.getFirst()))
                    .orElse(DataResult.error(() -> "Failed to process input as NBT.", Pair.of(null, input)));
        } else {
            return DataResult.error(() -> "Was not passed registry ops for serialization; cannot continue.");
        }
    }

    private <T> DataResult<Pair<S, T>> decodeNBT(T input, RegistryOps.HolderLookupAdapter adapter, Tag asNBT) {
        if (nbtTypeClass.isInstance(asNBT)) {
            S instance = defaultSupplier.get();
            try {
                deserializer.invoke(instance, adapter.lookupProvider, asNBT);
                return DataResult.success(Pair.of(instance, input));
            } catch (Exception ex) {
                return DataResult.error(() -> "Error finalizing deserialization. Did not deserialize to the expected class.",
                        Pair.of(null, input));
            }
        } else {
            return DataResult.error(() -> "Not the proper NBT type.", Pair.of(null, input));
        }
    }

    @Override
    public <T> DataResult<T> encode(S input, DynamicOps<T> ops, T prefix) {
        if (ops instanceof RegistryOps<T> regOps && regOps.lookupProvider instanceof RegistryOps.HolderLookupAdapter adapter) {
            try {
                var serialized = serializer.invoke(input, adapter.lookupProvider);
                if (nbtTypeClass.isInstance(serialized)) {
                    return TAG_CODEC.encode(nbtTypeClass.cast(serialized), ops, prefix);
                } else {
                    return DataResult.error(() -> "Error serializing the data.");
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                return DataResult.error(() -> "Failed to call the serialization function.");
            }
        }

        return DataResult.error(() -> "Was not passed registry ops for serialization; cannot continue.");
    }
}

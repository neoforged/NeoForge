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

import java.util.function.Supplier;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * A codec for instances of {@link INBTSerializable}.
 *
 * @param defaultSupplier Used to construct empty instances.
 * @param <S>             Serializer type param.
 * @param <NBT>           NBT type param.
 */
public record NBTSerializableCodec<S extends INBTSerializable<NBT>, NBT extends Tag>(Supplier<S> defaultSupplier) implements Codec<S> {

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
        S instance = defaultSupplier.get();
        try {
            //noinspection unchecked
            instance.deserializeNBT(adapter.lookupProvider, (NBT) asNBT);
            // Any type issue from the above line will be caught and logged
            return DataResult.success(Pair.of(instance, input));
        } catch (Exception ex) {
            return DataResult.error(() -> "Error deserializing: " + ex + ".",
                    Pair.of(null, input));
        }
    }

    @Override
    public <T> DataResult<T> encode(S input, DynamicOps<T> ops, T prefix) {
        if (ops instanceof RegistryOps<T> regOps && regOps.lookupProvider instanceof RegistryOps.HolderLookupAdapter adapter) {
            try {
                var serialized = input.serializeNBT(adapter.lookupProvider);
                return TAG_CODEC.encode(serialized, ops, prefix);
            } catch (Exception ex) {
                return DataResult.error(() -> "Error serializing: " + ex + ".");
            }
        }

        return DataResult.error(() -> "Was not passed registry ops for serialization; cannot continue.");
    }
}

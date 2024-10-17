/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.codec.NBTSerializableCodec;
import org.jetbrains.annotations.UnknownNullability;

/**
 * An interface designed to unify various things in the Minecraft
 * code base that can be serialized to and from a NBT tag.
 */
public interface INBTSerializable<T extends Tag> {
    /**
     * Creates a codec for an implementation of {@link INBTSerializable}.
     *
     * @param defaultSupplier A default supplier for the class; typically a method reference.
     * @param <S>             Type of class implementing INBTSerializable.
     * @param <NBT>           Type of NBT tag the class serializes to.
     * @return A codec for the serializable class.
     */
    static <S extends INBTSerializable<NBT>, NBT extends Tag> Codec<S> codec(Supplier<S> defaultSupplier) {
        return new NBTSerializableCodec<>(defaultSupplier);
    }

    static <NBT extends Tag, S extends INBTSerializable<NBT>> StreamCodec<RegistryFriendlyByteBuf, S> streamCodec(Supplier<S> defaultSupplier) {
        return StreamCodec.of(
                (RegistryFriendlyByteBuf buffer, S serializable) -> {
                    var serialized = serializable.serializeNBT(buffer.registryAccess());
                    buffer.writeNbt(serialized);
                },

                (RegistryFriendlyByteBuf buffer) -> {
                    S instance = defaultSupplier.get();
                    //noinspection unchecked
                    final var nbt = (NBT) buffer.readNbt(NbtAccounter.unlimitedHeap());
                    instance.deserializeNBT(buffer.registryAccess(), nbt);
                    return instance;
                });
    }

    @UnknownNullability
    T serializeNBT(HolderLookup.Provider provider);

    void deserializeNBT(HolderLookup.Provider provider, T nbt);
}

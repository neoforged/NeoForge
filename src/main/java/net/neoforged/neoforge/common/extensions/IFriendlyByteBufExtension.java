/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.google.common.collect.Sets;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Additional helper methods for {@link FriendlyByteBuf}.
 */
public interface IFriendlyByteBufExtension {
    private FriendlyByteBuf self() {
        return (FriendlyByteBuf) this;
    }

    /**
     * Writes a FluidStack to the packet buffer, easy enough. If EMPTY, writes a FALSE.
     * This behavior provides parity with the ItemStack method in PacketBuffer.
     *
     * @param stack FluidStack to be written to the packet buffer.
     */
    default void writeFluidStack(FluidStack stack) {
        if (stack.isEmpty()) {
            self().writeBoolean(false);
        } else {
            self().writeBoolean(true);
            stack.writeToPacket(self());
        }
    }

    /**
     * Reads a FluidStack from this buffer.
     */
    default FluidStack readFluidStack() {
        return !self().readBoolean() ? FluidStack.EMPTY : FluidStack.readFromPacket(self());
    }

    /**
     * Reads the values from the current buffer using the given reader into a set.
     *
     * @param reader The reader to read the values from
     * @return The set containing the values
     * @param <T> The type of the entry
     */
    default <T> Set<T> readSet(Function<FriendlyByteBuf, T> reader) {
        Set<T> ret = Sets.newHashSet();
        readSet(reader, ret);
        return ret;
    }

    /**
     * Reads the values from the current buffer using the given reader and adds them to the given set.
     *
     * @param reader The reader to read the values from
     * @param target The set to add the values to
     * @param <T>    The type of the entry
     */
    default <T> void readSet(Function<FriendlyByteBuf, T> reader, Set<T> target) {
        addToCollection(reader, target::add);
    }

    /**
     * Reads the values from the current buffer using the given reader and adds them to the given collection.
     *
     * @param reader The reader to read the values from
     * @param adder  The consumer to add the values to
     * @param <T>    The type of the entry
     */
    default <T> void addToCollection(Function<FriendlyByteBuf, T> reader, Consumer<T> adder) {
        int size = self().readVarInt();
        for (int i = 0; i < size; i++) {
            adder.accept(reader.apply(self()));
        }
    }

    /**
     * Writes the entries in the given set to the buffer, by first writing the count and then writing each entry.
     *
     * @param set    The set to write
     * @param writer The writer to use for writing each entry
     * @param <T>    The type of the entry
     */
    default <T> void writeSet(Set<T> set, BiConsumer<FriendlyByteBuf, T> writer) {
        self().writeVarInt(set.size());
        for (T entry : set) {
            writer.accept(self(), entry);
        }
    }

    /**
     * Writes the entries in the given set to the buffer, by first writing the count and then writing each entry.
     *
     * @param set    The set to write
     * @param writer The writer to use for writing each entry
     * @param <T>    The type of the entry
     */
    default <T> void writeObjectSet(Set<T> set, BiConsumer<T, FriendlyByteBuf> writer) {
        self().writeVarInt(set.size());
        for (T entry : set) {
            writer.accept(entry, self());
        }
    }

    /**
     * Reads an optional int from the buffer.
     *
     * @return The optional int
     */
    default OptionalInt readOptionalInt() {
        if (!self().readBoolean())
            return OptionalInt.empty();

        return OptionalInt.of(self().readVarInt());
    }

    /**
     * Writes an optional int to the buffer.
     *
     * @param optionalInt The optional int
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default void writeOptionalInt(final OptionalInt optionalInt) {
        self().writeBoolean(optionalInt.isPresent());
        optionalInt.ifPresent(self()::writeVarInt);
    }
}

/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import org.apache.commons.lang3.function.TriConsumer;

/**
 * Additional helper methods for {@link FriendlyByteBuf}.
 */
public interface IFriendlyByteBufExtension {
    private FriendlyByteBuf self() {
        return (FriendlyByteBuf) this;
    }

    /**
     * Writes the entries in the given set to the buffer, by first writing the count and then writing each entry.
     *
     * @param set    The set to write
     * @param writer The writer to use for writing each entry
     * @param <T>    The type of the entry
     * @implNote This is a convenience method for {@link FriendlyByteBuf#writeCollection(Collection, StreamEncoder)}, where the callback can be a method on the entry type.
     */
    default <T> void writeObjectCollection(Collection<T> set, BiConsumer<T, FriendlyByteBuf> writer) {
        self().writeCollection(set, (buf, t) -> writer.accept(t, buf));
    }

    // TODO 1.20.5: either fix or remove
//    /**
//     * Reads an {@link ItemStack} from the current buffer, but allows for a larger count than the vanilla method, using a variable length int instead of a byte.
//     *
//     * @return The read stack
//     */
//    default ItemStack readItemWithLargeCount() {
//        if (!self().readBoolean()) {
//            return ItemStack.EMPTY;
//        } else {
//            Item item = self().readById(BuiltInRegistries.ITEM::byId);
//            int i = self().readVarInt();
//            return reconstructItemStack(item, i, self().readNbt());
//        }
//    }
//
//    /**
//     * Writes an {@link ItemStack} to the current buffer, but allows for a larger count than the vanilla method, using a variable length int instead of a byte.
//     *
//     * @param stack The stack to write
//     * @return The buffer
//     */
//    default FriendlyByteBuf writeItemWithLargeCount(ItemStack stack) {
//        if (stack.isEmpty()) {
//            self().writeBoolean(false);
//        } else {
//            self().writeBoolean(true);
//            Item item = stack.getItem();
//            self().writeById(BuiltInRegistries.ITEM::getId, item);
//            self().writeVarInt(stack.getCount());
//            CompoundTag compoundtag = new CompoundTag();
//            if (item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt()) {
//                compoundtag = stack.getTag();
//            }
//            compoundtag = addAttachmentsToTag(compoundtag, stack, false);
//
//            self().writeNbt(compoundtag);
//        }
//
//        return self();
//    }

    /**
     * Reads an array of objects from the buffer.
     *
     * @param builder A function that creates an array of the given size
     * @param reader  A function that reads an object from the buffer
     * @return The array of objects
     * @param <T> The type of the objects
     */
    default <T> T[] readArray(IntFunction<T[]> builder, StreamDecoder<? super FriendlyByteBuf, T> reader) {
        int size = self().readVarInt();
        T[] array = builder.apply(size);
        for (int i = 0; i < size; i++) {
            array[i] = reader.decode(self());
        }
        return array;
    }

    /**
     * Writes an array of objects to the buffer.
     *
     * @param array  The array of objects
     * @param writer A function that writes an object to the buffer
     * @return The buffer
     * @param <T> The type of the objects
     */
    default <T> FriendlyByteBuf writeArray(T[] array, StreamEncoder<? super FriendlyByteBuf, T> writer) {
        self().writeVarInt(array.length);
        for (T t : array) {
            writer.encode(self(), t);
        }
        return self();
    }

    /**
     * Writes a byte to the buffer
     *
     * @param value The value to be written
     * @return The buffer
     */
    default FriendlyByteBuf writeByte(byte value) {
        return self().writeByte((int) value);
    }

    /**
     * Variant of {@link FriendlyByteBuf#readMap(StreamDecoder, StreamDecoder)} that allows reading values
     * that depend on the key.
     */
    default <K, V> Map<K, V> readMap(StreamDecoder<? super FriendlyByteBuf, K> keyReader, BiFunction<FriendlyByteBuf, K, V> valueReader) {
        final int size = self().readVarInt();
        final Map<K, V> map = Maps.newHashMapWithExpectedSize(size);

        for (int i = 0; i < size; ++i) {
            final K k = keyReader.decode(self());
            map.put(k, valueReader.apply(self(), k));
        }

        return map;
    }

    /**
     * Variant of {@link FriendlyByteBuf#writeMap(Map, StreamEncoder, StreamEncoder)} that allows writing values
     * that depend on the key.
     */
    default <K, V> void writeMap(Map<K, V> map, StreamEncoder<? super FriendlyByteBuf, K> keyWriter, TriConsumer<FriendlyByteBuf, K, V> valueWriter) {
        self().writeVarInt(map.size());
        map.forEach((key, value) -> {
            keyWriter.encode(self(), key);
            valueWriter.accept(self(), key, value);
        });
    }
}

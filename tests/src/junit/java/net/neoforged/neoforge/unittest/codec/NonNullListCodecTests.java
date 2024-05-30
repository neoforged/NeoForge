/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.codec;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.codec.NonNullListCodecs;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class NonNullListCodecTests {
    @Test
    void testCanSerializeNbtWithCodec(MinecraftServer server) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(5, ItemStack.EMPTY);
        stacks.set(2, new ItemStack(Items.GOLD_INGOT, 10));

        Codec<NonNullList<ItemStack>> codec = NonNullListCodecs.withIndices(ItemStack.OPTIONAL_CODEC, ItemStack.EMPTY, "slot", ItemStack::isEmpty);

        var result = codec.encodeStart(NbtOps.INSTANCE, stacks);

        final var res1 = result.getOrThrow();

        Assertions.assertThat(res1)
                .isNotNull()
                .isInstanceOf(CompoundTag.class);

        if (res1 instanceof CompoundTag ct) {
            var list = ct.getList("items", Tag.TAG_COMPOUND);

            Assertions.assertThat(list)
                    .matches(l -> l.size() == 1, "correct stack count")
                    .isInstanceOf(ListTag.class);

            var slot2 = list.getFirst();
            Assertions.assertThat(slot2)
                    .isNotNull()
                    .isInstanceOf(CompoundTag.class);

            if (slot2 instanceof CompoundTag item2) {
                Assertions.assertThat(item2)
                        .isNotNull()
                        .matches(i2 -> i2.contains("slot"), "has a slot")
                        .matches(i2 -> i2.contains("id"), "has item id")
                        .matches(i2 -> i2.contains("count") && i2.getInt("count") == 10, "has correct count");
            }
        }
    }

    @Test
    void testCanDeserializeNbtWithCodec(MinecraftServer server) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(5, ItemStack.EMPTY);
        stacks.set(2, new ItemStack(Items.GOLD_INGOT, 10));

        Codec<NonNullList<ItemStack>> codec = NonNullListCodecs.withIndices(ItemStack.OPTIONAL_CODEC, ItemStack.EMPTY, "slot", ItemStack::isEmpty);

        var result = codec.encodeStart(NbtOps.INSTANCE, stacks);

        final var res1 = result.getOrThrow();

        final var decoded = codec.parse(NbtOps.INSTANCE, res1)
                .getOrThrow();

        Assertions.assertThat(decoded)
                .isNotNull()
                .isInstanceOf(NonNullList.class)
                .matches(nnl -> !nnl.isEmpty());
    }
}

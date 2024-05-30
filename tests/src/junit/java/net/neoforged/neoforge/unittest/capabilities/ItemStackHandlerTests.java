/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.capabilities;

import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class ItemStackHandlerTests {
    @Test
    void testCanSerializeNbtWithCodec(MinecraftServer server) {
        final var handler = new ItemStackHandler(5);
        handler.setStackInSlot(3, new ItemStack(Items.GOLD_INGOT, 7));

        var encodeResult = ItemStackHandler.nbtCompatibleCodec(server.registryAccess())
                .encodeStart(NbtOps.INSTANCE, handler);

        var originalResult = handler.serializeNBT(server.registryAccess());

        Assertions.assertThat(encodeResult.isSuccess()).isTrue();

        Assertions.assertThat(originalResult)
                .isEqualTo(encodeResult.getOrThrow());

        final var res = encodeResult.getOrThrow();
        Assertions.assertThat(res).isInstanceOf(CompoundTag.class);

        if (res instanceof CompoundTag ct) {
            Assertions.assertThat(ct)
                    .matches(c -> c.contains("Size"))
                    .matches(c -> c.contains("Items"));

            Assertions.assertThat(ct.getInt("Size"))
                    .isEqualTo(5);
        }
    }

    @Test
    void testCanSerializeJsonWithCodec(MinecraftServer server) {
        final var handler = new ItemStackHandler(5);
        handler.setStackInSlot(3, new ItemStack(Items.GOLD_INGOT, 7));

        var encodeResult = ItemStackHandler.nbtCompatibleCodec(server.registryAccess())
                .encodeStart(JsonOps.INSTANCE, handler);

        Assertions.assertThat(encodeResult.isSuccess()).isTrue();

        final var res = encodeResult.getOrThrow()
                .getAsJsonObject();

        Assertions.assertThat(res)
                .matches(el -> el.has("Size"))
                .matches(el -> el.has("Items"));

        Assertions.assertThat(res.get("Size").getAsInt())
                .isEqualTo(5);
    }

    @Test
    void testCanDeserializeNbtWithCodec(MinecraftServer server) {
        final var handler = new ItemStackHandler(5);
        handler.setStackInSlot(3, new ItemStack(Items.GOLD_INGOT, 7));

        var originalResult = handler.serializeNBT(server.registryAccess());

        var decodeResult = ItemStackHandler.nbtCompatibleCodec(server.registryAccess())
                .parse(NbtOps.INSTANCE, originalResult);

        Assertions.assertThat(decodeResult).isNotNull();

        Assertions.assertThat(decodeResult.isSuccess()).isTrue();

        final var res = decodeResult.getOrThrow();
        Assertions.assertThat(res).isInstanceOf(ItemStackHandler.class);

        if (res instanceof ItemStackHandler h) {
            Assertions.assertThat(h.getSlots())
                    .isEqualTo(5);

            Assertions.assertThat(h.getStackInSlot(3))
                    .matches(stack -> stack.is(Items.GOLD_INGOT))
                    .matches(stack -> stack.getCount() == 7);
        }
    }
}

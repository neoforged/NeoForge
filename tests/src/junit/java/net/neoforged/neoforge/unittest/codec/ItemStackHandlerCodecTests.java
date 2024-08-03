/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.codec;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class ItemStackHandlerCodecTests {
    private static ItemStackHandler makeItemHandler() {
        var itemHandler = new ItemStackHandler(5);
        itemHandler.setStackInSlot(2, new ItemStack(Items.GOLD_INGOT, 25));
        return itemHandler;
    }

    @Test
    public void canSerializeWithCodec(MinecraftServer server) {
        var itemHandler = makeItemHandler();

        var wrappedResult = itemHandler.CODEC.encodeStart(server.registryAccess().createSerializationContext(NbtOps.INSTANCE), itemHandler);

        var naturalResult = itemHandler.serializeNBT(server.registryAccess());

        Assertions.assertTrue(wrappedResult.isSuccess());
        Assertions.assertEquals(naturalResult, wrappedResult.getOrThrow());
    }

    @Test
    public void canDeserializeWithCodec(MinecraftServer server) {
        var itemHandler = makeItemHandler();

        var naturalResult = itemHandler.serializeNBT(server.registryAccess());

        var wrappedResult = itemHandler.CODEC
                .parse(server.registryAccess().createSerializationContext(NbtOps.INSTANCE), naturalResult);

        Assertions.assertTrue(wrappedResult.isSuccess());

        final var handler = wrappedResult.getOrThrow();
        Assertions.assertNotEquals(ItemStack.EMPTY, handler.getStackInSlot(2));

        ItemStack slot2 = handler.getStackInSlot(2);
        Assertions.assertEquals(Items.GOLD_INGOT, slot2.getItem());
        Assertions.assertEquals(25, slot2.getCount());
    }

    @Test
    public void canRoundTripWithStreamCodec(MinecraftServer server) {
        var itemHandler = makeItemHandler();

        final RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess(), ConnectionType.NEOFORGE);

        itemHandler.STREAM_CODEC.encode(buf, itemHandler);

        final var result = itemHandler.STREAM_CODEC.decode(buf);

        Assertions.assertNotEquals(ItemStack.EMPTY, result.getStackInSlot(2));

        ItemStack slot2 = result.getStackInSlot(2);
        Assertions.assertEquals(Items.GOLD_INGOT, slot2.getItem());
        Assertions.assertEquals(25, slot2.getCount());
    }
}

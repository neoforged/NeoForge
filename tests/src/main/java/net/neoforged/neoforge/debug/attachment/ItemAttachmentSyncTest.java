/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.attachment;

import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * This test ensures that {@link ItemStack} attachments are written to and read from packets as we expect.
 * If loading a world doesn't crash, then the test passed.
 */
@Mod(ItemAttachmentSyncTest.MOD_ID)
public class ItemAttachmentSyncTest {
    public static final String MOD_ID = "item_attachment_sync_test";

    private static final boolean ENABLED = true;

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);
    private static final Supplier<AttachmentType<ItemStackHandler>> HANDLER = ATTACHMENT_TYPES.register(
            "handler", () -> AttachmentType.serializable(() -> new ItemStackHandler(1)).build());

    public ItemAttachmentSyncTest(IEventBus modBus) {
        if (!ENABLED)
            return;

        ATTACHMENT_TYPES.register(modBus);

        NeoForge.EVENT_BUS.addListener(ItemAttachmentSyncTest::serverStarted);
    }

    private static void serverStarted(ServerStartedEvent event) {
        ItemStack stack = Items.APPLE.getDefaultInstance();
        IItemHandler handler = stack.getData(HANDLER);
        if (handler == null)
            throw new AssertionError("ItemStack handler is null");

        handler.insertItem(0, Items.DIAMOND.getDefaultInstance(), false);
        if (!ItemStack.matches(Items.DIAMOND.getDefaultInstance(), handler.getStackInSlot(0)))
            throw new AssertionError("ItemStack handler did not accept the item");

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        // Try without stack nbt
        buf.writeItem(stack);
        ItemStack readStack = buf.readItem();

        if (!ItemStack.matches(stack, readStack))
            throw new AssertionError("ItemStack did not sync correctly without NBT");
        if (!ItemStack.matches(Items.DIAMOND.getDefaultInstance(), readStack.getData(HANDLER).getStackInSlot(0)))
            throw new AssertionError("ItemStack handler did not sync the contained item");

        // Try with empty nbt
        stack.getOrCreateTag();
        buf.writeItem(stack);
        readStack = buf.readItem();

        if (!ItemStack.matches(stack, readStack))
            throw new AssertionError("ItemStack did not sync correctly with NBT");
        if (!ItemStack.matches(Items.DIAMOND.getDefaultInstance(), readStack.getData(HANDLER).getStackInSlot(0)))
            throw new AssertionError("ItemStack handler did not sync the contained item");

        // Try with non-empty nbt
        stack.getOrCreateTag().putString("test", "test");
        buf.writeItem(stack);
        readStack = buf.readItem();

        if (!ItemStack.matches(stack, readStack))
            throw new AssertionError("ItemStack did not sync correctly with NBT");
        if (!ItemStack.matches(Items.DIAMOND.getDefaultInstance(), readStack.getData(HANDLER).getStackInSlot(0)))
            throw new AssertionError("ItemStack handler did not sync the contained item");

        LogUtils.getLogger().info("ItemAttachmentSyncTest passed");
    }
}

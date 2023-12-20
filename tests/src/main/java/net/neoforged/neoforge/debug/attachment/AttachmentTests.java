/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.attachment;

import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "attachment")
public class AttachmentTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that ItemStack attachments are written to and read from packets as we expect")
    static void itemAttachmentSyncTest(final DynamicTest test, final RegistrationHelper reg) {
        final var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("handler", () -> AttachmentType.serializable(() -> new ItemStackHandler(1)).build());

        test.onGameTest(helper -> {
            ItemStack stack = Items.APPLE.getDefaultInstance();
            IItemHandler handler = stack.getData(attachmentType);
            helper.assertTrue(handler != null, "ItemStack handler is null");
            assert handler != null;

            handler.insertItem(0, Items.DIAMOND.getDefaultInstance(), false);
            helper.assertTrue(ItemStack.matches(Items.DIAMOND.getDefaultInstance(), handler.getStackInSlot(0)), "ItemStack handler did not accept the item");

            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

            // Try without stack nbt
            buf.writeItem(stack);
            ItemStack readStack = buf.readItem();

            helper.assertTrue(ItemStack.matches(stack, readStack), "ItemStack did not sync correctly without NBT");
            helper.assertTrue(ItemStack.matches(Items.DIAMOND.getDefaultInstance(), readStack.getData(attachmentType).getStackInSlot(0)), "ItemStack handler did not sync the contained item");

            // Try with empty nbt
            stack.getOrCreateTag();
            buf.writeItem(stack);
            readStack = buf.readItem();

            helper.assertTrue(ItemStack.matches(stack, readStack), "ItemStack did not sync correctly with NBT");
            helper.assertTrue(ItemStack.matches(Items.DIAMOND.getDefaultInstance(), readStack.getData(attachmentType).getStackInSlot(0)), "ItemStack handler did not sync the contained item");

            // Try with non-empty nbt
            stack.getOrCreateTag().putString("test", "test");
            buf.writeItem(stack);
            readStack = buf.readItem();

            helper.assertTrue(ItemStack.matches(stack, readStack), "ItemStack did not sync correctly with NBT");
            helper.assertTrue(ItemStack.matches(Items.DIAMOND.getDefaultInstance(), readStack.getData(attachmentType).getStackInSlot(0)), "ItemStack handler did not sync the contained item");

            helper.succeed();
        });
    }
}

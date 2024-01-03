/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.attachment;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.Command;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
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

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that chunk attachments can capture a reference to the containing LevelChunk.")
    static void chunkAttachmentReferenceTest(DynamicTest test, RegistrationHelper reg) {
        class ChunkMutableInt implements INBTSerializable<IntTag> {
            private final LevelChunk chunk;
            private int value;

            public ChunkMutableInt(LevelChunk chunk, int value) {
                this.chunk = chunk;
                this.value = value;
            }

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
                chunk.setUnsaved(true);
            }

            @Override
            public IntTag serializeNBT() {
                return IntTag.valueOf(value);
            }

            @Override
            public void deserializeNBT(IntTag nbt) {
                this.value = nbt.getAsInt();
            }
        }

        var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("chunk_mutable_int", () -> AttachmentType.serializable(chunk -> new ChunkMutableInt((LevelChunk) chunk, 0)).build());

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            event.getDispatcher()
                    .register(literal(test.id())
                            .then(literal("print_and_increment")
                                    .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                                    .executes(ctx -> {
                                        var chunk = ctx.getSource().getLevel().getChunkAt(BlockPos.containing(ctx.getSource().getPosition()));
                                        var attachment = chunk.getData(attachmentType);
                                        attachment.setValue(attachment.getValue() + 1);
                                        ctx.getSource().sendSuccess(() -> Component.literal("New attachment value: " + attachment.getValue()), false);
                                        return Command.SINGLE_SUCCESS;
                                    })));
        });

        test.onGameTest(helper -> {
            var player = helper.makeOpMockPlayer(Commands.LEVEL_OWNERS);
            var pos = helper.absolutePos(BlockPos.ZERO);
            player.setPos(pos.getCenter());

            helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), test.id() + " print_and_increment");
            helper.assertTrue(((LevelChunk) helper.getLevel().getChunk(pos)).getData(attachmentType).getValue() == 1,
                    "Chunk attachment value should have been 1");

            helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), test.id() + " print_and_increment");
            helper.assertTrue(((LevelChunk) helper.getLevel().getChunk(pos)).getData(attachmentType).getValue() == 2,
                    "Chunk attachment value should have been 2");

            helper.succeed();
        });
    }
}

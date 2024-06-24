/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.attachment;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.Command;
import com.mojang.serialization.Codec;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "attachment")
public class AttachmentTests {
    private static class ChunkMutableInt {
        LevelChunk chunk;
        private int value;

        public static final Codec<ChunkMutableInt> CODEC = Codec.INT.xmap(ChunkMutableInt::new, ChunkMutableInt::getValue);

        public ChunkMutableInt(LevelChunk chunk, int value) {
            this.chunk = chunk;
            this.value = value;
        }

        public ChunkMutableInt(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
            chunk.setUnsaved(true);
        }
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that chunk attachments can capture a reference to the containing LevelChunk.")
    static void chunkAttachmentReferenceTest(DynamicTest test, RegistrationHelper reg) {
        var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("chunk_mutable_int", () -> AttachmentType.builder(chunk -> new ChunkMutableInt((LevelChunk) chunk, 0))
                        .serialize(ChunkMutableInt.CODEC)
                        .build());

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

            helper.getLevel().getChunk(pos).removeData(attachmentType); // remove data to ensure that the test can run multiple times

            helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), test.id() + " print_and_increment");
            helper.assertTrue(((LevelChunk) helper.getLevel().getChunk(pos)).getData(attachmentType).getValue() == 1,
                    "Chunk attachment value should have been 1");

            helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), test.id() + " print_and_increment");
            helper.assertTrue(((LevelChunk) helper.getLevel().getChunk(pos)).getData(attachmentType).getValue() == 2,
                    "Chunk attachment value should have been 2");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that chunk attachments can capture a reference to the containing LevelChunk.")
    static void postDeserializeFunctionsAreCalled(DynamicTest test, RegistrationHelper reg) {
        var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("chunk_mutable_int", () -> AttachmentType.builder(chunk -> new ChunkMutableInt((LevelChunk) chunk, 0))
                        .serialize(ChunkMutableInt.CODEC)
                        .postDeserialize((IAttachmentHolder holder, ChunkMutableInt chunkMutable) -> {
                            chunkMutable.chunk = (LevelChunk) holder;
                            chunkMutable.value += 100;
                        })
                        .build());

        test.onGameTest(helper -> {
            final var chunk = helper.getLevel().getChunk(helper.absolutePos(BlockPos.ZERO));

            chunk.removeData(attachmentType); // remove data to ensure that the test can run multiple times
            chunk.setData(attachmentType, new ChunkMutableInt(5));

            var serialized = chunk.dataAttachments()
                    .serializeAttachments(helper.getLevel().registryAccess());

            var chunkData = serialized.get(attachmentType.getId().toString());
            helper.assertTrue(chunkData instanceof IntTag, "Chunk data not an integer");

            assert chunkData instanceof IntTag;
            var chunkDataInt = (IntTag) chunkData;
            helper.assertValueEqual(5, chunkDataInt.getAsInt(), "Chunk data did not serialize correctly.");

            chunk.dataAttachments()
                    .deserializeAttachments(helper.getLevel().registryAccess(), serialized);

            helper.assertValueEqual(chunk, chunk.getData(attachmentType).chunk, "Chunk deserialize did not re-attach chunk reference");
            helper.assertValueEqual(105, chunk.getData(attachmentType).getValue(), "Chunk deserialize did not adjust number");
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that player attachments are copied on respawn when appropriate.")
    static void playerAttachmentCopyOnRespawn(DynamicTest test, RegistrationHelper reg) {
        var lostOnDeathBoolean = reg.attachments()
                .register("lost_on_death_boolean", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());
        var keptOnDeathBoolean = reg.attachments()
                .register("kept_on_death_boolean", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().build());

        test.onGameTest(helper -> {
            var player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL);
            player.setData(lostOnDeathBoolean, true);
            player.setData(keptOnDeathBoolean, true);

            var returningPlayer = player.getServer().getPlayerList().respawn(player, true, Entity.RemovalReason.CHANGED_DIMENSION);

            helper.assertTrue(returningPlayer.getData(lostOnDeathBoolean), "Lost-on-death attachment should have remained after end portal respawning.");
            helper.assertTrue(returningPlayer.getData(keptOnDeathBoolean), "Kept-on-death attachment should have remained after end portal respawning.");

            var respawnedPlayer = player.getServer().getPlayerList().respawn(returningPlayer, false, Entity.RemovalReason.KILLED);

            helper.assertFalse(respawnedPlayer.getData(lostOnDeathBoolean), "Lost-on-death attachment should not have remained after respawning.");
            helper.assertTrue(respawnedPlayer.getData(keptOnDeathBoolean), "Kept-on-death attachment should have remained after respawning.");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that attachments with dynamic data are de/serialized well")
    static void dynamicDataContentSerialization(DynamicTest test, RegistrationHelper reg) {
        var stackType = reg.attachments()
                .register("stack", () -> AttachmentType.builder(() -> new ItemStack(Items.IRON_AXE)).serialize(ItemStack.CODEC).build());
        test.onGameTest(helper -> {
            var player = helper.makeMockPlayer();
            var stack = new ItemStack(Items.IRON_SWORD);
            var enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            enchantments.set(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SHARPNESS), 3);
            stack.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
            player.setData(stackType, stack);
            helper.catchException(() -> {
                player.dataAttachments().serializeAttachments(helper.getLevel().registryAccess()); // This will throw if it fails
            });
            helper.succeed();
        });
    }
}

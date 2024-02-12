/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.attachment;

import static net.minecraft.commands.Commands.literal;

import com.google.common.base.Suppliers;
import com.mojang.brigadier.Command;
import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
import net.neoforged.testframework.condition.TestEnabledIngredient;
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

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that ItemStack attachments can be specified in recipe outputs")
    static void itemAttachmentRecipeOutput(final DynamicTest test, final RegistrationHelper reg) {
        var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

        var ingredient = new TestEnabledIngredient(
                Ingredient.of(Items.GOLDEN_SHOVEL),
                test.framework(),
                test.id());
        var outputStack = Suppliers.memoize(() -> {
            var stack = new ItemStack(Items.GOLDEN_SHOVEL);
            stack.setData(attachmentType, 1);
            return stack;
        });

        var shapelessId = new ResourceLocation(reg.modId(), "test_shapeless");
        var shapedId = new ResourceLocation(reg.modId(), "test_shaped");
        var blastingId = new ResourceLocation(reg.modId(), "test_blasting");

        reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput()) {
            @Override
            protected void buildRecipes(RecipeOutput recipeOutput) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, outputStack.get())
                        .requires(ingredient)
                        .unlockedBy("has_shovel", has(Items.GOLDEN_SHOVEL))
                        .save(recipeOutput, shapelessId);

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, outputStack.get())
                        .pattern("xx")
                        .pattern("xx")
                        .define('x', ingredient)
                        .unlockedBy("has_shovel", has(Items.GOLDEN_SHOVEL))
                        .save(recipeOutput, shapedId);

                SimpleCookingRecipeBuilder.blasting(ingredient, RecipeCategory.MISC, outputStack.get(), 1.0f, 100)
                        .unlockedBy("has_shovel", has(Items.GOLDEN_SHOVEL))
                        .save(recipeOutput, blastingId);
            }
        });

        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
                .thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

                .thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
                .thenExecute(crafter -> crafter.setItem(0, Items.GOLDEN_SHOVEL.getDefaultInstance()))
                .thenIdle(3)

                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> {
                    var chest = helper.requireBlockEntity(1, 2, 1, ChestBlockEntity.class);
                    var stack = chest.getItem(0);

                    helper.assertTrue(stack.getItem() == Items.GOLDEN_SHOVEL, "Expected apple");
                    helper.assertTrue(stack.hasData(attachmentType), "Expected attachment");
                    helper.assertTrue(stack.getData(attachmentType) == 1, "Expected attachment value of 1");
                })

                .thenExecute(() -> {
                    // Just look at the output via the RecipeManager
                    var recipeManager = helper.getLevel().getRecipeManager();

                    for (var id : List.of(shapelessId, shapedId, blastingId)) {
                        var recipe = recipeManager.byKey(id).map(RecipeHolder::value).orElse(null);
                        if (recipe == null) {
                            helper.fail("No recipe " + id);
                        }

                        helper.assertTrue(ItemStack.matches(outputStack.get(), recipe.getResultItem(helper.getLevel().registryAccess())), "Recipe output should match stack.");
                    }
                })

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that ItemStack.CODEC can handle attachments")
    static void itemAttachmentCodec(final DynamicTest test, final RegistrationHelper reg) {
        var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

        test.onGameTest(helper -> {
            ItemStack stack = Items.APPLE.getDefaultInstance();
            stack.setData(attachmentType, 1);

            var encodedNbt = Util.getOrThrow(ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack), IllegalStateException::new);
            var decodedStack = Util.getOrThrow(ItemStack.CODEC.parse(NbtOps.INSTANCE, encodedNbt), IllegalStateException::new);

            helper.assertTrue(decodedStack.hasData(attachmentType), "Decoded stack should have the data");
            helper.assertTrue(ItemStack.matches(stack, decodedStack), "Stacks should match");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that attachments can opt-out of serializing default values")
    static void itemAttachmentSkipSerialization(final DynamicTest test, final RegistrationHelper reg) {
        var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT, i -> i != 0).build());

        test.onGameTest(helper -> {
            ItemStack stack = Items.APPLE.getDefaultInstance();
            stack.setData(attachmentType, 1);
            helper.assertTrue(stack.serializeAttachments() != null, "Stack should have serialized attachments");
            stack.setData(attachmentType, 0);
            helper.assertTrue(stack.serializeAttachments() == null, "None of the stack's attachments should be serialized");
            helper.assertTrue(stack.hasData(attachmentType), "Stack should have attached data");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that removing attachments works")
    static void itemAttachmentRemoval(final DynamicTest test, final RegistrationHelper reg) {
        var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

        test.onGameTest(helper -> {
            ItemStack stack = Items.APPLE.getDefaultInstance();
            stack.setData(attachmentType, 1);
            helper.assertTrue(stack.hasData(attachmentType), "Stack should have attached data");
            stack.removeData(attachmentType);
            helper.assertFalse(stack.hasData(attachmentType), "Stack should not have attached data");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that the presence of non-serializable attachments can be checked")
    static void itemAttachmentPresence(final DynamicTest test, final RegistrationHelper reg) {
        var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("test_int", () -> AttachmentType.builder(() -> 0).build());

        test.onGameTest(helper -> {
            ItemStack stack = Items.APPLE.getDefaultInstance();
            stack.setData(attachmentType, 1);
            helper.assertTrue(stack.hasData(attachmentType), "Stack should have attached data");
            //Also check we can detect the presence if we don't know what types are attached
            helper.assertTrue(stack.hasAttachments(), "Stack should have attached data");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that custom cloning behaviour works")
    static void itemAttachmentCloning(final DynamicTest test, final RegistrationHelper reg) {
        var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("test_int", () -> AttachmentType.builder(() -> 0).cloner((holder, i) -> 2 * i).build());

        test.onGameTest(helper -> {
            ItemStack stack = Items.APPLE.getDefaultInstance();
            stack.setData(attachmentType, 1);
            helper.assertTrue(stack.getData(attachmentType) == 1, "Stack should have attached data");
            stack = stack.copy();
            helper.assertTrue(stack.getData(attachmentType) == 2, "Stack cloner should have cloned and modified the data");
            helper.succeed();
        });
    }
}

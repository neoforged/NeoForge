/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.attachment;

import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.attachment.AttachmentType;
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
    @TestHolder(description = "Ensures that ItemStack attachments can be specified in recipe outputs")
    static void itemAttachmentRecipeOutput(final DynamicTest test, final RegistrationHelper reg) {
        var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

        var recipeId = new ResourceLocation(reg.modId(), "test_recipe");

        reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void buildRecipes(RecipeOutput recipeOutput) {
                // ShapelessRecipeBuilder doesn't accept attachments (yet), so we build the ShapelessRecipe directly!
                recipeOutput.accept(
                        recipeId,
                        new ShapelessRecipe(
                                "",
                                CraftingBookCategory.MISC,
                                Util.make(new ItemStack(Items.APPLE), s -> s.setData(attachmentType, 1)),
                                NonNullList.copyOf(List.of(
                                        new TestEnabledIngredient(
                                                Ingredient.of(Items.APPLE),
                                                test.framework(),
                                                test.id())))),
                        null);
            }
        });

        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
                .thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

                .thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
                .thenExecute(crafter -> crafter.setItem(0, Items.APPLE.getDefaultInstance()))
                .thenIdle(3)

                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> {
                    var chest = helper.requireBlockEntity(1, 2, 1, ChestBlockEntity.class);
                    var stack = chest.getItem(0);

                    helper.assertTrue(stack.getItem() == Items.APPLE, "Expected apple");
                    helper.assertTrue(stack.hasData(attachmentType), "Expected attachment");
                    helper.assertTrue(stack.getData(attachmentType) == 1, "Expected attachment value of 1");
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
}

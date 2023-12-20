/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.crafting;

import java.util.Optional;
import net.minecraft.core.FrontAndTop;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.crafting.NBTIngredient;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.condition.TestEnabledIngredient;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "crafting.ingredient")
public class IngredientTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if partial NBT ingredients match the correct stacks")
    static void partialNBTIngredient(final DynamicTest test, final RegistrationHelper reg) {
        reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void buildRecipes(RecipeOutput output) {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.ALLIUM)
                        .pattern("IDE")
                        .define('I', new TestEnabledIngredient(
                                NBTIngredient.of(false, putInt(new CompoundTag(), ItemStack.TAG_DAMAGE, 2), Items.IRON_AXE),
                                test.framework(), test.id()))
                        .define('D', Items.DIAMOND)
                        .define('E', Items.EMERALD)
                        .unlockedBy("has_axe", has(Items.IRON_AXE))
                        .save(output, new ResourceLocation(reg.modId(), "partial_nbt"));
            }
        });

        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
                .thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

                .thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
                .thenExecute(crafter -> crafter.setItem(0, Items.IRON_AXE.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(1, Items.DIAMOND.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(2, Items.EMERALD.getDefaultInstance()))
                .thenIdle(3)

                // Axe is not damaged, the recipe shouldn't work
                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

                .thenIdle(5) // Crafter cooldown

                // Axe is now damaged, we expect the recipe to work, even if we also add other random values to the compound
                .thenExecute(crafter -> crafter.getItem(0).hurt(2, helper.getLevel().random, null))
                .thenExecute(crafter -> crafter.getItem(0).getTag().putFloat("abcd", helper.getLevel().random.nextFloat()))

                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.ALLIUM))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if strict NBT ingredients match the correct stacks")
    static void strictNBTIngredient(final DynamicTest test, final RegistrationHelper reg) {
        reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void buildRecipes(RecipeOutput output) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.ACACIA_BOAT)
                        .requires(new TestEnabledIngredient(
                                NBTIngredient.of(true, putInt(new CompoundTag(), ItemStack.TAG_DAMAGE, 4), Items.DIAMOND_PICKAXE),
                                test.framework(), test.id()))
                        .requires(Items.ACACIA_PLANKS)
                        .unlockedBy("has_pick", has(Items.DIAMOND_PICKAXE))
                        .save(output, new ResourceLocation(reg.modId(), "strict_nbt"));
            }
        });

        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
                .thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

                .thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
                .thenExecute(crafter -> crafter.setItem(1, new ItemStack(Items.DIAMOND_PICKAXE, 1, Optional.of(putInt(new CompoundTag(), ItemStack.TAG_DAMAGE, 1)))))
                .thenExecute(crafter -> crafter.setItem(0, Items.ACACIA_PLANKS.getDefaultInstance()))
                .thenIdle(3)

                // Pickaxe is damaged, but not enough, so, the recipe shouldn't work
                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

                .thenIdle(5) // Crafter cooldown

                // Pickaxe now has a damage value of 4, but superfluous nbt tags, so the recipe should still not work
                .thenExecute(crafter -> crafter.getItem(1).hurt(3, helper.getLevel().random, null))
                .thenExecute(crafter -> crafter.getItem(1).getOrCreateTag().putFloat("abcd", 12f))

                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

                .thenIdle(5) // Crafter cooldown

                // The superfluous element is gone, so the recipe should now work
                .thenExecute(crafter -> crafter.getItem(1).getOrCreateTag().remove("abcd"))
                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.ACACIA_BOAT))

                .thenSucceed());
    }

    private static CompoundTag putInt(CompoundTag tag, String key, int value) {
        tag.putInt(key, value);
        return tag;
    }
}

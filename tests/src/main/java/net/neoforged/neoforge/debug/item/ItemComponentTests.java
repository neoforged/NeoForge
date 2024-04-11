/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = ItemTests.GROUP + ".component")
public class ItemComponentTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that holders and deferred holders can be used as data components")
    public static void testHolderComponent(DynamicTest test, RegistrationHelper reg) {
        var blockHolderComponent = reg.registrar(Registries.DATA_COMPONENT_TYPE)
                .register("test_holder", () -> DataComponentType.<Holder<Block>>builder()
                        .persistent(BuiltInRegistries.BLOCK.holderByNameCodec())
                        .networkSynchronized(ByteBufCodecs.holderRegistry(Registries.BLOCK))
                        .build());

        test.onGameTest(helper -> {
            ItemStack stack = Items.DIAMOND.getDefaultInstance();

            ItemStack stack1 = stack.copy();
            stack1.set(blockHolderComponent.get(), Blocks.DIAMOND_BLOCK.builtInRegistryHolder());

            ItemStack stack2 = stack.copy();
            var diamondDh = DeferredBlock.createBlock(BuiltInRegistries.BLOCK.getKey(Blocks.DIAMOND_BLOCK));
            stack2.set(blockHolderComponent.get(), diamondDh);

            if (!ItemStack.matches(stack1, stack2)) {
                helper.fail("Expected the same item stacks");
            }

            if (!ItemStack.matches(stack2, stack1)) {
                helper.fail("Expected the same item stacks (reversed order)");
            }

            if (ItemStack.hashItemAndComponents(stack1) != ItemStack.hashItemAndComponents(stack2)) {
                helper.fail("Expected the same hash");
            }

            helper.succeed();
        });
    }
}

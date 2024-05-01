/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.debug.EventTests;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.ModifyDefaultComponentsEvent;
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
            stack1.set(blockHolderComponent, Blocks.DIAMOND_BLOCK.builtInRegistryHolder());

            ItemStack stack2 = stack.copy();
            var diamondDh = DeferredBlock.createBlock(BuiltInRegistries.BLOCK.getKey(Blocks.DIAMOND_BLOCK));
            stack2.set(blockHolderComponent, diamondDh);

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

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the ModifyDefaultComponentsEvent works", groups = EventTests.GROUP)
    static void testModifyDefaultComponentsEvent(DynamicTest test, RegistrationHelper reg) {
        final var testItem = reg.items().registerSimpleItem("test_item", new Item.Properties()
                .component(DataComponents.BASE_COLOR, DyeColor.BLUE))
                .withLang("Test components item");
        test.framework().modEventBus().addListener((final ModifyDefaultComponentsEvent event) -> {
            event.modify(testItem, builder -> builder
                    .remove(DataComponents.BASE_COLOR)
                    .set(DataComponents.MAX_STACK_SIZE, 5));
        });

        test.onGameTest(helper -> {
            helper.assertFalse(testItem.asItem().components().has(DataComponents.BASE_COLOR), "Default component was not removed");
            helper.assertValueEqual(testItem.asItem().getDefaultMaxStackSize(), 5, "max stack size");
            helper.succeed();
        });
    }
}

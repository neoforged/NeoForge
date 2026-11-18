/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.item;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = ItemTests.GROUP + ".creative_mode_tab")
public class CreativeModeTabTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the BuildCreativeModeTabContentsEvent can insert entries before and after existing ones in a custom creative mode tab, with their tab visibility")
    static void buildCreativeModeTabContents(final DynamicTest test, final RegistrationHelper reg) {
        final ResourceKey<CreativeModeTab> key = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(reg.modId(), "logs"));
        reg.registrar(Registries.CREATIVE_MODE_TAB).register(key.identifier().getPath(), () -> CreativeModeTab.builder()
                .title(Component.literal("Logs"))
                .icon(() -> new ItemStack(Blocks.OAK_LOG))
                .displayItems((parameters, output) -> {
                    output.accept(new ItemStack(Blocks.OAK_LOG));
                    output.accept(new ItemStack(Blocks.BIRCH_LOG));
                })
                .build());

        test.eventListeners().mod().addListener((final BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey().equals(key)) {
                event.insertAfter(new ItemStack(Blocks.OAK_LOG), new ItemStack(Blocks.STRIPPED_OAK_LOG), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                event.insertBefore(new ItemStack(Blocks.BIRCH_LOG), new ItemStack(Blocks.STRIPPED_BIRCH_LOG), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                event.accept(new ItemStack(Blocks.SPRUCE_LOG), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
            }
        });

        test.onGameTest(helper -> {
            CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(key);
            tab.buildContents(new CreativeModeTab.ItemDisplayParameters(helper.getLevel().enabledFeatures(), true, helper.getLevel().registryAccess()));

            List<Item> displayed = tab.getDisplayItems().stream().map(ItemStack::getItem).toList();
            helper.assertValueEqual(displayed, List.of(Blocks.OAK_LOG.asItem(), Blocks.STRIPPED_OAK_LOG.asItem(), Blocks.STRIPPED_BIRCH_LOG.asItem(), Blocks.BIRCH_LOG.asItem()), "Unexpected tab contents; contents are " + displayed);
            List<Item> searched = tab.getSearchTabDisplayItems().stream().map(ItemStack::getItem).toList();
            helper.assertValueEqual(searched, List.of(Blocks.OAK_LOG.asItem(), Blocks.STRIPPED_OAK_LOG.asItem(), Blocks.BIRCH_LOG.asItem(), Blocks.SPRUCE_LOG.asItem()), "Unexpected search tab contents; contents are " + searched);
            helper.succeed();
        });
    }
}

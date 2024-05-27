/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = "item.tooltip.event")
public class ItemTooltipTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if tooltips are added in correct orderings")
    static void itemTooltipTest(DynamicTest test) {
        var forge = test.eventListeners().forge();

        forge.addListener((ItemTooltipEvent.AfterName event) -> event.getToolTip().add(Component.literal("After Name")));
        forge.addListener((ItemTooltipEvent.BeforeComponents event) -> event.getToolTip().add(Component.literal("Before Components")));
        forge.addListener((ItemTooltipEvent.AfterComponents event) -> event.getToolTip().add(Component.literal("After Components")));
        forge.addListener((ItemTooltipEvent.BeforeDebug event) -> event.getToolTip().add(Component.literal("Before Debug")));
        forge.addListener((ItemTooltipEvent.AfterDebug event) -> event.getToolTip().add(Component.literal("After Debug")));
        forge.addListener((ItemTooltipEvent.AfterAll event) -> event.getToolTip().add(Component.literal("After All")));
    }
}

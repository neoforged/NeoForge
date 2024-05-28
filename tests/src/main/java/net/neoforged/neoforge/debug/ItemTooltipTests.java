/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;

@ForEachTest(groups = "item.tooltip.event")
public class ItemTooltipTests {
    @OnInit
    static void itemTooltipTest(TestFramework framework) {
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent.AfterName event) -> event.getToolTip().add(Component.literal("After Name")));
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent.BeforeComponents event) -> event.getToolTip().add(Component.literal("Before Components")));
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent.AfterComponents event) -> event.getToolTip().add(Component.literal("After Components")));
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent.BeforeDebug event) -> event.getToolTip().add(Component.literal("Before Debug")));
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent.AfterDebug event) -> event.getToolTip().add(Component.literal("After Debug")));
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent.AfterAll event) -> event.getToolTip().add(Component.literal("After All")));
    }
}

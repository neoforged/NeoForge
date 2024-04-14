/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

import java.util.function.Consumer;

@ForEachTest(groups = CustomHelperTests.GROUP)
public class CustomHelperTests {
    public static final String GROUP = "customHelper";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that the framework creates and provides a custom helper")
    static void testCustomHelper(final Helper gameTestHelper) {
        gameTestHelper.customSucceed();
    }

    public static class Helper extends ExtendedGameTestHelper {

        public Helper(GameTestInfo info) {
            super(info);
        }

        void customSucceed() {
            this.succeed();
        }
    }
}

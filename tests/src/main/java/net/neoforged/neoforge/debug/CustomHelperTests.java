/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestInfo;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

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

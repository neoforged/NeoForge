/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.gametest.framework.GameTest;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = "mixin")
public class MixinTests {
    public static class Target {
        public static boolean wasMixinApplied() {
            // A mixin injects here to return true.
            return false;
        }
    }

    @GameTest
    @EmptyTemplate
    @TestHolder
    static void testMixin(DynamicTest test) {
        test.onGameTest(helper -> {
            if (Target.wasMixinApplied()) {
                helper.succeed();
            } else {
                helper.fail("Mixin was not applied!");
            }
        });
    }
}

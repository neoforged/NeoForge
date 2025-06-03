/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.groups;

import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = GroupTest.groupA)
public class SubGroupTest {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests group calls.")
    public static void groupTest(ExtendedGameTestHelper helper) {
        var pos = GroupTest.setupLevelEnvironment(helper);

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests group calls.")
    public static void groupTest2(ExtendedGameTestHelper helper) {
        var pos = GroupTest.setupLevelEnvironment(helper);
        helper.succeed();
    }
}

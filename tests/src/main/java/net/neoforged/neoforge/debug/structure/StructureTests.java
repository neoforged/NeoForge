/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.structure;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

@ForEachTest(groups = StructureTests.GROUP)
public class StructureTests {
    public static final String GROUP = "structure";

    @GameTest
    @TestHolder(description = "Tests that the structure bounds for use in Game Tests is correct")
    static void everyBlockInStructure(final DynamicTest test) {
        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(3, 3, 3)
                .fill(0, 0, 0, 2, 2, 2, Blocks.DIAMOND_BLOCK));

        test.onGameTest(helper -> helper.startSequence()
                .thenWaitUntil(0, () -> helper.forEveryBlockInStructure(pos -> helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, pos)))
                .thenExecute(test::pass)
                .thenSucceed());
    }
}

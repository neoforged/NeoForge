/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import net.neoforged.neoforge.debug.entity.EntityTests;
import net.neoforged.testframework.annotation.ForEachTest;

@ForEachTest(groups = PlayerTests.GROUP)
public class PlayerTests {
    public static final String GROUP = EntityTests.GROUP + ".player";
}

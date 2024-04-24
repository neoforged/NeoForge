/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.gametest;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Rotation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record GameTestData(
        @Nullable String batchName, String structureName, boolean required, int maxAttempts,
        int requiredSuccesses, Consumer<GameTestHelper> function, int maxTicks,
        long setupTicks, Rotation rotation, boolean skyAccess) {}

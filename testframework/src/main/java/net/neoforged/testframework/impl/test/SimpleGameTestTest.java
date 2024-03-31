/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.test;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTestData;

public class SimpleGameTestTest extends AbstractTest.Dynamic {
    private final Consumer<ExtendedGameTestHelper> function;

    public SimpleGameTestTest(GameTestData data, Consumer<ExtendedGameTestHelper> function) {
        this.gameTestData = data;
        this.function = function;

        this.visuals.description().add(Component.literal("GameTest-only"));
    }

    @Override
    public void init(TestFramework framework) {
        super.init(framework);
        onGameTest(helper -> {
            try {
                function.accept(helper);
            } catch (Throwable e) {
                throw new RuntimeException("Encountered exception running method-based test", e);
            }
        });
    }
}

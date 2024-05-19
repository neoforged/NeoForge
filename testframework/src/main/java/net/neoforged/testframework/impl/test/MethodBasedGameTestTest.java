/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.impl.ReflectionUtils;

public class MethodBasedGameTestTest extends AbstractTest.Dynamic {
    protected MethodHandle handle;
    private final Method method;
    private final Class<? extends GameTestHelper> helperType;

    public MethodBasedGameTestTest(Method method, Class<? extends GameTestHelper> helperType) {
        this.method = method;
        this.helperType = helperType;

        configureFrom(AnnotationHolder.method(method));
        this.visuals.description().add(Component.literal("GameTest-only"));

        this.handle = ReflectionUtils.handle(method);
    }

    @Override
    public void init(TestFramework framework) {
        super.init(framework);

        configureGameTest(method.getAnnotation(GameTest.class), method.getAnnotation(EmptyTemplate.class));

        onGameTest(helperType, helper -> {
            try {
                handle.invoke(helper);
            } catch (Throwable exception) {
                throw new RuntimeException("Encountered exception running method-based gametest test: " + method, exception);
            }
        });
    }
}

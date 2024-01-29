/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.impl.ReflectionUtils;

public class MethodBasedGameTestTest extends AbstractTest.Dynamic {
    protected MethodHandle handle;
    private final Method method;

    public MethodBasedGameTestTest(Method method) {
        this.method = method;

        configureFrom(AnnotationHolder.method(method));
        this.visuals.description().add(Component.literal("GameTest-only"));

        this.handle = ReflectionUtils.handle(method);
    }

    @Override
    public void init(TestFramework framework) {
        super.init(framework);

        configureGameTest(method.getAnnotation(GameTest.class), method.getAnnotation(EmptyTemplate.class));

        onGameTest(helper -> {
            try {
                handle.invoke(helper);
            } catch (Throwable exception) {
                throw new RuntimeException("Encountered exception running method-based gametest test: " + method, exception);
            }
        });
    }
}

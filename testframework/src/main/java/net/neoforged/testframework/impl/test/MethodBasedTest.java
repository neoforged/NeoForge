/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.impl.ReflectionUtils;

public class MethodBasedTest extends AbstractTest.Dynamic {
    protected MethodHandle handle;
    private final Method method;

    public MethodBasedTest(Method method) {
        this.method = method;

        configureFrom(AnnotationHolder.method(method));

        this.handle = ReflectionUtils.handle(method);
    }

    @Override
    public void init(TestFramework framework) {
        super.init(framework);

        configureGameTest(method.getAnnotation(GameTest.class), method.getAnnotation(EmptyTemplate.class));

        try {
            if (handle.type().parameterCount() == 1) {
                this.handle.invoke(this);
            } else {
                this.handle.invoke(this, registrationHelper());
            }
        } catch (Throwable e) {
            throw new RuntimeException("Encountered exception initiating method-based test: " + method, e);
        }
    }

}

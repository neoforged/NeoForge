/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework;

import java.lang.invoke.MethodType;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.world.entity.Entity;
import net.neoforged.testframework.impl.ReflectionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A listener which listens for changes in tests.
 */
@ParametersAreNonnullByDefault
public interface TestListener {
    /**
     * This method is called when a test changes its status.
     *
     * @param framework the framework of the test
     * @param test      the test that changed its status
     * @param oldStatus the old status of the test
     * @param newStatus the new status of the test
     * @param changer   the entity that changed the status of the test
     */
    default void onStatusChange(TestFramework framework, Test test, Test.Status oldStatus, Test.Status newStatus, @Nullable Entity changer) {}

    /**
     * This method is called when a test is enabled.
     *
     * @param framework the framework of the test
     * @param test      the test that was enabled
     * @param changer   the entity that enabled the test
     */
    default void onEnabled(TestFramework framework, Test test, @Nullable Entity changer) {}

    /**
     * This method is called when a test is disabled.
     *
     * @param framework the framework of the test
     * @param test      the test that was disabled
     * @param changer   the entity that disabled the test
     */
    default void onDisabled(TestFramework framework, Test test, @Nullable Entity changer) {}

    @ApiStatus.Internal
    static TestListener instantiate(Class<? extends TestListener> clazz) {
        try {
            return (TestListener) ReflectionUtils.constructor(clazz, MethodType.methodType(void.class)).invokeWithArguments(List.of());
        } catch (Throwable e) {
            throw new RuntimeException("BARF!", e);
        }
    }
}

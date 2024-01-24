/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.gametest.GameTestData;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class GameTestRegistration {
    public static final Method REGISTER_METHOD = LamdbaExceptionUtils.uncheck(() -> GameTestRegistration.class.getDeclaredMethod("register"));

    @GameTestGenerator
    public static List<TestFunction> register() {
        final List<TestFunction> tests = new ArrayList<>();
        for (final TestFrameworkImpl framework : TestFrameworkImpl.FRAMEWORKS) {
            if (!framework.configuration().isEnabled(Feature.GAMETEST)) continue;

            for (final Test test : framework.tests().all()) {
                final GameTestData data = test.asGameTest();
                if (data != null) {
                    final String batchName = !test.groups().isEmpty() ? test.groups().get(0) : "ungrouped";
                    tests.add(new TestFunction(
                            data.batchName() == null ? batchName : data.batchName(),
                            test.id(), data.structureName(),
                            data.rotation(), data.maxTicks(), data.setupTicks(),
                            data.required(), data.requiredSuccesses(), data.maxAttempts(),
                            rethrow(helper -> {
                                ReflectionUtils.addListener(helper, new GameTestListener() {
                                    @Override
                                    public void testStructureLoaded(GameTestInfo info) {}

                                    @Override
                                    public void testPassed(GameTestInfo info) {
                                        if (framework.tests().getStatus(test.id()).result() == Test.Result.NOT_PROCESSED) {
                                            framework.changeStatus(test, new Test.Status(Test.Result.PASSED, "GameTest passed"), null);
                                        }
                                        disable();
                                    }

                                    @Override
                                    public void testFailed(GameTestInfo info) {
                                        framework.changeStatus(test, new Test.Status(Test.Result.FAILED, "GameTest fail: " + info.getError().getMessage()), null);
                                        disable();
                                    }

                                    private void disable() {
                                        framework.setEnabled(test, false, null);
                                    }
                                });

                                framework.setEnabled(test, true, null);
                                framework.changeStatus(test, Test.Status.DEFAULT, null); // Reset the status, just in case

                                try {
                                    data.function().accept(helper);
                                } catch (GameTestAssertException assertion) {
                                    framework.tests().setStatus(test.id(), new Test.Status(Test.Result.FAILED, "GameTest fail: " + assertion.getMessage()));
                                    throw assertion;
                                }
                            }, GameTestAssertException.class, ex -> framework.setEnabled(test, false, null))));
                }
            }
        }
        return tests;
    }

    private static <T, E extends RuntimeException> Consumer<T> rethrow(Consumer<T> consumer, Class<E> exClass, Consumer<E> ex) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (RuntimeException e) {
                if (exClass.isInstance(e)) ex.accept(exClass.cast(e));
                throw e;
            }
        };
    }
}

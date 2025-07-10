/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import com.google.common.collect.Multimaps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.gametest.GameTestData;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class GameTestRegistration {
    static final class Instance extends GameTestInstance {
        public static final MapCodec<Instance> CODEC = RecordCodecBuilder.mapCodec(
                in -> in.group(
                        TestData.CODEC.fieldOf("data").forGetter(Instance::info),
                        MutableTestFramework.REFERENCE_CODEC.fieldOf("framework").forGetter(i -> i.framework),
                        Codec.STRING.fieldOf("testId").forGetter(i -> i.testId)).apply(in, Instance::new));

        private final TestFramework framework;
        private final String testId;

        public Instance(TestData<Holder<TestEnvironmentDefinition>> data, TestFramework framework, String testId) {
            super(data);
            this.framework = framework;
            this.testId = testId;
        }

        @Override
        public void run(GameTestHelper helper) {
            var test = framework.tests().byId(testId).orElseThrow();
            var game = test.asGameTest();
            try {
                ReflectionUtils.addListener(helper, new GameTestListener() {
                    @Override
                    public void testStructureLoaded(GameTestInfo info) {}

                    @Override
                    public void testPassed(GameTestInfo info, GameTestRunner runner) {
                        if (framework.tests().getStatus(test.id()).result() == Test.Result.NOT_PROCESSED) {
                            framework.changeStatus(test, Test.Status.passed("GameTest passed"), null);
                        }
                        disable();
                    }

                    @Override
                    public void testFailed(GameTestInfo info, GameTestRunner runner) {
                        framework.changeStatus(test, Test.Status.failed("GameTest failure: " + info.getError().getMessage(), info.getError()), null);
                        disable();
                    }

                    @Override
                    public void testAddedForRerun(GameTestInfo pre, GameTestInfo post, GameTestRunner runner) {}

                    private void disable() {
                        framework.setEnabled(test, false, null);
                    }
                });

                framework.setEnabled(test, true, null);
                framework.changeStatus(test, Test.Status.DEFAULT, null); // Reset the status, just in case

                try {
                    game.function().accept(helper);
                } catch (GameTestAssertException assertion) {
                    ((MutableTestFramework) framework).tests().setStatus(test.id(), Test.Status.failed("GameTest failure: " + assertion.getMessage(), assertion));
                    throw assertion;
                }
            } catch (GameTestAssertException exception) {
                framework.setEnabled(test, false, null);
                throw exception;
            }
        }

        @Override
        public MapCodec<? extends GameTestInstance> codec() {
            return CODEC;
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.empty();
        }
    }

    public static void register(RegisterGameTestsEvent event) {
        for (final TestFrameworkImpl framework : TestFrameworkImpl.FRAMEWORKS) {
            if (!framework.configuration().isEnabled(Feature.GAMETEST)) continue;

            record TestEntry(Test test, ResourceLocation batchName, GameTestData gameTestData) {}
            var byBatch = framework.tests().all()
                    .stream().map(t -> {
                        var data = t.asGameTest();
                        if (data == null) return null;
                        ResourceLocation batch;
                        if (data.batchName() != null) {
                            batch = data.batchName().contains(":") ? ResourceLocation.parse(data.batchName().toLowerCase(Locale.ROOT)) : framework.id().withSuffix(data.batchName().toLowerCase(Locale.ROOT));
                        } else {
                            final String batchName = !t.groups().isEmpty() ? t.groups().get(0) : "ungrouped";
                            batch = framework.id().withSuffix("/" + batchName.toLowerCase(Locale.ROOT));
                        }
                        return new TestEntry(t, batch, data);
                    })
                    .filter(Objects::nonNull)
                    .collect(Multimaps.toMultimap(
                            TestEntry::batchName,
                            Function.identity(),
                            () -> Multimaps.newListMultimap(new HashMap<>(), ArrayList::new)));

            for (var entry : byBatch.asMap().entrySet()) {
                var batch = event.registerEnvironment(entry.getKey());
                for (TestEntry testEntry : entry.getValue()) {
                    var test = testEntry.test;
                    var game = testEntry.gameTestData();
                    event.registerTest(
                            framework.id().withSuffix("/" + test.id().toLowerCase(Locale.ROOT)),
                            new Instance(new TestData<>(
                                    batch,
                                    ResourceLocation.parse(game.structureName()),
                                    game.maxTicks(), game.setupTicks(),
                                    game.required(), game.rotation(),
                                    game.manualOnly(), game.maxAttempts(),
                                    game.requiredSuccesses(), game.skyAccess()), framework, test.id()));
                }
            }
        }
    }
}

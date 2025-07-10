/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.summary;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.TestFramework;

public record TestSummary(TestFramework framework, boolean isGameTestRun, List<TestInfo> testInfos) {
    public record TestInfo(
            String testId,
            Component name,
            List<Component> description,
            Test.Status status,
            List<String> groups,
            boolean enabled,
            boolean manual,
            boolean required) {
        public Test.Result result() {
            return status().result();
        }

        public String message() {
            return status().message();
        }
    }

    public static class Builder {
        private final TestFramework framework;
        private final boolean isGameTestRun;
        private final ImmutableList.Builder<TestInfo> tests = ImmutableList.builder();

        public Builder(TestFramework framework, boolean isGameTestRun) {
            this.framework = framework;
            this.isGameTestRun = isGameTestRun;
        }

        public void addTest(String testId, Component name, List<Component> description, Test.Status status, List<String> groups, boolean enabled, boolean manual, boolean required) {
            this.tests.add(new TestInfo(testId, name, List.copyOf(description), status, groups, enabled, manual, required));
        }

        public TestSummary build() {
            return new TestSummary(framework, isGameTestRun, tests.build());
        }
    }
}

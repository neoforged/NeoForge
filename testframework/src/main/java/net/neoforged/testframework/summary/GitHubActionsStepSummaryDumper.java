/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.summary;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.summary.md.Alignment;
import net.neoforged.testframework.summary.md.Table;
import org.slf4j.Logger;

public class GitHubActionsStepSummaryDumper implements FileSummaryDumper {
    private final Function<TestSummary, String> heading;

    public GitHubActionsStepSummaryDumper() {
        this("Test Summary");
    }

    public GitHubActionsStepSummaryDumper(String heading) {
        this(summary -> heading);
    }

    public GitHubActionsStepSummaryDumper(Function<TestSummary, String> heading) {
        this.heading = heading;
    }

    @Override
    public Path outputPath(ResourceLocation frameworkId) {
        return Path.of(System.getenv("GITHUB_STEP_SUMMARY"));
    }

    @Override
    public boolean enabled(TestSummary summary) {
        return summary.isGameTestRun() && System.getenv().containsKey("GITHUB_STEP_SUMMARY");
    }

    @Override
    public void write(TestSummary summary, Logger logger, PrintWriter writer) {
        writer.println("# " + this.heading.apply(summary));
        Map<Test.Result, List<TestSummary.TestInfo>> testsByStatus = summary.testInfos()
                .stream()
                .collect(Collectors.groupingBy(test -> test.status().result(), () -> new EnumMap<>(Test.Result.class), Collectors.toList()));
        List<TestSummary.TestInfo> failedTests = testsByStatus.getOrDefault(Test.Result.FAILED, List.of());
        List<TestSummary.TestInfo> passedTests = testsByStatus.getOrDefault(Test.Result.PASSED, List.of());
        Table.Builder builder = Table.builder()
                .withAlignments(Alignment.LEFT, Alignment.CENTER, Alignment.LEFT, Alignment.LEFT)
                .addRow("Test Id", "Test Result", "Status message", "Test description");
        if (!failedTests.isEmpty()) {
            for (TestSummary.TestInfo failedTest : failedTests) {
                builder.addRow(
                        failedTest.testId(),
                        formatStatus(failedTest.result(), !failedTest.manual() && !failedTest.required()),
                        failedTest.status().message(),
                        getDescription(failedTest));
            }
        }
        if (!passedTests.isEmpty()) {
            for (TestSummary.TestInfo passedTest : passedTests) {
                builder.addRow(
                        passedTest.testId(),
                        formatStatus(passedTest.status().result(), false),
                        passedTest.status().message(),
                        getDescription(passedTest));
            }
        }
        if (!passedTests.isEmpty() && failedTests.isEmpty()) {
            writer.println("All tests passed");
        }
        writer.println();
        writer.println(builder.build());
    }

    protected String formatStatus(Test.Result result, boolean optional) {
        if (result.failed() && !optional) {
            return "❌";
        } else if (result.passed()) {
            return "✅";
        }
        return "⚠️";
    }

    private static String getDescription(TestSummary.TestInfo failedTest) {
        return FormattingUtil.componentsToPlainString(failedTest.description().stream().filter(c -> !c.getString().equals("GameTest-only")).toList());
    }
}

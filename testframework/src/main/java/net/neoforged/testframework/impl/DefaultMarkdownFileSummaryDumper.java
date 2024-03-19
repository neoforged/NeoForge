/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.summary.FileSummaryDumper;
import net.neoforged.testframework.summary.FormattingUtil;
import net.neoforged.testframework.summary.TestSummary;
import net.neoforged.testframework.summary.md.Alignment;
import net.neoforged.testframework.summary.md.Table;
import org.slf4j.Logger;

public class DefaultMarkdownFileSummaryDumper implements FileSummaryDumper {
    @Override
    public Path outputPath(ResourceLocation frameworkId) {
        return Path.of("logs/tests/" + frameworkId.toString().replace(":", "_") + "/summary_" + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replaceAll("[:TZ-]", "") + ".md");
    }

    @Override
    public void write(TestSummary summary, Logger logger, PrintWriter writer) {
        String disabledList = summary.testInfos()
                .stream()
                .filter(info -> !info.enabled())
                .map(info -> "- %s".formatted(info.testId()))
                .collect(Collectors.joining("\n"));

        String enabledList = summary.testInfos()
                .stream()
                .filter(TestSummary.TestInfo::enabled)
                .map(info -> MessageFormat.format("- {0}:\n\t\t{1}", info.testId(), FormattingUtil.componentToMarkdownFormattedText(info.status().asComponent())))
                .collect(Collectors.joining("\n"));

        Table.Builder builder = Table.builder()
                .useFirstRowAsHeader(true)
                .withAlignment(Alignment.CENTER)
                .addRow("Test ID", "Status", "Extra Information");

        for (TestSummary.TestInfo test : summary.testInfos()) {
            Test.Status status = test.status();
            if (!test.enabled() && status.result() == Test.Result.NOT_PROCESSED) continue;

            String actualMessage = status.message().isBlank() ? "-" : status.message();
            builder.addRow(
                    test.testId(),
                    FormattingUtil.componentToMarkdownFormattedText(status.result().asComponent()),
                    status.result() == Test.Result.FAILED ? "<font color=red>" + actualMessage + "</red>" : actualMessage);
        }

        writer.format("""
                # Test Summary

                ## Disabled Tests
                %s

                ## Enabled Tests
                %s

                %s
                """, disabledList, enabledList, builder.build());
    }
}

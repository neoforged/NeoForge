/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.impl.md.Alignment;
import net.neoforged.testframework.impl.md.Table;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record SummaryDumper(MutableTestFramework framework) {
    public String dumpTable() {
        final Table.Builder builder = Table.builder()
                .useFirstRowAsHeader(true)
                .withAlignment(Alignment.CENTER)
                .addRow("Test ID", "Status", "Extra Information");

        framework.tests().all().forEach(test -> {
            final Test.Status status = framework.tests().getStatus(test.id());
            if (!framework.tests().isEnabled(test.id()) && status.result() == Test.Result.NOT_PROCESSED) return;

            final String actualMessage = status.message().isBlank() ? "-" : status.message();
            builder.addRow(test.id(), colouredStatusResult(status), switch (status.result()) {
                case FAILED -> "<font color=red>" + actualMessage + "</red>";
                default -> actualMessage;
            });
        });

        return builder.toString();
    }

    public String createLoggingSummary() {
        final StringBuilder summary = new StringBuilder();

        final Iterator<Test> itr = framework.tests().all().iterator();
        while (itr.hasNext()) {
            final Test test = itr.next();
            final Test.Status status = framework.tests().getStatus(test.id());
            summary.append("\tTest ").append(test.id()).append(":\n");
            summary.append("\t\t").append(status.result()).append(status.message().isBlank() ? "" : " - " + status.message());
            if (itr.hasNext()) summary.append('\n');
        }

        return summary.toString();
    }

    public String createEnabledList() {
        final StringBuilder summary = new StringBuilder();

        final Iterator<Test> itr = enabledTests().iterator();
        while (itr.hasNext()) {
            final Test test = itr.next();
            final Test.Status status = framework.tests().getStatus(test.id());
            summary.append("- ").append(test.id()).append(":\n");
            summary.append("\t\t").append(colouredStatusResult(status)).append(status.message().isBlank() ? "" : " - " + status.message());
            if (itr.hasNext()) summary.append('\n');
        }

        return summary.toString();
    }

    public String createDisabledList() {
        return String.join(", ", framework.tests().all().stream()
                .map(Test::id)
                .filter(id -> !framework.tests().isEnabled(id))
                .toList());
    }

    private Stream<Test> enabledTests() {
        final Predicate<Test> testPredicate = unique().and(test -> framework.tests().isEnabled(test.id()));
        return framework.tests().allGroups()
                .stream()
                .flatMap(group -> group.resolveAsStream().filter(testPredicate));
    }

    private static Predicate<Test> unique() {
        return new HashSet<Test>()::add;
    }

    private static String colouredStatusResult(Test.Status status) {
        return switch (status.result()) {
            case PASSED -> "<font color=green>" + status.result().asHumanReadable() + "</font>";
            case FAILED -> "<font color=red>" + status.result().asHumanReadable() + "</font>";
            default -> status.result().asHumanReadable();
        };
    }
}

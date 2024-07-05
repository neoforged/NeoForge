/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.summary;

import java.text.MessageFormat;
import java.util.stream.Collectors;
import org.slf4j.Logger;

public class DefaultLogSummaryDumper implements SummaryDumper {
    @Override
    public void dump(TestSummary summary, Logger logger) {
        String text = summary.testInfos()
                .stream()
                .map(test -> MessageFormat.format("\tTest {0}:\n\t\t{1}", test.testId(), FormattingUtil.componentToAnsiFormattedText(test.status().asComponent())))
                .collect(Collectors.joining("\n"));
        logger.info("Test summary:\n{}", text);
    }
}

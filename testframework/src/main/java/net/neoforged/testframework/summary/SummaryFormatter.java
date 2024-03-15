/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.summary;

import org.slf4j.Logger;

@FunctionalInterface
public interface SummaryFormatter {
    void format(TestSummary summary, Logger logger);

    default boolean enabled(TestSummary summary) {
        return true;
    }
}

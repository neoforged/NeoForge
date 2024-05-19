/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.summary;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public interface FileSummaryDumper extends SummaryDumper {
    Path outputPath(ResourceLocation frameworkId);

    void write(TestSummary summary, Logger logger, PrintWriter writer) throws Exception;

    default void dump(TestSummary summary, Logger logger) {
        logger.info("Test summary processing...");
        Path outputPath = outputPath(summary.frameworkId());
        try {
            Files.createDirectories(outputPath.getParent());
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputPath))) {
                this.write(summary, logger, writer);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("Wrote test summary to {}", outputPath);
    }
}

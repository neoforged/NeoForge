package net.neoforged.testframework.summary;

import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public interface FileSummaryFormatter extends SummaryFormatter {
    Path outputPath(ResourceLocation frameworkId);

    void write(TestSummary summary, Logger logger, PrintWriter writer) throws Exception;

    default void format(TestSummary summary, Logger logger) {
        logger.info("Test summary processing...");
        Path outputPath = outputPath(summary.frameworkId());
        LamdbaExceptionUtils.uncheck(() -> {
            Files.createDirectories(outputPath.getParent());
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputPath))) {
                this.write(summary, logger, writer);
            }
        });
        logger.info("Wrote test summary to {}", outputPath);
    }
}

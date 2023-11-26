/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import net.neoforged.testframework.TestFramework;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.Builder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public record LoggerSetup(TestFrameworkInternal framework) {

    /**
     * Set the {@link TestFramework#logger()} to only write to logs/tests/{@code id}.log, and to console.
     */
    public void prepareLogger() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final LoggerConfig loggerConfig = getLoggerConfiguration(config, framework.logger().getName());

        loggerConfig.setParent(null);
        loggerConfig.getAppenders().keySet().forEach(loggerConfig::removeAppender);

        loggerConfig.addAppender(
                fileAppender(),
                Level.DEBUG,
                null);
        loggerConfig.addAppender(
                consoleAppender(),
                Level.INFO,
                null);
    }

    private Appender fileAppender() {
        return started(RollingRandomAccessFileAppender.newBuilder()
                .setName("TestFramework " + framework.id() + " file log")
                .withFileName("logs/tests/" + framework.id().toString().replace(":", "_") + "/log.log")
                .withFilePattern("logs/%d{yyyy-MM-dd}-%i.log.gz")
                .setLayout(PatternLayout.newBuilder()
                        .withPattern("[%d{ddMMMyyyy HH:mm:ss}] [%logger]: %minecraftFormatting{%msg}{strip}%n%xEx")
                        .build())
                .withPolicy(
                        OnStartupTriggeringPolicy.createPolicy(1)));
    }

    private Appender consoleAppender() {
        return started(ConsoleAppender.newBuilder()
                .setName("TestFramework " + framework.id() + " console log")
                .setLayout(PatternLayout.newBuilder()
                        .withPattern("%highlightForge{Tests:}{FATAL=magenta, ERROR=magenta, WARN=magenta, INFO=magenta, DEBUG=magenta, TRACE=magenta} %highlightForge{[%d{ddMMMyyyy HH:mm:ss}] [%logger]: %minecraftFormatting{%msg}{strip}%n%xEx}")
                        .build())
                .setIgnoreExceptions(false));
    }

    private static <A extends Appender> A started(Builder<? extends A> builder) {
        final A app = builder.build();
        app.start();
        return app;
    }

    private static LoggerConfig getLoggerConfiguration(@NotNull final Configuration configuration, @NotNull final String loggerName) {
        final LoggerConfig lc = configuration.getLoggerConfig(loggerName);
        if (lc.getName().equals(loggerName)) {
            return lc;
        } else {
            final LoggerConfig nlc = new LoggerConfig(loggerName, lc.getLevel(), lc.isAdditive());
            nlc.setParent(lc);
            configuration.addLogger(loggerName, nlc);
            configuration.getLoggerContext().updateLoggers();

            return nlc;
        }
    }
}

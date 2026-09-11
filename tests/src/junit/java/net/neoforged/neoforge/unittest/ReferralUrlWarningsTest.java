/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.ReferralUrlWarningsHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ReferralUrlWarningsHandler unit tests
 * including missing / wrong URL and much other
 * 
 * @author HowXu {@code <dev@howxu.cn>}
 */
public class ReferralUrlWarningsTest {
    private static final String TARGET_MOD = "referral_url_target_test";
    private static final String MOD_WITH_REFERRAL = "referral_url_present_test";
    private static final String MOD_WITHOUT_REFERRAL = "referral_url_missing_test";
    private static final String MOD_WITH_PLACEHOLDER = "referral_url_placeholder_test";

    @Mod(TARGET_MOD)
    public static class TargetMod {
        public TargetMod(ModContainer container) {}
    }

    @Mod(MOD_WITH_REFERRAL)
    public static class HasReferralUrlMod {
        public HasReferralUrlMod(ModContainer container) {}
    }

    @Mod(MOD_WITHOUT_REFERRAL)
    public static class MissingReferralUrlMod {
        public MissingReferralUrlMod(ModContainer container) {}
    }

    @Mod(MOD_WITH_PLACEHOLDER)
    public static class PlaceholderReferralUrlMod {
        public PlaceholderReferralUrlMod(ModContainer container) {}
    }

    private TestAppender appender;
    private LoggerConfig loggerConfig;
    private LoggerContext loggerContext;

    @BeforeEach
    void setUp() {
        assertTrue(
                ModList.get().getMods().stream().anyMatch(m -> m.getModId().equals(MOD_WITHOUT_REFERRAL)),
                "Test mod '" + MOD_WITHOUT_REFERRAL + "' must be present in ModList for this test to be meaningful");
        assertTrue(
                ModList.get().getMods().stream().anyMatch(m -> m.getModId().equals(MOD_WITH_REFERRAL)),
                "Test mod '" + MOD_WITH_REFERRAL + "' must be present in ModList for this test to be meaningful");
        assertTrue(
                ModList.get().getMods().stream().anyMatch(m -> m.getModId().equals(MOD_WITH_PLACEHOLDER)),
                "Test mod '" + MOD_WITH_PLACEHOLDER + "' must be present in ModList for this test to be meaningful");
        assertTrue(
                ModList.get().getMods().stream().anyMatch(m -> m.getModId().equals(TARGET_MOD)),
                "Test mod '" + TARGET_MOD + "' must be present in ModList for this test to be meaningful");

        loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration config = loggerContext.getConfiguration();
        loggerConfig = config.getLoggerConfig(ReferralUrlWarningsHandler.class.getName());
        appender = new TestAppender("ReferralUrlWarningsTest");
        appender.start();
        loggerConfig.addAppender(appender, Level.ALL, (Filter) null);
        loggerContext.updateLoggers();
    }

    @AfterEach
    void tearDown() {
        loggerConfig.removeAppender("ReferralUrlWarningsTest");
        appender.stop();
        loggerContext.updateLoggers();
    }

    @Test
    void logsWarningForModWithoutReferralUrl() {
        new ReferralUrlWarningsHandler((ModContainer) null);

        assertTrue(
                appender.messages.stream().anyMatch(m -> m.contains(MOD_WITHOUT_REFERRAL)
                        && m.contains(TARGET_MOD)
                        && m.contains("referralUrl")),
                "Expected an INFO line about missing referralUrl for mod '"
                        + MOD_WITHOUT_REFERRAL + "' depending on '" + TARGET_MOD
                        + "'. Captured: " + appender.messages);
    }

    @Test
    void doesNotLogForModWithReferralUrl() {
        new ReferralUrlWarningsHandler((ModContainer) null);

        assertFalse(
                appender.messages.stream().anyMatch(m -> m.contains(MOD_WITH_REFERRAL) && m.contains("without a referralUrl")),
                "Did not expect a warning about missing referralUrl for mod '"
                        + MOD_WITH_REFERRAL + "'. Captured: " + appender.messages);
    }

    @Test
    void logsWarningForModWithPlaceholderReferralUrl() {
        new ReferralUrlWarningsHandler((ModContainer) null);

        assertTrue(
                appender.messages.stream().anyMatch(m -> m.contains(MOD_WITH_PLACEHOLDER)
                        && m.contains(TARGET_MOD)
                        && m.contains("referralUrl")),
                "Expected a warning for mod '" + MOD_WITH_PLACEHOLDER
                        + "' whose referralUrl is the 'myurl.me' placeholder (FML treats it as missing)."
                        + " Captured: " + appender.messages);
    }

    @Test
    void doesNotLogForModWithNoDependencies() {
        new ReferralUrlWarningsHandler((ModContainer) null);

        assertFalse(
                appender.messages.stream().anyMatch(m -> m.contains("Mod '" + TARGET_MOD + "' declares")),
                "Did not expect a warning for stub mod '" + TARGET_MOD
                        + "' which has no dependencies at all. Captured: " + appender.messages);
    }

    private static class TestAppender extends AbstractAppender {
        final List<String> messages = new ArrayList<>();

        protected TestAppender(String name) {
            super(name, (Filter) null, PatternLayout.createDefaultLayout(), false);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }
    }
}

/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class AttachmentHolderTests {
    @Test
    void unknownAttachmentIsReportedAtErrorOncePerKey(MinecraftServer server) {
        // The reported keys are remembered for the lifetime of the process, so use keys no other test has seen
        final String unknownKey = "removed_mod_" + UUID.randomUUID().toString().replace("-", "") + ":attachment";
        final String invalidKey = "Invalid Key " + UUID.randomUUID();
        final CompoundTag attachments = new CompoundTag();
        attachments.put(unknownKey, new CompoundTag());
        attachments.put(invalidKey, new CompoundTag());

        final Logger logger = (Logger) LogManager.getLogger(AttachmentHolder.class);
        final CapturingAppender appender = new CapturingAppender();
        appender.start();
        logger.addAppender(appender);
        try {
            for (int i = 0; i < 3; i++) {
                final TestAttachmentHolder holder = new TestAttachmentHolder();
                holder.attachments.deserializeInternal(server.registryAccess(), TagValueInput.create(ProblemReporter.DISCARDING, server.registryAccess(), attachments));
                Assertions.assertFalse(holder.hasAttachments(), "Unknown and invalid attachments should be skipped");
            }
        } finally {
            logger.removeAppender(appender);
            appender.stop();
        }

        Assertions.assertEquals(1, appender.countErrorsMentioning(unknownKey), "Unknown attachment should be reported at error level once");
        Assertions.assertEquals(1, appender.countErrorsMentioning(invalidKey), "Invalid attachment key should be reported at error level once");
    }

    private static final class TestAttachmentHolder implements IAttachmentHolder {
        private final AttachmentHolder.AsField attachments = new AttachmentHolder.AsField(this);

        @Override
        public boolean hasAttachments() {
            return this.attachments.hasAttachments();
        }

        @Override
        public boolean hasData(AttachmentType<?> type) {
            return this.attachments.hasData(type);
        }

        @Override
        public <T> T getData(AttachmentType<T> type) {
            return this.attachments.getData(type);
        }

        @Override
        public <T> @Nullable T getExistingDataOrNull(AttachmentType<T> type) {
            return this.attachments.getExistingDataOrNull(type);
        }

        @Override
        public <T> @Nullable T setData(AttachmentType<T> type, T data) {
            return this.attachments.setData(type, data);
        }

        @Override
        public <T> @Nullable T removeData(AttachmentType<T> type) {
            return this.attachments.removeData(type);
        }
    }

    private static final class CapturingAppender extends AbstractAppender {
        private final List<LogEvent> events = new CopyOnWriteArrayList<>();

        private CapturingAppender() {
            super("AttachmentHolderTests", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            this.events.add(event.toImmutable());
        }

        private long countErrorsMentioning(String key) {
            return this.events.stream()
                    .filter(event -> event.getLevel() == Level.ERROR)
                    .filter(event -> event.getMessage().getFormattedMessage().contains(key))
                    .count();
        }
    }
}

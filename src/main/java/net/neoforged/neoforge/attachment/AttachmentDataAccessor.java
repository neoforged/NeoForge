/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.neoforged.neoforge.attachment.storage.AttachmentDataStorage;
import org.jspecify.annotations.Nullable;

@org.jetbrains.annotations.ApiStatus.OverrideOnly
public interface AttachmentDataAccessor extends AttachmentDataStorage {
    AttachmentDataStorage attachmentDataStorage();

    @Override
    default boolean hasAttachments() {
        return attachmentDataStorage().hasAttachments();
    }

    @Override
    default boolean hasData(AttachmentType<?> attachmentType) {
        return attachmentDataStorage().hasData(attachmentType);
    }

    @Override
    default <T> T getData(AttachmentType<T> attachmentType) {
        return attachmentDataStorage().getData(attachmentType);
    }

    @Override
    default <T> Optional<T> getExistingData(AttachmentType<T> attachmentType) {
        return attachmentDataStorage().getExistingData(attachmentType);
    }

    @Override
    default <T> T getExistingDataOrNull(AttachmentType<T> attachmentType) {
        return attachmentDataStorage().getExistingDataOrNull(attachmentType);
    }

    @Override
    default <T> @Nullable T getExistingDataOrNull(Supplier<AttachmentType<T>> type) {
        return attachmentDataStorage().getExistingDataOrNull(type);
    }

    @Override
    default <T> @Nullable T setData(AttachmentType<T> attachmentType, T t) {
        return attachmentDataStorage().setData(attachmentType, t);
    }

    @Override
    default <T> @Nullable T removeData(AttachmentType<T> attachmentType) {
        return attachmentDataStorage().removeData(attachmentType);
    }

    @Override
    default int size() {
        return attachmentDataStorage().size();
    }

    @Override
    default Stream<AttachmentType<?>> storedTypes() {
        return attachmentDataStorage().storedTypes();
    }

    @Override
    default void putDataNoSync(AttachmentType<?> type, Object copy) {
        attachmentDataStorage().putDataNoSync(type, copy);
    }
}

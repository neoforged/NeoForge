/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * An attachment holder that forwards all non-default implementations from {@link IAttachmentHolder}
 * to an implementation exposed on the class. Particularly useful when combined with
 * {@link net.neoforged.neoforge.attachment.AttachmentHolder.AsField} attachment holders.
 */
@org.jetbrains.annotations.ApiStatus.OverrideOnly
public interface IForwardingAttachmentHolder extends IAttachmentHolder {
    IAttachmentHolder getAttachmentHolder();

    @Override
    default boolean hasAttachments() {
        return getAttachmentHolder().hasAttachments();
    }

    @Override
    default boolean hasData(AttachmentType<?> attachmentType) {
        return getAttachmentHolder().hasData(attachmentType);
    }

    @Override
    default <T> T getData(AttachmentType<T> attachmentType) {
        return getAttachmentHolder().getData(attachmentType);
    }

    @Override
    default <T> Optional<T> getExistingData(AttachmentType<T> attachmentType) {
        return getAttachmentHolder().getExistingData(attachmentType);
    }

    @Override
    default <T> @Nullable T setData(AttachmentType<T> attachmentType, T t) {
        return getAttachmentHolder().setData(attachmentType, t);
    }

    @Override
    default <T> @Nullable T removeData(AttachmentType<T> attachmentType) {
        return getAttachmentHolder().removeData(attachmentType);
    }

    @Override
    default <T> @Nullable T getExistingDataOrNull(Supplier<AttachmentType<T>> type) {
        return getAttachmentHolder().getExistingDataOrNull(type);
    }

    @Override
    default <T> T getExistingDataOrNull(AttachmentType<T> attachmentType) {
        return getAttachmentHolder().getExistingDataOrNull(attachmentType);
    }
}

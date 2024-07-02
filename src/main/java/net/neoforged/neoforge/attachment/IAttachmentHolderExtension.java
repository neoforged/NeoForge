/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import java.util.Optional;
import java.util.stream.Stream;

public interface IAttachmentHolderExtension<T> extends IAttachmentHolder<T> {
    IAttachmentHolder<T> dataAttachments();

    default T parent() {
        return dataAttachments().parent();
    }

    @Override
    default boolean hasAttachments() {
        return dataAttachments().hasAttachments();
    }

    @Override
    default boolean hasData(AttachmentType<?> type) {
        return dataAttachments().hasData(type);
    }

    @Override
    default <T> T getData(AttachmentType<T> type) {
        return dataAttachments().getData(type);
    }

    @Override
    default <T> Optional<T> getExistingData(AttachmentType<T> type) {
        return dataAttachments().getExistingData(type);
    }

    @Override
    default <T> T setData(AttachmentType<T> type, T data) {
        return dataAttachments().setData(type, data);
    }

    @Override
    default <T> T removeData(AttachmentType<T> type) {
        return dataAttachments().removeData(type);
    }

    @Override
    default Stream<AttachmentType<?>> existingDataTypes() {
        return dataAttachments().existingDataTypes();
    }
}

/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/**
 * An object that can hold data attachments.
 */
public interface IAttachmentHolder<H extends IAttachmentHolder<H>> {
    /**
     * Returns {@code true} if there is a data attachment of the give type, {@code false} otherwise.
     */
    boolean hasData(AttachmentType<? super H, ?> type);

    /**
     * Returns {@code true} if there is a data attachment of the give type, {@code false} otherwise.
     */
    default <AT extends AttachmentType<? super H, ?>> boolean hasData(Supplier<AT> type) {
        return hasData(type.get());
    }

    /**
     * {@return the data attachment of the given type}
     *
     * <p>If there is no data attachment of the given type, <b>the default value is stored in this holder and returned.</b>
     */
    <T> T getData(AttachmentType<? super H, T> type);

    /**
     * {@return the data attachment of the given type}
     *
     * <p>If there is no data attachment of the given type, <b>the default value is stored in this holder and returned.</b>
     */
    default <T, AT extends AttachmentType<? super H, T>> T getData(Supplier<AT> type) {
        return getData(type.get());
    }

    /**
     * Sets the data attachment of the given type.
     *
     * @return the previous value for that attachment type, if any, or {@code null} if there was none
     */
    <T> @Nullable T setData(AttachmentType<? super H, T> type, T data);

    /**
     * Sets the data attachment of the given type.
     *
     * @return the previous value for that attachment type, if any, or {@code null} if there was none
     */
    default <T, AT extends AttachmentType<? super H, T>> @Nullable T setData(Supplier<AT> type, T data) {
        return setData(type.get(), data);
    }
}

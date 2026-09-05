/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.storage;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

public class SimpleAttachmentDataStorage implements AttachmentDataStorage, ValidatingAttachmentDataStorage {
    private final IAttachmentHolder holder;
    protected final Map<AttachmentType<?>, Object> attachments;

    public SimpleAttachmentDataStorage(IAttachmentHolder holder) {
        this.holder = holder;
        this.attachments = new IdentityHashMap<>();
    }

    @Override
    public final boolean hasAttachments() {
        return !attachments.isEmpty();
    }

    @Override
    public final boolean hasData(AttachmentType<?> type) {
        validateAttachmentType(holder, type);
        return attachments.containsKey(type);
    }

    @Override
    public final <T> T getData(AttachmentType<T> type) {
        validateAttachmentType(holder, type);
        //noinspection unchecked
        return (T) attachments.computeIfAbsent(type, t -> t.defaultValueSupplier.apply(holder));
    }

    @Override
    @Nullable
    public <T> T getExistingDataOrNull(AttachmentType<T> type) {
        validateAttachmentType(holder, type);
        //noinspection unchecked
        return (T) this.attachments.get(type);
    }

    @Override
    @MustBeInvokedByOverriders
    public <T> @Nullable T setData(AttachmentType<T> type, T data) {
        validateAttachmentType(holder, type);
        Objects.requireNonNull(data);
        //noinspection unchecked
        return (T) attachments.put(type, data);
    }

    @Override
    @MustBeInvokedByOverriders
    public <T> @Nullable T removeData(AttachmentType<T> type) {
        validateAttachmentType(holder, type);
        //noinspection unchecked
        return (T) attachments.remove(type);
    }

    @Override
    public int size() {
        return attachments.size();
    }

    @Override
    public Stream<AttachmentType<?>> storedTypes() {
        return attachments.keySet().stream();
    }

    @Override
    public void putDataNoSync(AttachmentType<?> type, Object copy) {
        attachments.put(type, copy);
    }
}

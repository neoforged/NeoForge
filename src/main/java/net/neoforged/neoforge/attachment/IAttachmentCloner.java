package net.neoforged.neoforge.attachment;

import javax.annotation.Nullable;

/**
 * Custom cloner for data attachments, to improve efficiency compared to the default
 * serialize-copy nbt-deserialize-implementation.
 */
public interface IAttachmentCloner<T> {
    @Nullable
    T copy(IAttachmentHolder holder, T attachment);
}

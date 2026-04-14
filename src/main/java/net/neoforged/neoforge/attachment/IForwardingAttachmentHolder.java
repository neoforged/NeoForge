package net.neoforged.neoforge.attachment;

import org.jetbrains.annotations.Nullable;

/**
 * An attachment holder that forwards all non-default implementations from {@link IAttachmentHolder}
 * to an implementation exposed on the class. Particularly useful when combined with
 * {@link net.neoforged.neoforge.attachment.AttachmentHolder.Forwarding} attachment holders.
 */
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
    default <T> @Nullable T getExistingDataOrNull(AttachmentType<T> attachmentType) {
        return getAttachmentHolder().getExistingDataOrNull(attachmentType);
    }

    @Override
    default <T> @Nullable T setData(AttachmentType<T> attachmentType, T t) {
        return getAttachmentHolder().setData(attachmentType, t);
    }

    @Override
    default <T> @Nullable T removeData(AttachmentType<T> attachmentType) {
        return getAttachmentHolder().removeData(attachmentType);
    }
}
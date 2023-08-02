package net.minecraftforge.registries.attachment;

import javax.annotation.Nullable;

public interface IWithAttachments<T> {
    default <A> @Nullable A getAttachment(AttachmentTypeKey<A> key) {
        return null;
    }
}

package net.neoforged.neoforge.registries.attachment;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a registry object (usually a {@link net.minecraft.core.Holder}) that has registry attachments.
 *
 * @param <R> the type of the object
 */
public interface IWithRegistryAttachments<R> {
    /**
     * {@return the attachment of the given type that is attached to this object, or {@code null} if one isn't}
     *
     * @param type the attachment type
     * @param <T>  the type of the attachment
     */
    @Nullable
    default <T> T getAttachment(RegistryAttachment<T, R, ?> type) {
        return null;
    }
}

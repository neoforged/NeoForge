package net.neoforged.neoforge.registries.attachment;

import org.jetbrains.annotations.Nullable;

public interface IWithRegistryAttachments<R> {
    @Nullable
    default <T> T getAttachment(RegistryAttachment<T, R, ?> attachment) {
        return null;
    }
}

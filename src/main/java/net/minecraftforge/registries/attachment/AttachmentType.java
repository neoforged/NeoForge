/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries.attachment;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents the type of an arbitrary value attached to registry entries. <br>
 * An attachment type is registered via the {@link RegisterAttachmentTypeEvent event}. <br>
 * <strong>Note:</strong> {@linkplain net.minecraftforge.registries.IForgeRegistry Forge registries} can have attachments but <i>only</i>
 * if they have a {@linkplain net.minecraftforge.registries.RegistryBuilder#hasWrapper() wrapper}.
 *
 * @param <A> the type of the attachment object
 * @param <T> the type of the registry object to attach to
 * @see AttachmentTypeBuilder
 */
public record AttachmentType<A, T>(
        AttachmentTypeKey<A> key, ResourceKey<Registry<T>> registryKey,
        Function<T, @Nullable A> defaultProvider,
        AttachmentValueMerger<A> valueMerger,
        Codec<A> attachmentCodec,
        @Nullable Codec<A> networkCodec,
        boolean forciblySynced
)
{
    /**
     * @apiNote this class should not be constructed manually, but instead via a {@link #builder(AttachmentTypeKey, ResourceKey)}  builder}.
     */
    @ApiStatus.Internal
    public AttachmentType
    {
    }

    public static <A, T> AttachmentTypeBuilder<A, T> builder(AttachmentTypeKey<A> key, ResourceKey<Registry<T>> registryKey)
    {
        return new AttachmentTypeBuilder<>(key, registryKey);
    }
}
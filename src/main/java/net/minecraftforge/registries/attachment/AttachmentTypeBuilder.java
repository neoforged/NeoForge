/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries.attachment;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A builder for {@link AttachmentType AttachmentTypes}. <br>
 * Generally you will interact with the builder in the {@linkplain java.util.function.Consumer} {@link RegisterAttachmentTypeEvent#register(ResourceKey, AttachmentTypeKey, Consumer)}
 * provides.
 *
 * @param <A> the type of the attachment object
 * @param <T> the type of the registry object to attach to
 */
public final class AttachmentTypeBuilder<A, T>
{
    private final AttachmentTypeKey<A> key;
    private final ResourceKey<Registry<T>> registryKey;
    private Function<T, @Nullable A> defaultProvider = a -> null;
    private Codec<A> attachmentCodec, networkCodec;
    private AttachmentValueMerger<A> valueMerger = (AttachmentValueMerger) AttachmentValueMerger.DEFAULT;
    private boolean forciblySynced = true;

    AttachmentTypeBuilder(AttachmentTypeKey<A> key, ResourceKey<Registry<T>> registryKey)
    {
        this.key = Objects.requireNonNull(key, "type key must not be null");
        this.registryKey = Objects.requireNonNull(registryKey, "registry key must not be null");
    }

    /**
     * Sets the function that will provide the default attachment for a given object. <br>
     * This will <strong>only</strong> be called once, during attachment loading, for all objects that do not have a value attached yet.
     *
     * @param provider the function that provides default attachment values
     * @return the builder instance
     */
    public AttachmentTypeBuilder<A, T> provideDefault(Function<T, @Nullable A> provider)
    {
        this.defaultProvider = Objects.requireNonNull(provider, "default provider must not be null");
        return this;
    }

    /**
     * Sets the codec used to decode the attachment values from the datapack JSON entries.
     *
     * @param attachmentCodec the codec to be used for loading data from datapacks
     * @return the builder instance
     */
    public AttachmentTypeBuilder<A, T> withAttachmentCodec(Codec<A> attachmentCodec)
    {
        this.attachmentCodec = attachmentCodec;
        return this;
    }

    /**
     * Sets the codec used to encode and decode the attachment values for syncing to clients.
     *
     * @param networkCodec the syncing codec. If {@code null}, this attachment type will not be synced to clients (default behavior).
     * @return the builder instance
     */
    public AttachmentTypeBuilder<A, T> withNetworkCodec(@Nullable Codec<A> networkCodec)
    {
        this.networkCodec = networkCodec;
        return this;
    }

    /**
     * Sets the merger used to merge two different attachments for the same object. <br>
     * Defaults to {@link AttachmentValueMerger#DEFAULT}.
     *
     * @param merger the value merge
     * @return the builder instance
     */
    public AttachmentTypeBuilder<A, T> withMerger(AttachmentValueMerger<A> merger)
    {
        this.valueMerger = Objects.requireNonNull(merger, "merger must not be null");
        return this;
    }

    /**
     * This will make the attachment type only sync when it is present on the client-side, ignoring vanilla connections
     * or any connections that do not have it present. <br>
     * The default behavior for synced attachments is to not accept any connection that does not have the attachment type present.
     *
     * @return the builder instance
     */
    public AttachmentTypeBuilder<A, T> setOptionallySynced()
    {
        this.forciblySynced = false;
        return this;
    }

    /**
     * {@return a built attachment type}
     */
    public AttachmentType<A, T> build()
    {
        if (networkCodec == null) forciblySynced = false;
        Objects.requireNonNull(attachmentCodec, "attachment codec must not be null");
        return new AttachmentType<>(
                key, registryKey, defaultProvider,
                valueMerger,
                attachmentCodec, networkCodec,
                forciblySynced
        );
    }

}
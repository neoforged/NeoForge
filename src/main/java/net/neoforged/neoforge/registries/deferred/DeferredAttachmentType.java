/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Special {@link DeferredHolder} for {@link AttachmentType AttachmentTypes}.
 *
 * @param <TData> The specific data type.
 */
public class DeferredAttachmentType<TData> extends DeferredHolder<AttachmentType<?>, AttachmentType<TData>> {
    protected DeferredAttachmentType(ResourceKey<AttachmentType<?>> key) {
        super(key);
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the specified {@link AttachmentType}.
     *
     * @param <TData>     The type of the target {@link AttachmentType}.
     * @param registryKey The resource key of the target {@link AttachmentType}.
     */
    public static <TData> DeferredAttachmentType<TData> createAttachmentType(ResourceKey<AttachmentType<?>> registryKey) {
        return new DeferredAttachmentType<>(registryKey);
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the {@link AttachmentType} with the specified name.
     *
     * @param <TData>      The type of the target {@link AttachmentType}.
     * @param registryName The name of the target {@link AttachmentType}.
     */
    public static <TData> DeferredAttachmentType<TData> createAttachmentType(ResourceLocation registryName) {
        return createAttachmentType(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, registryName));
    }
}

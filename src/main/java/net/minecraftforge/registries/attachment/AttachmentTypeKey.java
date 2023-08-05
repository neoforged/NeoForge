/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries.attachment;

import com.google.common.collect.MapMaker;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.ConcurrentMap;

/**
 * An interned key used to access registry attachments.
 *
 * @param <T> the type of the attachment object this key is for
 * @see #get(ResourceLocation)
 */
public final class AttachmentTypeKey<T>
{
    private static final ConcurrentMap<ResourceLocation, AttachmentTypeKey<?>> VALUES = new MapMaker().weakValues().makeMap();

    private final ResourceLocation id;

    private AttachmentTypeKey(ResourceLocation id)
    {
        this.id = id;
    }

    /**
     * Gets or creates an interned attachment key with the given {@code id}.
     *
     * @param id  the ID of the attachment type
     * @param <T> the type of the attachment object the key is for
     * @return the interned key
     */
    public static <T> AttachmentTypeKey<T> get(ResourceLocation id)
    {
        return (AttachmentTypeKey<T>) VALUES.computeIfAbsent(id, AttachmentTypeKey::new);
    }

    /**
     * {@return the ID of this attachment type}
     * {@link AttachmentTypeKey AttachmentTypeKeys} are interned based on this value.
     */
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return "AttachmentTypeKey[" + id + "]";
    }
}

/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries.attachment;

import com.google.common.collect.Maps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.GameData;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stores and manages the attachments of a {@link Registry}.
 *
 * @param <T> the registry type
 */
public final class AttachmentHolder<T>
{
    private final ResourceKey<? extends Registry<T>> registryKey;
    private Map<AttachmentTypeKey<?>, AttachmentType<?, T>> attachmentTypes;
    private Map<AttachmentTypeKey<?>, IdentityHashMap<Object, ?>> attachments;
    private Map<AttachmentTypeKey<?>, Map<Object, ?>> attachmentView;

    @ApiStatus.Internal
    public AttachmentHolder(ResourceKey<? extends Registry<T>> registryKey)
    {
        this.registryKey = registryKey;
        if (attachmentsRegistered) populateMaps();
    }

    /**
     * {@return a view of all the attachment types the registry this holder is associated with supports}
     *
     * @throws IllegalStateException if the attachments are not yet initialised
     */
    public Map<AttachmentTypeKey<?>, AttachmentType<?, T>> getAttachmentTypes()
    {
        if (attachmentTypes == null) attemptPopulate();
        return attachmentTypes;
    }

    /**
     * {@return a view of the attachments this holder holds} <br>
     * The value maps associate either a {@link net.minecraft.tags.TagKey} or a {@link ResourceKey} to an attachment.
     *
     * @throws IllegalStateException if the attachments are not yet initialised
     */
    public Map<AttachmentTypeKey<?>, Map<Object, ?>> getAttachments()
    {
        if (attachmentView == null) attemptPopulate();
        return attachmentView;
    }

    @ApiStatus.Internal
    public void bindAttachments(Map<AttachmentTypeKey<?>, Map<Object, ?>> attachments)
    {
        if (this.attachments == null) populateMaps();
        this.attachments.values().forEach(Map::clear);
        attachments.forEach((key, value) ->
        {
            final var toReplaceIn = this.attachments.get(key);
            if (toReplaceIn == null)
            {
                throw new IllegalArgumentException("Attempted to bind attachments of type " + key.getId() + " to registry " + this.registryKey + " which does not support such attachments!");
            }
            toReplaceIn.putAll((Map) value);
        });
    }

    private void populateMaps()
    {
        this.attachmentTypes = (Map) Map.copyOf(registeredAttachmentTypes.getOrDefault(registryKey, Map.of()));
        this.attachments = attachmentTypes.keySet().stream().collect(Collectors.toMap(Function.identity(), k -> new IdentityHashMap<>(), (a, b) -> b, IdentityHashMap::new));
        this.attachmentView = Collections.unmodifiableMap(attachments.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, m -> Collections.unmodifiableMap(m.getValue()), (a, b) -> b, IdentityHashMap::new)));
    }

    private void attemptPopulate()
    {
        if (attachmentsRegistered)
        {
            populateMaps();
        } else
        {
            throw new IllegalStateException("Attachment holders are not yet initialised!");
        }
    }

    private static boolean attachmentsRegistered;
    private static Map<ResourceKey<Registry<?>>, Map<AttachmentTypeKey<?>, AttachmentType<?, ?>>> registeredAttachmentTypes;

    @Deprecated
    public static void init()
    {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != GameData.class)
        {
            throw new IllegalCallerException();
        }
        attachmentsRegistered = true;
        registeredAttachmentTypes = Map.copyOf(GameData.gatherAttachmentTypes());
    }

    /**
     * {@returns the attachment types that are forcibly synced to clients}
     */
    public static Map<ResourceKey<? extends Registry<?>>, List<AttachmentTypeKey<?>>> getForciblySyncedAttachments()
    {
        final Map<ResourceKey<? extends Registry<?>>, List<AttachmentTypeKey<?>>> types = Maps.newHashMapWithExpectedSize(registeredAttachmentTypes.size());
        registeredAttachmentTypes.forEach((key, attachmentTypes) ->
                types.put(key, (List) attachmentTypes.values().stream()
                        .filter(t -> t.forciblySynced() && t.networkCodec() != null)
                        .map(AttachmentType::key)
                        .toList()));
        return types;
    }
}

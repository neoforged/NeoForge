/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries.attachment;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RegisterAttachmentTypeEvent extends Event implements IModBusEvent
{
    private final Map<ResourceKey<Registry<?>>, Map<AttachmentTypeKey<?>, AttachmentType<?, ?>>> values;

    @ApiStatus.Internal
    public RegisterAttachmentTypeEvent(Map<ResourceKey<Registry<?>>, Map<AttachmentTypeKey<?>, AttachmentType<?, ?>>> values)
    {
        this.values = values;
    }

    public <A, T> void register(ResourceKey<Registry<T>> registryKey, AttachmentTypeKey<A> key, Consumer<AttachmentTypeBuilder<A, T>> builderConsumer)
    {
        final Map<AttachmentTypeKey<?>, AttachmentType<?, T>> attachmentsForRegistry = values.computeIfAbsent((ResourceKey) registryKey, k -> new HashMap<>());
        if (attachmentsForRegistry.containsKey(key))
        {
            throw new UnsupportedOperationException("Attempted to override attachment type with ID '" + key + "' for registry '" + registryKey.location() + "'!");
        }
        final AttachmentTypeBuilder<A, T> builder = AttachmentType.builder(key, registryKey);
        builderConsumer.accept(builder);
        attachmentsForRegistry.put(key, builder.build());
    }
}

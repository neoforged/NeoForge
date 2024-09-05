/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Specialized DeferredRegister for {@link AttachmentType AttachmentTypes} that uses the specialized {@link DeferredAttachmentType} as the return type for {@link #register}.
 */
public class DeferredAttachmentTypes extends DeferredRegister<AttachmentType<?>> {
    protected DeferredAttachmentTypes(String namespace) {
        super(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, namespace);
    }

    @Override
    protected <TAttachmentType extends AttachmentType<?>> DeferredHolder<AttachmentType<?>, TAttachmentType> createHolder(ResourceKey<? extends Registry<AttachmentType<?>>> registryType, ResourceLocation registryName) {
        return (DeferredHolder<AttachmentType<?>, TAttachmentType>) DeferredAttachmentType.createAttachmentType(ResourceKey.create(registryType, registryName));
    }

    /**
     * Adds a new attachment type to the list of entries to be registered and returns a {@link DeferredAttachmentType} that will be populated with the created entry automatically.
     *
     * @param identifier    The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory       A factory for the new entry. The factory should not cache the created entry.
     * @param builderAction Action to be invoked with the builder during registration.
     * @return A {@link DeferredAttachmentType} that will track updates from the registry for this entry.
     */
    public <TData> DeferredAttachmentType<TData> registerAttachmentType(String identifier, Function<IAttachmentHolder, TData> factory, UnaryOperator<AttachmentType.Builder<TData>> builderAction) {
        return (DeferredAttachmentType<TData>) register(identifier, () -> builderAction.apply(AttachmentType.builder(factory)).build());
    }

    /**
     * Adds a new attachment type to the list of entries to be registered and returns a {@link DeferredAttachmentType} that will be populated with the created entry automatically.
     *
     * @param identifier The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new entry. The factory should not cache the created entry.
     * @return A {@link DeferredAttachmentType} that will track updates from the registry for this entry.
     */
    public <TData> DeferredAttachmentType<TData> registerAttachmentType(String identifier, Function<IAttachmentHolder, TData> factory) {
        return registerAttachmentType(identifier, factory, UnaryOperator.identity());
    }

    /**
     * Adds a new attachment type to the list of entries to be registered and returns a {@link DeferredAttachmentType} that will be populated with the created entry automatically.
     *
     * @param identifier    The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory       A factory for the new entry. The factory should not cache the created entry.
     * @param builderAction Action to be invoked with the builder during registration.
     * @return A {@link DeferredAttachmentType} that will track updates from the registry for this entry.
     */
    public <TData> DeferredAttachmentType<TData> registerAttachmentType(String identifier, Supplier<TData> factory, UnaryOperator<AttachmentType.Builder<TData>> builderAction) {
        return registerAttachmentType(identifier, holder -> factory.get(), builderAction);
    }

    /**
     * Adds a new attachment type to the list of entries to be registered and returns a {@link DeferredAttachmentType} that will be populated with the created entry automatically.
     *
     * @param identifier The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new entry. The factory should not cache the created entry.
     * @return A {@link DeferredAttachmentType} that will track updates from the registry for this entry.
     */
    public <TData> DeferredAttachmentType<TData> registerAttachmentType(String identifier, Supplier<TData> factory) {
        return registerAttachmentType(identifier, factory, UnaryOperator.identity());
    }

    /**
     * Adds a new serializable attachment type to the list of entries to be registered and returns a {@link DeferredAttachmentType} that will be populated with the created entry automatically.
     *
     * @param identifier    The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory       A factory for the new entry. The factory should not cache the created entry.
     * @param builderAction Action to be invoked with the builder during registration.
     * @return A {@link DeferredAttachmentType} that will track updates from the registry for this entry.
     */
    public <TData extends INBTSerializable<TTag>, TTag extends Tag> DeferredAttachmentType<TData> registerSerializableAttachmentType(String identifier, Function<IAttachmentHolder, TData> factory, UnaryOperator<AttachmentType.Builder<TData>> builderAction) {
        return (DeferredAttachmentType<TData>) register(identifier, () -> builderAction.apply(AttachmentType.serializable(factory)).build());
    }

    /**
     * Adds a new serializable attachment type to the list of entries to be registered and returns a {@link DeferredAttachmentType} that will be populated with the created entry automatically.
     *
     * @param identifier The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new entry. The factory should not cache the created entry.
     * @return A {@link DeferredAttachmentType} that will track updates from the registry for this entry.
     */
    public <TData extends INBTSerializable<TTag>, TTag extends Tag> DeferredAttachmentType<TData> registerSerializableAttachmentType(String identifier, Function<IAttachmentHolder, TData> factory) {
        return registerSerializableAttachmentType(identifier, factory, UnaryOperator.identity());
    }

    /**
     * Adds a new serializable attachment type to the list of entries to be registered and returns a {@link DeferredAttachmentType} that will be populated with the created entry automatically.
     *
     * @param identifier    The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory       A factory for the new entry. The factory should not cache the created entry.
     * @param builderAction Action to be invoked with the builder during registration.
     * @return A {@link DeferredAttachmentType} that will track updates from the registry for this entry.
     */
    public <TData extends INBTSerializable<TTag>, TTag extends Tag> DeferredAttachmentType<TData> registerSerializableAttachmentType(String identifier, Supplier<TData> factory, UnaryOperator<AttachmentType.Builder<TData>> builderAction) {
        return registerSerializableAttachmentType(identifier, holder -> factory.get(), builderAction);
    }

    /**
     * Adds a new serializable attachment type to the list of entries to be registered and returns a {@link DeferredAttachmentType} that will be populated with the created entry automatically.
     *
     * @param identifier The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new entry. The factory should not cache the created entry.
     * @return A {@link DeferredAttachmentType} that will track updates from the registry for this entry.
     */
    public <TData extends INBTSerializable<TTag>, TTag extends Tag> DeferredAttachmentType<TData> registerSerializableAttachmentType(String identifier, Supplier<TData> factory) {
        return registerSerializableAttachmentType(identifier, factory, UnaryOperator.identity());
    }

    /**
     * Factory for a specialized DeferredRegister for {@link AttachmentType AttachmentTypes}.
     *
     * @param namespace The namespace for all objects registered to this DeferredRegister
     */
    public static DeferredAttachmentTypes createAttachmentTypes(String namespace) {
        return new DeferredAttachmentTypes(namespace);
    }
}

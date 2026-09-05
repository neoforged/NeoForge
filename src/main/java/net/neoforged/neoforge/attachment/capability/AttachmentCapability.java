/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.capability;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.capabilities.CapabilityRegistry;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public class AttachmentCapability<TAttachment, TContext extends @Nullable Object> extends BaseCapability<TAttachment, TContext> {
    private static final CapabilityRegistry<AttachmentCapability<?, ?>> registry = new CapabilityRegistry<>(AttachmentCapability::new);
    private final Map<Class<TAttachment>, List<ICapabilityProvider<IAttachmentHolder, TContext, TAttachment>>> providers = new IdentityHashMap<>();

    protected AttachmentCapability(Identifier name, Class<TAttachment> typeClass, Class<TContext> contextClass) {
        super(name, typeClass, contextClass);
    }

    public static <T, C extends @Nullable Object> AttachmentCapability<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        //noinspection unchecked
        return (AttachmentCapability<T, C>) registry.create(name, typeClass, contextClass);
    }

    public static <T> AttachmentCapability<T, @Nullable Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    @ApiStatus.Internal
    public <THolder extends IAttachmentHolder> void register(Class<THolder> holderClass, AttachmentCapability<TAttachment, TContext> capability, ICapabilityProvider<THolder, TContext, TAttachment> provider) {
        Objects.requireNonNull(provider);
        ICapabilityProvider<IAttachmentHolder, TContext, TAttachment> adapted = (holder, ctx) -> provider.getCapability(holderClass.cast(holder), ctx);

        providers.computeIfAbsent(capability.typeClass(), b -> new ArrayList<>()).add(adapted);
    }

    @ApiStatus.Internal
    @Nullable
    public <THolder extends IAttachmentHolder> TAttachment getCapability(THolder host, @Nullable TContext context) {
        for (var provider : providers.getOrDefault(this.typeClass(), List.of())) {
            final TAttachment ret = provider.getCapability(host, context);
            if (ret != null)
                return typeClass().cast(ret);
        }

        return null;
    }
}

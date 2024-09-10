/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.registration;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.neoforged.neoforge.attachment.AttachmentType;

public class DeferredAttachmentTypes extends net.neoforged.neoforge.registries.deferred.DeferredAttachmentTypes {
    public DeferredAttachmentTypes(String namespace) {
        super(namespace);
    }

    public <T> AttachmentType<T> registerSimpleAttachment(String name, Supplier<T> defaultValue) {
        return register(name, defaultValue, UnaryOperator.identity());
    }

    public <T> AttachmentType<T> register(String name, Supplier<T> defaultValue, UnaryOperator<AttachmentType.Builder<T>> factory) {
        final var attach = factory.apply(AttachmentType.builder(defaultValue)).build();
        register(name, () -> attach);
        return attach;
    }
}

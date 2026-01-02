/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Base class to reuse code common between most/all {@code *Capability} implementation.
 *
 * <p>This is only relevant for authors of new capability types
 * (i.e. which are not {@linkplain BlockCapability blocks}, {@linkplain EntityCapability entities},
 * or {@linkplain ItemCapability items}).
 * Otherwise, use one of the subclasses directly.
 *
 * @param <T> Type of queried objects.
 * @param <C> Type of the additional context.
 */
public abstract class BaseCapability<T, C extends @Nullable Object> {
    private final Identifier name;
    private final Class<T> typeClass;
    private final Class<C> contextClass;

    protected BaseCapability(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        this.name = name;
        this.typeClass = typeClass;
        this.contextClass = contextClass;
    }

    /**
     * {@return the name of this capability}
     */
    public final Identifier name() {
        return name;
    }

    /**
     * {@return the type of queried objects}
     */
    public final Class<T> typeClass() {
        return typeClass;
    }

    /**
     * {@return the type of the additional context}
     */
    public final Class<C> contextClass() {
        return contextClass;
    }
}

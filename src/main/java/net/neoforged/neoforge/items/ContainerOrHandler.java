/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import net.minecraft.world.Container;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import org.jetbrains.annotations.Nullable;

public record ContainerOrHandler(
        @Nullable Container container,
        @Nullable IResourceHandler<ItemResource> itemHandler) {
    public ContainerOrHandler {
        if (container != null && itemHandler != null) {
            throw new IllegalArgumentException("Cannot have both a container and an item handler.");
        }
    }

    public static ContainerOrHandler container(Container container) {
        //noinspection ConstantValue This is to mitigate user error. These should NEVER be null, but if they are we should stop.
        if (container == null) {
            throw new IllegalArgumentException("Cannot have a null specified container");
        }
        return new ContainerOrHandler(container, null);
    }

    public static ContainerOrHandler handler(IResourceHandler<ItemResource> itemHandler) {
        //noinspection ConstantValue This is to mitigate user error. These should NEVER be null, but if they are we should stop.
        if (itemHandler == null) {
            throw new IllegalArgumentException("Cannot have a null specified resource handler");
        }
        return new ContainerOrHandler(null, itemHandler);
    }

    public static final ContainerOrHandler EMPTY = new ContainerOrHandler(null, null);

    public boolean isEmpty() {
        return container == null && itemHandler == null;
    }
}

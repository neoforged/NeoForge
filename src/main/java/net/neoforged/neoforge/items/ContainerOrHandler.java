/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import net.minecraft.world.Container;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import org.jetbrains.annotations.Nullable;
//Why?... this is just a fancy either?

/**
 * @deprecated This is just an {@link com.mojang.datafixers.util.Either}
 */
@Deprecated(since = "1.21.4", forRemoval = true)
public record ContainerOrHandler(
        @Nullable Container container,
        @Nullable IResourceHandler<ItemResource> itemHandler) {
    public ContainerOrHandler {
        if (container != null && itemHandler != null) {
            throw new IllegalArgumentException("Cannot have both a container and an item handler.");
        }
    }

    public static final ContainerOrHandler EMPTY = new ContainerOrHandler(null, null);

    public boolean isEmpty() {
        return container == null && itemHandler == null;
    }
}

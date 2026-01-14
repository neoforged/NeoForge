/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * NeoForge extension for {@link net.minecraft.world.item.ItemInstance}.
 */
public interface ItemInstanceExtensions {
    default ItemInstance self() {
        return (ItemInstance) this;
    }

    /**
     * {@return an ItemStackTemplate equivalent to this item instance}
     */
    default ItemStackTemplate toTemplate() {
        throw new UnsupportedOperationException();
    }
}

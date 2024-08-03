/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.flag.Flag;
import net.neoforged.neoforge.network.IContainerFactory;

public interface IMenuTypeExtension<T> {
    static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory, Flag... requiredFlags) {
        return new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS, Set.of(requiredFlags));
    }

    T create(int windowId, Inventory playerInv, RegistryFriendlyByteBuf extraData);
}

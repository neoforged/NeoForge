/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Special {@link DeferredHolder} for {@link MenuType MenuTypes}.
 *
 * @param <TMenu> The specific {@link MenuType}.
 */
public class DeferredMenuType<TMenu extends AbstractContainerMenu> extends DeferredHolder<MenuType<?>, MenuType<TMenu>> {
    protected DeferredMenuType(ResourceKey<MenuType<?>> key) {
        super(key);
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the specified {@link MenuType}.
     *
     * @param <TMenu>     The type of the target {@link MenuType}.
     * @param registryKey The resource key of the target {@link MenuType}.
     */
    public static <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> createMenuType(ResourceKey<MenuType<?>> registryKey) {
        return new DeferredMenuType<>(registryKey);
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the {@link MenuType} with the specified name.
     *
     * @param <TMenu>      The type of the target {@link MenuType}.
     * @param registryName The name of the target {@link MenuType}.
     */
    public static <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> createMenuType(ResourceLocation registryName) {
        return createMenuType(ResourceKey.create(Registries.MENU, registryName));
    }
}

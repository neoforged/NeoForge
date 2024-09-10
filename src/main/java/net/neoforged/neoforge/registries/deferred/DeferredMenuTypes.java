/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;

/**
 * Specialized DeferredRegister for {@link MenuType MenuTypes} that uses the specialized {@link DeferredMenuType} as the return type for {@link #register}.
 */
public class DeferredMenuTypes extends DeferredRegister<MenuType<?>> {
    protected DeferredMenuTypes(String namespace) {
        super(Registries.MENU, namespace);
    }

    @Override
    protected <TMenuType extends MenuType<?>> DeferredHolder<MenuType<?>, TMenuType> createHolder(ResourceKey<? extends Registry<MenuType<?>>> registryType, ResourceLocation registryName) {
        return (DeferredHolder<MenuType<?>, TMenuType>) DeferredMenuType.createMenuType(ResourceKey.create(registryType, registryName));
    }

    /**
     * Adds a new menu type to the list of entries to be registered and returns a {@link DeferredMenuType} that will be populated with the created entry automatically.
     *
     * @param identifier The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new entry. The factory should not cache the created entry.
     * @return A {@link DeferredMenuType} that will track updates from the registry for this entry.
     */
    public <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> registerMenu(String identifier, IContainerFactory<TMenu> factory) {
        return (DeferredMenuType<TMenu>) register(identifier, () -> IMenuTypeExtension.create(factory));
    }

    /**
     * Factory for a specialized DeferredRegister for {@link MenuType MenuTypes}.
     *
     * @param namespace The namespace for all objects registered to this DeferredRegister
     */
    public static DeferredMenuTypes createMenuTypes(String namespace) {
        return new DeferredMenuTypes(namespace);
    }
}

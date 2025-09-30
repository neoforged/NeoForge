/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import com.google.common.collect.MapMaker;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;

/**
 * {@code ResourceHandler<ItemResource>} implementation for the carried slot of an {@link AbstractContainerMenu}.
 */
public final class CarriedSlotWrapper extends ItemStackResourceHandler {
    /**
     * See {@link VanillaContainerWrapper#wrappers} which is similar.
     *
     * <p>We use weak keys and values to avoid keeping a strong reference to the {@link AbstractContainerMenu} until the next time the map is cleaned.
     * As long as a wrapper is used, there is a strong reference to the {@link AbstractContainerMenu} class,
     * which ensures that the entries remain in the map at least as long as the wrappers are in use.
     */
    private static final Map<AbstractContainerMenu, CarriedSlotWrapper> wrappers = new MapMaker().weakKeys().weakValues().makeMap();

    /**
     * Return a wrapper around the carried slot of a menu,
     * i.e. the stack that can be manipulated with {@link AbstractContainerMenu#getCarried}
     * and {@link AbstractContainerMenu#setCarried}.
     */
    public static ResourceHandler<ItemResource> of(AbstractContainerMenu menu) {
        return wrappers.computeIfAbsent(menu, CarriedSlotWrapper::new);
    }

    private final AbstractContainerMenu menu;

    private CarriedSlotWrapper(AbstractContainerMenu menu) {
        this.menu = menu;
    }

    @Override
    protected ItemStack getStack() {
        return menu.getCarried();
    }

    @Override
    protected void setStack(ItemStack stack) {
        menu.setCarried(stack);
    }

    @Override
    public String toString() {
        return "CarriedSlotWrapper[" + menu + "/" + BuiltInRegistries.MENU.getId(menu.getType()) + "]";
    }
}

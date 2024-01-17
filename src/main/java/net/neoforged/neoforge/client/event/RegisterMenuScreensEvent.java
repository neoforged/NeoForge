/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

public class RegisterMenuScreensEvent extends Event implements IModBusEvent {
    private final Map<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>> registeredScreens;

    @ApiStatus.Internal
    public RegisterMenuScreensEvent(Map<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>> registeredScreens) {
        this.registeredScreens = registeredScreens;
    }

    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(
            MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> screenConstructor) {
        if (registeredScreens.containsKey(menuType)) {
            throw new IllegalStateException("Duplicate attempt to register screen: " + BuiltInRegistries.MENU.getKey(menuType));
        }
        registeredScreens.put(menuType, screenConstructor);
    }

    public Map<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>> getRegisteredScreens() {
        return registeredScreens;
    }
}

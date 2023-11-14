/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom {@link net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent}
 * factories for their {@link net.minecraft.world.inventory.tooltip.TooltipComponent} types.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the {@linkplain FMLJavaModLoadingContext#getModEventBus() mod-specific event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterClientTooltipComponentFactoriesEvent extends Event implements IModBusEvent {
    private final Map<Class<? extends TooltipComponent>, Function<TooltipComponent, ClientTooltipComponent>> factories;

    @ApiStatus.Internal
    public RegisterClientTooltipComponentFactoriesEvent(Map<Class<? extends TooltipComponent>, Function<TooltipComponent, ClientTooltipComponent>> factories) {
        this.factories = factories;
    }

    /**
     * Registers a {@link ClientTooltipComponent} factory for a {@link TooltipComponent}.
     */
    @SuppressWarnings("unchecked")
    public <T extends TooltipComponent> void register(Class<T> type, Function<? super T, ? extends ClientTooltipComponent> factory) {
        factories.put(type, (Function<TooltipComponent, ClientTooltipComponent>) factory);
    }
}

/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom {@link DimensionSpecialEffects} for their dimensions.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the {@linkplain FMLJavaModLoadingContext#getModEventBus() mod-specific event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterDimensionSpecialEffectsEvent extends Event implements IModBusEvent {
    private final Map<ResourceLocation, DimensionSpecialEffects> effects;

    @ApiStatus.Internal
    public RegisterDimensionSpecialEffectsEvent(Map<ResourceLocation, DimensionSpecialEffects> effects) {
        this.effects = effects;
    }

    /**
     * Registers the effects for a given dimension type.
     */
    public void register(ResourceLocation dimensionType, DimensionSpecialEffects effects) {
        this.effects.put(dimensionType, effects);
    }
}

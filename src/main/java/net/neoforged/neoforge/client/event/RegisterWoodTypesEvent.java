/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Map;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom {@link WoodType wood types}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterWoodTypesEvent extends Event implements IModBusEvent {
    private final Map<WoodType, Material> signMaterials;
    private final Map<WoodType, Material> hangingSignMaterials;

    @ApiStatus.Internal
    public RegisterWoodTypesEvent(Map<WoodType, Material> signMaterials, Map<WoodType, Material> hangingSignMaterials) {
        this.signMaterials = signMaterials;
        this.hangingSignMaterials = hangingSignMaterials;
    }

    public void register(WoodType woodType) {
        signMaterials.put(woodType, Sheets.createSignMaterial(woodType));
        hangingSignMaterials.put(woodType, Sheets.createHangingSignMaterial(woodType));
    }
}
